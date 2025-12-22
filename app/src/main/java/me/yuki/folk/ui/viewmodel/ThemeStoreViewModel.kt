package me.yuki.folk.ui.viewmodel

import android.content.Context
import android.net.Uri
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

class ThemeStoreViewModel : ViewModel() {
    companion object {
        private const val TAG = "ThemeStoreViewModel"
        // TODO: Replace with your actual backend URL (e.g., http://your-server.com/api/themes.php)
        // See backend_reference/README.md for server setup instructions
        private const val THEMES_URL = "https://folk.mysqil.com/api/themes.php"
    }

    data class RemoteTheme(
        val id: String,
        val name: String,
        val author: String,
        val description: String,
        val version: String,
        val previewUrl: String,
        val downloadUrl: String,
        val type: String,
        val source: String
    )

    // Original full list
    private var allThemes = listOf<RemoteTheme>()

    var themes by mutableStateOf<List<RemoteTheme>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var filterAuthor by mutableStateOf("")
        private set
    var filterSource by mutableStateOf("all") // all, official, third_party
        private set
    var filterTypePhone by mutableStateOf(true)
        private set
    var filterTypeTablet by mutableStateOf(true)
        private set

    fun updateFilters(author: String, source: String, phone: Boolean, tablet: Boolean) {
        filterAuthor = author
        filterSource = source
        filterTypePhone = phone
        filterTypeTablet = tablet
        applyFilters()
    }

    private fun applyFilters() {
        themes = allThemes.filter { theme ->
            // Search Query Logic
            val matchesSearch = if (searchQuery.isBlank()) true else {
                theme.name.contains(searchQuery, ignoreCase = true) ||
                theme.author.contains(searchQuery, ignoreCase = true) ||
                theme.description.contains(searchQuery, ignoreCase = true)
            }

            // Author Filter
            val matchesAuthor = if (filterAuthor.isBlank()) true else {
                theme.author.contains(filterAuthor, ignoreCase = true)
            }

            // Source Filter
            val matchesSource = when (filterSource) {
                "official" -> theme.source == "official"
                "third_party" -> theme.source != "official"
                else -> true
            }

            // Type Filter
            val matchesType = (filterTypePhone && theme.type == "phone") ||
                              (filterTypeTablet && theme.type == "tablet")

            matchesSearch && matchesAuthor && matchesSource && matchesType
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyFilters()
    }

    var isRefreshing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchThemes() {
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing = true
            errorMessage = null
            try {
                val request = okhttp3.Request.Builder()
                    .url(THEMES_URL)
                    .build()
                
                val response = apApp.okhttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val jsonString = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(jsonString)
                    val list = ArrayList<RemoteTheme>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            RemoteTheme(
                                id = obj.optString("id"),
                                name = obj.optString("name"),
                                author = obj.optString("author"),
                                description = obj.optString("description"),
                                version = obj.optString("version"),
                                previewUrl = obj.optString("preview_url"),
                                downloadUrl = obj.optString("download_url"),
                                type = obj.optString("type", "phone"),
                                source = obj.optString("source", "third_party")
                            )
                        )
                    }
                    allThemes = list
                    // Re-apply filter if search query exists
                    onSearchQueryChange(searchQuery)
                } else {
                    Log.e(TAG, "Failed to fetch themes: ${response.code}")
                    errorMessage = "Failed to fetch themes: HTTP ${response.code}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching themes", e)
                errorMessage = "Error: ${e.message}"
            } finally {
                isRefreshing = false
            }
        }
    }
}
