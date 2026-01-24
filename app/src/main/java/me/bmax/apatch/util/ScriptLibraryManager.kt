package me.bmax.apatch.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.bmax.apatch.data.ScriptInfo
import java.io.File

 object ScriptLibraryManager {

    private const val BASE_DIR = "/storage/emulated/0/Download/FolkPatch"
    private const val SCRIPTS_DIR = "$BASE_DIR/script"
    private const val CONFIG_FILE = "$BASE_DIR/scripts_library.json"

    private val gson = Gson()

    data class ScriptExecutionResult(
        val success: Boolean,
        val exitCode: Int,
        val output: String,
        val error: String
    )

    suspend fun loadScripts(): List<ScriptInfo> = withContext(Dispatchers.IO) {
        try {
            val configFile = File(CONFIG_FILE)
            if (!configFile.exists()) {
                return@withContext emptyList()
            }

            val json = configFile.readText()
            val type = object : TypeToken<List<ScriptInfo>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveScripts(scripts: List<ScriptInfo>): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(scripts)
            val configFile = File(CONFIG_FILE)

            val baseDir = File(BASE_DIR)
            if (!baseDir.exists()) {
                baseDir.mkdirs()
            }

            val scriptsDir = File(SCRIPTS_DIR)
            if (!scriptsDir.exists()) {
                scriptsDir.mkdirs()
            }

            configFile.writeText(json)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addScript(sourceFile: File, alias: String): ScriptInfo? = withContext(Dispatchers.IO) {
        try {
            val baseDir = File(BASE_DIR)
            if (!baseDir.exists()) {
                baseDir.mkdirs()
            }

            val scriptsDir = File(SCRIPTS_DIR)
            if (!scriptsDir.exists()) {
                scriptsDir.mkdirs()
            }

            val scriptId = java.util.UUID.randomUUID().toString()
            
            val originalName = sourceFile.name
            var scriptFileName = originalName
            
            if (!originalName.endsWith(".sh", ignoreCase = true)) {
                scriptFileName = "$originalName.sh"
            }
            
            var scriptFile = File(scriptsDir, scriptFileName)
            var counter = 1
            
            while (scriptFile.exists()) {
                val nameWithoutExt = if (scriptFileName.endsWith(".sh", ignoreCase = true)) {
                    scriptFileName.substring(0, scriptFileName.length - 3)
                } else {
                    scriptFileName
                }
                scriptFileName = "${nameWithoutExt}_$counter.sh"
                scriptFile = File(scriptsDir, scriptFileName)
                counter++
            }
            
            val scriptPath = scriptFile.absolutePath

            sourceFile.inputStream().use { input ->
                scriptFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            scriptFile.setExecutable(true)

            val finalAlias = alias.ifEmpty { 
                if (scriptFileName.endsWith(".sh", ignoreCase = true)) {
                    scriptFileName.substring(0, scriptFileName.length - 3)
                } else {
                    scriptFileName
                }
            }
            
            val scriptInfo = ScriptInfo(
                id = scriptId,
                path = scriptPath,
                alias = finalAlias
            )

            scriptInfo
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun removeScript(scriptInfo: ScriptInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val scriptFile = File(scriptInfo.path)
            if (scriptFile.exists()) {
                scriptFile.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun executeScript(scriptInfo: ScriptInfo): ScriptExecutionResult = withContext(Dispatchers.IO) {
        try {
            val outList = ArrayList<String>()
            val errList = ArrayList<String>()

            val result = getRootShell().newJob()
                .add("sh \"${scriptInfo.path}\"")
                .to(outList, errList)
                .exec()

            ScriptExecutionResult(
                success = result.isSuccess,
                exitCode = result.code,
                output = outList.joinToString("\n"),
                error = errList.joinToString("\n")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ScriptExecutionResult(
                success = false,
                exitCode = -1,
                output = "",
                error = e.message ?: "执行失败"
            )
        }
    }

    suspend fun executeScriptWithCallbacks(
        scriptInfo: ScriptInfo,
        onStdout: (String) -> Unit,
        onStderr: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val shell = getRootShell()

            val stdoutCallback = object : CallbackList<String?>() {
                override fun onAddElement(s: String?) {
                    onStdout(s ?: "")
                }
            }

            val stderrCallback = object : CallbackList<String?>() {
                override fun onAddElement(s: String?) {
                    onStderr(s ?: "")
                }
            }

            val result = shell.newJob()
                .add("sh \"${scriptInfo.path}\"")
                .to(stdoutCallback, stderrCallback)
                .exec()

            result.isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            onStderr("Error: ${e.message}\n")
            false
        }
    }
}
