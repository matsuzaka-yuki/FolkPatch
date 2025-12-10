package me.bmax.apatch.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.bmax.apatch.apApp
import org.json.JSONArray

class OnlineKPMViewModel : ViewModel() {
    companion object {
        private const val TAG = "OnlineKPMViewModel"
        // Placeholder URL. User should update this.
        const val MODULES_URL = "https://folk.mysqil.com/api/modules.php?type=kpm"
    }

    data class OnlineKPM(
        val name: String,
        val version: String,
        val url: String,
        val description: String
    )

    var modules by mutableStateOf<List<OnlineKPM>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
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
                    val list = ArrayList<OnlineKPM>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            OnlineKPM(
                                name = obj.optString("name"),
                                version = obj.optString("version"),
                                url = obj.optString("url"),
                                description = obj.optString("description")
                            )
                        )
                    }
                    modules = list
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
}