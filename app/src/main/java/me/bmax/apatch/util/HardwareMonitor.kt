package me.bmax.apatch.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Hardware Monitor for CPU, GPU, Memory, and Swap/ZRAM.
 * Implements stateful calculations for differential metrics (like Adreno GPU load).
 */
object HardwareMonitor {
    private const val TAG = "HardwareMonitor"

    // CPU State
    private var prevTotalCpu: Long = 0
    private var prevIdleCpu: Long = 0

    // GPU State
    private var prevGpuUsage: Long = 0
    private var prevGpuTotal: Long = 0
    private var lastGpuUpdateTime: Long = 0

    // Adreno path
    private const val ADRENO_PATH_NEW = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"
    private const val ADRENO_PATH = "/sys/class/kgsl/kgsl-3d0/gpubusy"
    // Mali path
    private const val MALI_PATH = "/sys/class/misc/mali0/device/utilization"
    // Generic path
    private const val GENERIC_PATH = "/sys/kernel/gpu/gpu_busy"

    data class MemoryInfo(
        val ramTotal: Long,
        val ramUsed: Long,
        val swapTotal: Long,
        val swapUsed: Long,
        val zramTotal: Long,
        val zramUsed: Long
    )

    /**
     * Get CPU Usage percentage (0-100) using differential /proc/stat.
     */
    suspend fun getCpuUsage(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val result = rootShellForResult("cat /proc/stat")
                if (result.isSuccess && result.out.isNotEmpty()) {
                    val line = result.out.firstOrNull { it.startsWith("cpu ") } ?: return@withContext 0
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 8) {
                        // user, nice, system, idle, iowait, irq, softirq
                        val user = parts[1].toLong()
                        val nice = parts[2].toLong()
                        val system = parts[3].toLong()
                        val idle = parts[4].toLong()
                        val iowait = parts[5].toLong()
                        val irq = parts[6].toLong()
                        val softirq = parts[7].toLong()

                        val currentTotal = user + nice + system + idle + iowait + irq + softirq
                        val currentIdle = idle + iowait

                        if (prevTotalCpu == 0L || currentTotal < prevTotalCpu) {
                            prevTotalCpu = currentTotal
                            prevIdleCpu = currentIdle
                            return@withContext 0
                        }

                        val deltaTotal = currentTotal - prevTotalCpu
                        val deltaIdle = currentIdle - prevIdleCpu

                        prevTotalCpu = currentTotal
                        prevIdleCpu = currentIdle

                        if (deltaTotal > 0) {
                            val usage = (deltaTotal - deltaIdle) * 100 / deltaTotal
                            return@withContext usage.toInt().coerceIn(0, 100)
                        }
                    }
                }
                0
            } catch (e: Exception) {
                Log.e(TAG, "Error reading CPU usage", e)
                0
            }
        }
    }

    /**
     * Get GPU Usage percentage (0-100).
     */
    suspend fun getGpuUsage(): Int {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Try Direct Percentage Path (New Adreno)
                val adrenoPercentResult = rootShellForResult("cat $ADRENO_PATH_NEW")
                if (adrenoPercentResult.isSuccess && adrenoPercentResult.out.isNotEmpty()) {
                    val content = adrenoPercentResult.out[0].trim().replace("%", "")
                    val value = content.toIntOrNull()
                    if (value != null) return@withContext value.coerceIn(0, 100)
                }

                // 2. Try Adreno (Cumulative Differential)
                val adrenoResult = rootShellForResult("cat $ADRENO_PATH")
                if (adrenoResult.isSuccess && adrenoResult.out.isNotEmpty()) {
                    val content = adrenoResult.out[0].trim()
                    val parts = content.split(Regex("\\s+"))
                    if (parts.size >= 2) {
                        val currUsage = parts[0].toLongOrNull() ?: 0L
                        val currTotal = parts[1].toLongOrNull() ?: 0L

                        if (lastGpuUpdateTime == 0L || currTotal < prevGpuTotal) {
                            // First run or reset/overflow
                            prevGpuUsage = currUsage
                            prevGpuTotal = currTotal
                            lastGpuUpdateTime = System.currentTimeMillis()
                            return@withContext 0
                        }

                        val deltaUsage = currUsage - prevGpuUsage
                        val deltaTotal = currTotal - prevGpuTotal

                        prevGpuUsage = currUsage
                        prevGpuTotal = currTotal
                        lastGpuUpdateTime = System.currentTimeMillis()

                        if (deltaTotal > 0) {
                            val percent = (deltaUsage.toDouble() / deltaTotal.toDouble() * 100.0).toInt()
                            return@withContext percent.coerceIn(0, 100)
                        }
                        // If deltaTotal is 0, usage hasn't changed or instantaneous?
                        // If values are same as before, load is likely 0 (no new cycles) or constant?
                        // For Adreno, gpubusy is cycles. If no cycles added, load is 0.
                        return@withContext 0
                    }
                }

                // 3. Try Mali (Direct Value)
                val maliResult = rootShellForResult("cat $MALI_PATH")
                if (maliResult.isSuccess && maliResult.out.isNotEmpty()) {
                    val value = maliResult.out[0].trim().toIntOrNull() ?: 0
                    if (value > 100) {
                        return@withContext (value * 100 / 255).coerceIn(0, 100)
                    }
                    return@withContext value.coerceIn(0, 100)
                }
                
                // 4. Try Generic
                val genericResult = rootShellForResult("cat $GENERIC_PATH")
                 if (genericResult.isSuccess && genericResult.out.isNotEmpty()) {
                     val content = genericResult.out[0].trim().replace("%", "")
                     return@withContext content.toIntOrNull()?.coerceIn(0, 100) ?: 0
                 }

                0
            } catch (e: Exception) {
                Log.e(TAG, "Error reading GPU usage", e)
                0
            }
        }
    }

    /**
     * Get Memory and Swap info using `free` command and `/proc/swaps`.
     */
    suspend fun getMemoryInfo(): MemoryInfo {
        return withContext(Dispatchers.IO) {
            var ramTotal = 0L
            var ramUsed = 0L
            var swapTotal = 0L
            var swapUsed = 0L
            var zramTotal = 0L
            var zramUsed = 0L

            try {
                // Use /proc/meminfo for more reliable parsing
                val memInfoResult = rootShellForResult("cat /proc/meminfo")
                if (memInfoResult.isSuccess) {
                    var memTotal = 0L
                    var memFree = 0L
                    var memAvailable = 0L
                    var buffers = 0L
                    var cached = 0L
                    
                    memInfoResult.out.forEach { line ->
                        val parts = line.split(Regex(":\\s+"))
                        if (parts.size >= 2) {
                            val key = parts[0].trim()
                            val valueStr = parts[1].trim().split(Regex("\\s+"))[0]
                            val valueKb = valueStr.toLongOrNull() ?: 0L
                            val valueBytes = valueKb * 1024
                            
                            when (key) {
                                "MemTotal" -> memTotal = valueBytes
                                "MemFree" -> memFree = valueBytes
                                "MemAvailable" -> memAvailable = valueBytes
                                "Buffers" -> buffers = valueBytes
                                "Cached" -> cached = valueBytes
                            }
                        }
                    }
                    
                    ramTotal = memTotal
                    
                    // Calculate Available if MemAvailable is missing (older kernels)
                    if (memAvailable == 0L && memFree > 0) {
                         // Rough estimation: Free + Cached + Buffers
                         // Note: This is an over-estimation as not all cached is reclaimable,
                         // but better than nothing for old kernels.
                         memAvailable = memFree + cached + buffers
                    }
                    
                    // Calculate Used
                    // Used = Total - Available
                    if (ramTotal > 0) {
                        ramUsed = (ramTotal - memAvailable).coerceAtLeast(0)
                    }
                }
                
                // Parse /proc/swaps to distinguish ZRAM vs Swap File
                val swapsResult = rootShellForResult("cat /proc/swaps")
                if (swapsResult.isSuccess) {
                    swapsResult.out.forEach { line ->
                        if (line.trim().startsWith("Filename")) return@forEach
                        val parts = line.split(Regex("\\s+"))
                        if (parts.size >= 4) {
                            val filename = parts[0]
                            val sizeKb = parts[2].toLongOrNull() ?: 0L
                            val usedKb = parts[3].toLongOrNull() ?: 0L
                            
                            val sizeBytes = sizeKb * 1024
                            val usedBytes = usedKb * 1024
                            
                            if (filename.contains("zram")) {
                                zramTotal += sizeBytes
                                zramUsed += usedBytes
                            } else {
                                // Assume everything else is "Swap File" (e.g., /data/swapfile)
                                swapTotal += sizeBytes
                                swapUsed += usedBytes
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading Memory info", e)
            }

            MemoryInfo(ramTotal, ramUsed, swapTotal, swapUsed, zramTotal, zramUsed)
        }
    }
}
