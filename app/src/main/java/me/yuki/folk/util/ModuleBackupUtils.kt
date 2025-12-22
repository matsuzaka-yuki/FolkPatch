package me.yuki.folk.util

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.yuki.folk.APApplication
import me.yuki.folk.R
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.security.MessageDigest

import android.os.Environment

object ModuleBackupUtils {

    private const val MODULE_DIR = "/data/adb/modules"

    suspend fun autoBackupModule(context: Context, file: File, originalFileName: String?): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (!APApplication.sharedPreferences.getBoolean("auto_backup_module", false)) {
                    return@withContext null
                }

                val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FolkPatch/ModuleBackups")
                if (!backupDir.exists()) backupDir.mkdirs()

                // Calculate hash of the incoming file
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                file.inputStream().use { input ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }
                val fileHash = digest.digest().joinToString("") { "%02x".format(it) }

                val baseName = originalFileName ?: file.name
                val nameWithoutExt = baseName.substringBeforeLast(".")
                val ext = baseName.substringAfterLast(".", "")
                val extWithDot = if (ext.isNotEmpty()) ".$ext" else ""

                var counter = 0
                while (true) {
                    val candidateName = if (counter == 0) baseName else "$nameWithoutExt ($counter)$extWithDot"
                    val candidateFile = File(backupDir, candidateName)

                    if (candidateFile.exists()) {
                        // Check hash
                        val existingDigest = MessageDigest.getInstance("SHA-256")
                        candidateFile.inputStream().use { input ->
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                existingDigest.update(buffer, 0, bytesRead)
                            }
                        }
                        val existingHash = existingDigest.digest().joinToString("") { "%02x".format(it) }

                        if (fileHash == existingHash) {
                            return@withContext "Duplicate found: $candidateName"
                        }
                        // Hash mismatch, try next name
                        counter++
                    } else {
                        // File doesn't exist, save here
                        file.copyTo(candidateFile)
                        return@withContext null // Success
                    }
                }
                @Suppress("UNREACHABLE_CODE")
                null
            } catch (e: Exception) {
                e.message
            }
        }
    }

    suspend fun backupModules(context: Context, snackBarHost: SnackbarHostState, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                // Use the busybox bundled with APatch
                val busyboxPath = "/data/adb/ap/bin/busybox"
                val tempFile = File(context.cacheDir, "backup_tmp.tar.gz")
                val tempPath = tempFile.absolutePath

                if (tempFile.exists()) tempFile.delete()

                // Construct command to tar the modules directory to temp file
                // And chmod it so the app can read it
                val command = "cd \"$MODULE_DIR\" && $busyboxPath tar -czf \"$tempPath\" ./* && chmod 666 \"$tempPath\""

                val result = getRootShell().newJob().add(command).exec()

                if (result.isSuccess) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        tempFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.delete()
                    withContext(Dispatchers.Main) {
                        snackBarHost.showSnackbar(context.getString(R.string.apm_backup_success))
                    }
                } else {
                    val error = result.err.joinToString("\n")
                    withContext(Dispatchers.Main) {
                        snackBarHost.showSnackbar(context.getString(R.string.apm_backup_failed_msg, error))
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    snackBarHost.showSnackbar(context.getString(R.string.apm_backup_failed_msg, e.message))
                }
            }
        }
    }

    suspend fun restoreModules(context: Context, snackBarHost: SnackbarHostState, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val busyboxPath = "/data/adb/ap/bin/busybox"
                val tempFile = File(context.cacheDir, "restore_tmp.tar.gz")
                val tempPath = tempFile.absolutePath

                if (tempFile.exists()) tempFile.delete()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Make sure root can read it
                tempFile.setReadable(true, false)

                val command = "cd \"$MODULE_DIR\" && $busyboxPath tar -xzf \"$tempPath\""
                val result = getRootShell().newJob().add(command).exec()

                tempFile.delete()

                if (result.isSuccess) {
                    // Refresh module list
                    // APatchCli.refresh() // Wait, this refreshes shell, not module list. 
                    // Module list is refreshed by viewModel.fetchModuleList() in UI
                    
                    withContext(Dispatchers.Main) {
                        snackBarHost.showSnackbar(context.getString(R.string.apm_restore_success))
                    }
                } else {
                    val error = result.err.joinToString("\n")
                    withContext(Dispatchers.Main) {
                        snackBarHost.showSnackbar(context.getString(R.string.apm_restore_failed_msg, error))
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    snackBarHost.showSnackbar(context.getString(R.string.apm_restore_failed_msg, e.message))
                }
            }
        }
    }
}
