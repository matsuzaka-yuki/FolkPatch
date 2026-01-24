package me.bmax.apatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.bmax.apatch.data.ScriptInfo
import me.bmax.apatch.util.ScriptLibraryManager
import java.io.File

class ScriptLibraryViewModel : ViewModel() {

    private val _scripts = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val scripts: StateFlow<List<ScriptInfo>> = _scripts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadScripts()
    }

    fun loadScripts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loadedScripts = ScriptLibraryManager.loadScripts()
                _scripts.value = loadedScripts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addScript(sourceFile: File, alias: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val scriptInfo = ScriptLibraryManager.addScript(sourceFile, alias)
                if (scriptInfo != null) {
                    val updatedList = _scripts.value.toMutableList()
                    updatedList.add(scriptInfo)
                    _scripts.value = updatedList
                    
                    val saveSuccess = ScriptLibraryManager.saveScripts(updatedList)
                    if (saveSuccess) {
                        onSuccess()
                    } else {
                        onError("保存配置失败")
                    }
                } else {
                    onError("脚本文件复制失败")
                }
            } catch (e: Exception) {
                onError(e.message ?: "未知错误")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeScript(scriptInfo: ScriptInfo, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val removeSuccess = ScriptLibraryManager.removeScript(scriptInfo)
                if (removeSuccess) {
                    val updatedList = _scripts.value.toMutableList()
                    updatedList.remove(scriptInfo)
                    _scripts.value = updatedList
                    
                    val saveSuccess = ScriptLibraryManager.saveScripts(updatedList)
                    if (saveSuccess) {
                        onSuccess()
                    } else {
                        onError("保存配置失败")
                    }
                } else {
                    onError("删除脚本文件失败")
                }
            } catch (e: Exception) {
                onError(e.message ?: "未知错误")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun executeScript(scriptInfo: ScriptInfo, onComplete: (ScriptLibraryManager.ScriptExecutionResult) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = ScriptLibraryManager.executeScript(scriptInfo)
                onComplete(result)
            } catch (e: Exception) {
                onComplete(
                    ScriptLibraryManager.ScriptExecutionResult(
                        success = false,
                        exitCode = -1,
                        output = "",
                        error = e.message ?: "执行失败"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
