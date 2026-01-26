package me.bmax.apatch.util

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import me.bmax.apatch.Natives
import org.json.JSONArray

/**
 * AppData - Data management center for badge counts
 * Manages counts for superuser and APM modules
 */
object AppData {
    private const val TAG = "AppData"

    object DataRefreshManager {
        // Private state flows for counts
        private val _superuserCount = MutableStateFlow(0)
        private val _apmModuleCount = MutableStateFlow(0)
        private val _kernelModuleCount = MutableStateFlow(0)

        private var lastRefreshAt = 0L

        // Public read-only state flows
        val superuserCount: StateFlow<Int> = _superuserCount.asStateFlow()
        val apmModuleCount: StateFlow<Int> = _apmModuleCount.asStateFlow()
        val kernelModuleCount: StateFlow<Int> = _kernelModuleCount.asStateFlow()

        /**
         * Refresh all data counts
         */
        suspend fun refreshData(
            enableSuperUser: Boolean,
            enableApm: Boolean,
            enableKernel: Boolean,
            minIntervalMs: Long = 15000L,
            force: Boolean = false
        ) = withContext(Dispatchers.IO) {
            if (!enableSuperUser && !enableApm && !enableKernel) {
                return@withContext
            }

            val now = SystemClock.elapsedRealtime()
            if (!force && now - lastRefreshAt < minIntervalMs) {
                return@withContext
            }
            lastRefreshAt = now

            if (enableSuperUser) {
                val count = getSuperuserCount()
                if (_superuserCount.value != count) {
                    _superuserCount.value = count
                }
            }

            if (enableApm) {
                val count = getApmModuleCount()
                if (_apmModuleCount.value != count) {
                    _apmModuleCount.value = count
                }
            }

            if (enableKernel) {
                val count = getKernelModuleCount()
                if (_kernelModuleCount.value != count) {
                    _kernelModuleCount.value = count
                }
            }
        }
    }

    /**
     * Get superuser count
     * Note: Minus 1 to exclude the APatch manager itself from the count
     */
    private fun getSuperuserCount(): Int {
        return try {
            val uids = Natives.suUids()
            // Subtract 1 because the manager itself is hidden from the list
            (uids.size - 1).coerceAtLeast(0)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get superuser count", e)
            0
        }
    }

    /**
     * Get APM module count
     */
    private suspend fun getApmModuleCount(): Int {
        return try {
            val result = listModules()
            val array = JSONArray(result)
            array.length()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get APM module count", e)
            0
        }
    }

    /**
     * Get kernel module count
     */
    private fun getKernelModuleCount(): Int {
        return try {
            Natives.kernelPatchModuleNum().toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get kernel module count", e)
            0
        }
    }
}

