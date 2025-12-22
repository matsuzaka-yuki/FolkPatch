package me.yuki.folk.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.yuki.folk.apApp
import org.json.JSONArray
import android.net.Uri

class OnlineModuleViewModel : ViewModel() {
    companion object {
        private const val TAG = "OnlineModuleViewModel"
        // Placeholder URL. User should update this.
        const val MODULES_URL = "https://folk.mysqil.com/api/modules.php?type=apm"
    }

    data class OnlineModule(
        val name: String,
        val version: String,
        val url: String,
        val description: String
    )

    var modules by mutableStateOf<List<OnlineModule>>(emptyList())
        private set

    private var allModules = listOf<OnlineModule>()

    var searchQuery by mutableStateOf("")
        private set

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            modules = allModules
        } else {
            modules = allModules.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    var isRefreshing by mutableStateOf(false)
        private set
    
    // Track which modules are being downloaded
    private val downloadingModules = mutableSetOf<String>()
    
    // Track recently completed downloads to show completion messages
    private val recentlyCompletedDownloads = mutableSetOf<String>()
    var currentDownloadId by mutableStateOf<Long?>(null)
        private set

    fun fetchModules() {
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing = true
            try {
                val response = apApp.okhttpClient.newCall(
                    okhttp3.Request.Builder().url(MODULES_URL).build()
                ).execute()
                
                if (response.isSuccessful) {
                    val jsonString = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(jsonString)
                    val list = ArrayList<OnlineModule>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            OnlineModule(
                                name = obj.optString("name"),
                                version = obj.optString("version"),
                                url = obj.optString("url"),
                                description = obj.optString("description")
                            )
                        )
                    }
                    modules = list
                    allModules = list
                    onSearchQueryChange(searchQuery)
                } else {
                    Log.e(TAG, "Failed to fetch modules: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching modules", e)
            } finally {
                isRefreshing = false
            }
        }
    }
    
    fun isDownloading(moduleName: String): Boolean {
        return downloadingModules.contains(moduleName)
    }
    
    fun startDownload(moduleName: String) {
        downloadingModules.add(moduleName)
        // Remove from recently completed if it was there
        recentlyCompletedDownloads.remove(moduleName)
    }
    
    fun finishDownload(moduleName: String) {
        downloadingModules.remove(moduleName)
        recentlyCompletedDownloads.add(moduleName)
    }
    
    fun wasDownloadJustCompleted(moduleName: String): Boolean {
        return if (recentlyCompletedDownloads.contains(moduleName)) {
            recentlyCompletedDownloads.remove(moduleName)
            true
        } else {
            false
        }
    }
    
    fun handleDownloadComplete(uri: Uri) {
        // Try to determine which module completed from the URI
        val uriString = uri.toString()
        
        // Simple approach: try to match the URI with module URLs
        for (module in modules) {
            if (uriString.contains(module.name) || uriString.contains("${module.name}-${module.version}.zip")) {
                finishDownload(module.name)
                return
            }
        }
        
        // If we can't determine the module, clear all downloads as a fallback
        val currentlyDownloading = downloadingModules.toList()
        for (moduleName in currentlyDownloading) {
            finishDownload(moduleName)
        }
        
        currentDownloadId = null
    }
}
