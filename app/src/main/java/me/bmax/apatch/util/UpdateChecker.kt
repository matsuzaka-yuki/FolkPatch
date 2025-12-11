package me.bmax.apatch.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.bmax.apatch.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    // Placeholder URL for version file.
    // It is expected to return a plain text integer version code.
    private const val UPDATE_API_URL = "http://192.168.8.238/api/version.php"
    private const val UPDATE_URL = "https://github.com/matsuzaka-yuki/FolkPatch/releases"

    /**
     * Checks for updates.
     * Returns true if an update is available (remote version > current version), false otherwise.
     */
    suspend fun checkUpdate(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(UPDATE_API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val remoteVersionCodeStr = reader.readText().trim()
                    reader.close()
                    
                    val remoteVersionCode = remoteVersionCodeStr.toIntOrNull()
                    if (remoteVersionCode != null) {
                        return@withContext remoteVersionCode > BuildConfig.VERSION_CODE
                    }
                }
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun openUpdateUrl(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
