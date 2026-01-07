package me.bmax.apatch.ui.component

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import me.bmax.apatch.APApplication
import me.bmax.apatch.apApp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

/**
 * KPM自动加载配置数据类
 */
data class KpmAutoLoadConfig(
    val enabled: Boolean = false,
    val kpmPaths: List<String> = emptyList()
)

/**
 * KPM自动加载配置管理器
 */
object KpmAutoLoadManager {
    private const val TAG = "KpmAutoLoadManager"
    private const val CONFIG_FILE_NAME = "kpm_autoload_config.json"
    private const val KPMS_DIR_NAME = "autoload_kpms"

    // 当前配置状态
    var isEnabled = mutableStateOf(false)
        private set
    var kpmPaths = mutableStateOf<List<String>>(emptyList())
        private set

    /**
     * 导入KPM文件到内部存储
     */
    fun importKpm(context: Context, uri: android.net.Uri): String? {
        return try {
            val kpmDir = File(context.filesDir, KPMS_DIR_NAME)
            if (!kpmDir.exists()) {
                kpmDir.mkdirs()
            }

            val fileName = getFileName(context, uri) ?: "unknown_${System.currentTimeMillis()}.kpm"
            val destFile = File(kpmDir, fileName)

            // 如果文件已存在，先删除
            if (destFile.exists()) {
                destFile.delete()
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "KPM导入成功: ${destFile.absolutePath}")
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "KPM导入失败: ${e.message}", e)
            null
        }
    }

    /**
     * 获取文件名
     */
    private fun getFileName(context: Context, uri: android.net.Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    /**
     * 删除不再使用的KPM文件
     */
    fun cleanupUnusedKpms(context: Context, currentPaths: List<String>) {
        try {
            val kpmDir = File(context.filesDir, KPMS_DIR_NAME)
            if (kpmDir.exists() && kpmDir.isDirectory) {
                kpmDir.listFiles()?.forEach { file ->
                    if (file.absolutePath !in currentPaths) {
                        Log.d(TAG, "删除未使用的KPM文件: ${file.absolutePath}")
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理KPM文件失败: ${e.message}", e)
        }
    }

    /**
     * 加载配置文件
     */
    fun loadConfig(context: Context): KpmAutoLoadConfig {
        return try {
            val configFile = File(context.filesDir, CONFIG_FILE_NAME)
            if (!configFile.exists()) {
                Log.d(TAG, "配置文件不存在，使用默认配置")
                return KpmAutoLoadConfig()
            }

            val reader = FileReader(configFile)
            val jsonContent = reader.readText()
            reader.close()

            val config = parseConfigFromJson(jsonContent) ?: KpmAutoLoadConfig()
            isEnabled.value = config.enabled
            kpmPaths.value = config.kpmPaths
            Log.d(TAG, "配置加载成功: enabled=${config.enabled}, kpmPaths=${config.kpmPaths}")
            config
        } catch (e: Exception) {
            Log.e(TAG, "加载配置失败: ${e.message}", e)
            val defaultConfig = KpmAutoLoadConfig()
            isEnabled.value = defaultConfig.enabled
            kpmPaths.value = defaultConfig.kpmPaths
            defaultConfig
        }
    }

    /**
     * 保存配置文件
     */
    fun saveConfig(context: Context, config: KpmAutoLoadConfig): Boolean {
        return try {
            val configFile = File(context.filesDir, CONFIG_FILE_NAME)
            val jsonContent = getConfigJson(config)
            val writer = FileWriter(configFile)
            writer.write(jsonContent)
            writer.close()

            // 更新状态
            isEnabled.value = config.enabled
            kpmPaths.value = config.kpmPaths
            Log.d(TAG, "配置保存成功: enabled=${config.enabled}, kpmPaths=${config.kpmPaths}")

            // 清理未使用的KPM文件
            cleanupUnusedKpms(context, config.kpmPaths)

            true
        } catch (e: IOException) {
            Log.e(TAG, "保存配置失败: ${e.message}", e)
            false
        }
    }

    /**
     * 获取配置的JSON字符串
     */
    fun getConfigJson(): String {
        return getConfigJson(KpmAutoLoadConfig(isEnabled.value, kpmPaths.value))
    }

    /**
     * 获取指定配置的JSON字符串
     */
    fun getConfigJson(config: KpmAutoLoadConfig): String {
        val jsonObject = JSONObject()
        jsonObject.put("enabled", config.enabled)

        val pathsArray = JSONArray()
        config.kpmPaths.forEach { path ->
            pathsArray.put(path)
        }
        jsonObject.put("kpmPaths", pathsArray)

        return jsonObject.toString(2) // 使用缩进格式化
    }

    /**
     * 从JSON字符串解析配置
     */
    fun parseConfigFromJson(jsonString: String): KpmAutoLoadConfig? {
        return try {
            val jsonObject = JSONObject(jsonString)
            val enabled = jsonObject.optBoolean("enabled", false)

            val kpmPaths = mutableListOf<String>()
            val pathsArray = jsonObject.optJSONArray("kpmPaths")
            if (pathsArray != null) {
                for (i in 0 until pathsArray.length()) {
                    pathsArray.optString(i)?.let { path ->
                        if (path.isNotEmpty()) {
                            kpmPaths.add(path)
                        }
                    }
                }
            }

            KpmAutoLoadConfig(enabled, kpmPaths)
        } catch (e: Exception) {
            Log.e(TAG, "解析JSON失败: ${e.message}", e)
            null
        }
    }

    /**
     * 在应用启动时自动加载KPM模块
     */
    fun autoLoadKpmModules(): Boolean {
        Log.d(TAG, "开始检查KPM自动加载配置...")
        Log.d(TAG, "当前状态: enabled=${isEnabled.value}, kpmPaths=${kpmPaths.value}")

        if (!isEnabled.value || kpmPaths.value.isEmpty()) {
            Log.d(TAG, "自动加载未启用或没有配置KPM路径，跳过加载")
            Log.d(TAG, "enabled=${isEnabled.value}, paths empty=${kpmPaths.value.isEmpty()}")
            return false
        }

        var successCount = 0
        var failCount = 0

        kpmPaths.value.forEach { path ->
            try {
                Log.d(TAG, "尝试加载KPM: $path")
                // 检查文件是否存在
                val file = java.io.File(path)
                if (!file.exists()) {
                    Log.e(TAG, "KPM文件不存在: $path")
                    failCount++
                    return@forEach
                }

                val rc = me.bmax.apatch.Natives.loadKernelPatchModule(path, "")
                if (rc == 0L) {
                    successCount++
                    Log.d(TAG, "KPM加载成功: $path")
                } else {
                    failCount++
                    Log.e(TAG, "KPM加载失败: $path, rc=$rc")
                }
            } catch (e: Exception) {
                failCount++
                Log.e(TAG, "KPM加载异常: $path, ${e.message}", e)
            }
        }

        Log.i(TAG, "KPM自动加载完成: 成功=$successCount, 失败=$failCount")
        return successCount > 0
    }

    /**
     * 调试方法：检查当前配置状态
     */
    fun debugConfigState() {
        Log.d(TAG, "=== KPM配置状态调试 ===")
        Log.d(TAG, "enabled=${isEnabled.value}")
        Log.d(TAG, "kpmPaths size=${kpmPaths.value.size}")
        kpmPaths.value.forEachIndexed { index, path ->
            Log.d(TAG, "路径[$index]: $path")
        }
        Log.d(TAG, "=== 调试结束 ===")
    }
}
