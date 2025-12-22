package me.yuki.folk.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import me.yuki.folk.APApplication
import me.yuki.folk.util.MusicManager
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object ThemeManager {
    private const val TAG = "ThemeManager"
    private const val THEME_CONFIG_FILENAME = "theme.json"
    private const val BACKGROUND_FILENAME = "background.jpg"
    private const val FONT_FILENAME = "font.ttf"
    private const val KEY_STR = "FolkPatchThemeSecretKey2025"

    private fun getSecretKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(KEY_STR.toByteArray())
        return SecretKeySpec(bytes, "AES")
    }

    data class ThemeConfig(
        val isBackgroundEnabled: Boolean,
        val backgroundOpacity: Float,
        val backgroundBlur: Float = 0f,
        val backgroundDim: Float,
        val isDualBackgroundDimEnabled: Boolean = false,
        val backgroundDayDim: Float = 0.0f,
        val backgroundNightDim: Float = 0.0f,
        val isFontEnabled: Boolean,
        val customColor: String,
        val homeLayoutStyle: String,
        val nightModeEnabled: Boolean,
        val nightModeFollowSys: Boolean,
        val useSystemDynamicColor: Boolean,
        val appLanguage: String?,
        // Grid Working Card Background
        val isGridWorkingCardBackgroundEnabled: Boolean = false,
        val gridWorkingCardBackgroundOpacity: Float = 1.0f,
        val isGridDualOpacityEnabled: Boolean = false,
        val gridWorkingCardBackgroundDayOpacity: Float = 1.0f,
        val gridWorkingCardBackgroundNightOpacity: Float = 1.0f,
        val gridWorkingCardBackgroundDim: Float = 0.3f,
        val isGridWorkingCardCheckHidden: Boolean = false,
        val isGridWorkingCardTextHidden: Boolean = false,
        val isGridWorkingCardModeHidden: Boolean = false,
        val isListWorkingCardModeHidden: Boolean = false,
        // Multi-Background Mode
        val isMultiBackgroundEnabled: Boolean = false,
        // Music Config
        val isMusicEnabled: Boolean = false,
        val musicVolume: Float = 1.0f,
        val isAutoPlayEnabled: Boolean = false,
        val isLoopingEnabled: Boolean = false,
        val musicFilename: String? = null,
        // Video Background
        val isVideoBackgroundEnabled: Boolean = false,
        val videoVolume: Float = 0f
    )

    data class ThemeMetadata(
        val name: String,
        val type: String, // "phone" or "tablet"
        val version: String,
        val author: String,
        val description: String
    )

    suspend fun exportTheme(context: Context, uri: Uri, metadata: ThemeMetadata): Boolean {
        return withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "theme_export")
            if (cacheDir.exists()) cacheDir.deleteRecursively()
            cacheDir.mkdirs()

            try {
                // 1. Collect Config
                val prefs = APApplication.sharedPreferences
                val config = ThemeConfig(
                    isBackgroundEnabled = BackgroundConfig.isCustomBackgroundEnabled,
                    backgroundOpacity = BackgroundConfig.customBackgroundOpacity,
                    backgroundBlur = BackgroundConfig.customBackgroundBlur,
                    backgroundDim = BackgroundConfig.customBackgroundDim,
                    isDualBackgroundDimEnabled = BackgroundConfig.isDualBackgroundDimEnabled,
                    backgroundDayDim = BackgroundConfig.customBackgroundDayDim,
                    backgroundNightDim = BackgroundConfig.customBackgroundNightDim,
                    isFontEnabled = FontConfig.isCustomFontEnabled,
                    customColor = prefs.getString("custom_color", "blue") ?: "blue",
                    homeLayoutStyle = prefs.getString("home_layout_style", "default") ?: "default",
                    nightModeEnabled = prefs.getBoolean("night_mode_enabled", false),
                    nightModeFollowSys = prefs.getBoolean("night_mode_follow_sys", true),
                    useSystemDynamicColor = prefs.getBoolean("use_system_color_theme", true),
                    appLanguage = AppCompatDelegate.getApplicationLocales().toLanguageTags(),
                    isGridWorkingCardBackgroundEnabled = BackgroundConfig.isGridWorkingCardBackgroundEnabled,
                    gridWorkingCardBackgroundOpacity = BackgroundConfig.gridWorkingCardBackgroundOpacity,
                    isGridDualOpacityEnabled = BackgroundConfig.isGridDualOpacityEnabled,
                    gridWorkingCardBackgroundDayOpacity = BackgroundConfig.gridWorkingCardBackgroundDayOpacity,
                    gridWorkingCardBackgroundNightOpacity = BackgroundConfig.gridWorkingCardBackgroundNightOpacity,
                    gridWorkingCardBackgroundDim = BackgroundConfig.gridWorkingCardBackgroundDim,
                    isGridWorkingCardCheckHidden = BackgroundConfig.isGridWorkingCardCheckHidden,
                    isGridWorkingCardTextHidden = BackgroundConfig.isGridWorkingCardTextHidden,
                    isGridWorkingCardModeHidden = BackgroundConfig.isGridWorkingCardModeHidden,
                    isListWorkingCardModeHidden = BackgroundConfig.isListWorkingCardModeHidden,
                    isMultiBackgroundEnabled = BackgroundConfig.isMultiBackgroundEnabled,
                    isMusicEnabled = MusicConfig.isMusicEnabled,
                    musicVolume = MusicConfig.volume,
                    isAutoPlayEnabled = MusicConfig.isAutoPlayEnabled,
                    isLoopingEnabled = MusicConfig.isLoopingEnabled,
                    musicFilename = MusicConfig.musicFilename,
                    isVideoBackgroundEnabled = BackgroundConfig.isVideoBackgroundEnabled,
                    videoVolume = BackgroundConfig.videoVolume
                )

                // 2. Write Config JSON
                val json = JSONObject().apply {
                    put("isBackgroundEnabled", config.isBackgroundEnabled)
                    put("backgroundOpacity", config.backgroundOpacity.toDouble())
                    put("backgroundBlur", config.backgroundBlur.toDouble())
                    put("backgroundDim", config.backgroundDim.toDouble())
                    put("isDualBackgroundDimEnabled", config.isDualBackgroundDimEnabled)
                    put("backgroundDayDim", config.backgroundDayDim.toDouble())
                    put("backgroundNightDim", config.backgroundNightDim.toDouble())
                    put("isFontEnabled", config.isFontEnabled)
                    put("customColor", config.customColor)
                    put("homeLayoutStyle", config.homeLayoutStyle)
                    put("nightModeEnabled", config.nightModeEnabled)
                    put("nightModeFollowSys", config.nightModeFollowSys)
                    put("useSystemDynamicColor", config.useSystemDynamicColor)
                    put("appLanguage", config.appLanguage)
                    
                    // Grid Working Card Background
                    put("isGridWorkingCardBackgroundEnabled", config.isGridWorkingCardBackgroundEnabled)
                    put("gridWorkingCardBackgroundOpacity", config.gridWorkingCardBackgroundOpacity.toDouble())
                    put("isGridDualOpacityEnabled", config.isGridDualOpacityEnabled)
                    put("gridWorkingCardBackgroundDayOpacity", config.gridWorkingCardBackgroundDayOpacity.toDouble())
                    put("gridWorkingCardBackgroundNightOpacity", config.gridWorkingCardBackgroundNightOpacity.toDouble())
                    put("gridWorkingCardBackgroundDim", config.gridWorkingCardBackgroundDim.toDouble())
                    put("isGridWorkingCardCheckHidden", config.isGridWorkingCardCheckHidden)
                    put("isGridWorkingCardTextHidden", config.isGridWorkingCardTextHidden)
                    put("isGridWorkingCardModeHidden", config.isGridWorkingCardModeHidden)
                    put("isListWorkingCardModeHidden", config.isListWorkingCardModeHidden)

                    // Multi-Background Mode
                    put("isMultiBackgroundEnabled", config.isMultiBackgroundEnabled)

                    // Music Config
                    put("isMusicEnabled", config.isMusicEnabled)
                    put("musicVolume", config.musicVolume.toDouble())
                    put("isAutoPlayEnabled", config.isAutoPlayEnabled)
                    put("isLoopingEnabled", config.isLoopingEnabled)
                    put("musicFilename", config.musicFilename)

                    // Video Background
                    put("isVideoBackgroundEnabled", config.isVideoBackgroundEnabled)
                    put("videoVolume", config.videoVolume.toDouble())

                    // Add metadata
                    put("meta_name", metadata.name)
                    put("meta_type", metadata.type)
                    put("meta_version", metadata.version)
                    put("meta_author", metadata.author)
                    put("meta_description", metadata.description)
                }
                File(cacheDir, THEME_CONFIG_FILENAME).writeText(json.toString())


                // 3. Copy Background if enabled
                if (config.isBackgroundEnabled) {
                    val extensions = listOf(".jpg", ".png", ".gif", ".webp")
                    for (ext in extensions) {
                        val bgFile = File(context.filesDir, "background$ext")
                        if (bgFile.exists()) {
                            bgFile.copyTo(File(cacheDir, "background$ext"))
                            break // Only one background file should exist
                        }
                    }
                }
                
                // Copy Grid Working Card Background if enabled
                if (config.isGridWorkingCardBackgroundEnabled) {
                    val extensions = listOf(".jpg", ".png", ".gif", ".webp")
                    for (ext in extensions) {
                        val bgFile = File(context.filesDir, "grid_working_card_background$ext")
                        if (bgFile.exists()) {
                            bgFile.copyTo(File(cacheDir, "grid_working_card_background$ext"))
                            break 
                        }
                    }
                }

                // Copy Multi-Backgrounds if enabled
                if (config.isMultiBackgroundEnabled) {
                    val multiBackgrounds = listOf(
                        "background_home",
                        "background_kernel",
                        "background_superuser",
                        "background_system_module",
                        "background_settings"
                    )
                    val extensions = listOf(".jpg", ".png", ".gif", ".webp")
                    
                    for (bgName in multiBackgrounds) {
                        for (ext in extensions) {
                            val bgFile = File(context.filesDir, "$bgName$ext")
                            if (bgFile.exists()) {
                                bgFile.copyTo(File(cacheDir, "$bgName$ext"))
                                break
                            }
                        }
                    }
                }

                // 4. Copy Font if enabled
                if (config.isFontEnabled) {
                    val fontName = FontConfig.customFontFilename
                    if (fontName != null) {
                        val fontFile = File(context.filesDir, fontName)
                        if (fontFile.exists()) {
                            fontFile.copyTo(File(cacheDir, FONT_FILENAME))
                        }
                    }
                }

                // 6. Copy Music if enabled
                if (config.isMusicEnabled) {
                    val musicName = config.musicFilename
                    if (musicName != null) {
                        val musicFile = MusicConfig.getMusicFile(context)
                        if (musicFile != null && musicFile.exists()) {
                            musicFile.copyTo(File(cacheDir, musicName))
                        }
                    }
                }

                // 7. Copy Video Background if enabled
                if (config.isVideoBackgroundEnabled) {
                    val extensions = listOf(".mp4", ".webm", ".mkv")
                    for (ext in extensions) {
                        val videoFile = File(context.filesDir, "video_background$ext")
                        if (videoFile.exists()) {
                            videoFile.copyTo(File(cacheDir, "video_background$ext"))
                            break
                        }
                    }
                }

                // 8. Encrypt and Zip to Uri
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    // Init Cipher
                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
                    cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), IvParameterSpec(iv))

                    // Write IV first
                    os.write(iv)

                    CipherOutputStream(os, cipher).use { cos ->
                        ZipOutputStream(BufferedOutputStream(cos)).use { zos ->
                            cacheDir.listFiles()?.forEach { file ->
                                val entry = ZipEntry(file.name)
                                zos.putNextEntry(entry)
                                FileInputStream(file).use { fis ->
                                    fis.copyTo(zos)
                                }
                                zos.closeEntry()
                            }
                        }
                    }
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Export failed", e)
                false
            } finally {
                cacheDir.deleteRecursively()
            }
        }
    }

    suspend fun readThemeMetadata(context: Context, uri: Uri): ThemeMetadata? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { `is` ->
                    // Read IV
                    val iv = ByteArray(16)
                    if (`is`.read(iv) != 16) return@withContext null

                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(iv))

                    CipherInputStream(`is`, cipher).use { cis ->
                        ZipInputStream(BufferedInputStream(cis)).use { zis ->
                            var entry: ZipEntry?
                            while (zis.nextEntry.also { entry = it } != null) {
                                if (entry!!.name == THEME_CONFIG_FILENAME) {
                                    // Read the JSON content
                                    val jsonStr = zis.bufferedReader().use { it.readText() }
                                    val json = JSONObject(jsonStr)
                                    return@withContext ThemeMetadata(
                                        name = json.optString("meta_name", ""),
                                        type = json.optString("meta_type", "phone"),
                                        version = json.optString("meta_version", ""),
                                        author = json.optString("meta_author", ""),
                                        description = json.optString("meta_description", "")
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read theme metadata", e)
            }
            null
        }
    }

    suspend fun importTheme(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "theme_import")
            if (cacheDir.exists()) cacheDir.deleteRecursively()
            cacheDir.mkdirs()

            try {
                // 1. Decrypt and Unzip
                context.contentResolver.openInputStream(uri)?.use { `is` ->
                    // Read IV
                    val iv = ByteArray(16)
                    if (`is`.read(iv) != 16) throw Exception("Invalid theme file")

                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(iv))

                    CipherInputStream(`is`, cipher).use { cis ->
                        ZipInputStream(BufferedInputStream(cis)).use { zis ->
                            var entry: ZipEntry?
                            while (zis.nextEntry.also { entry = it } != null) {
                                val file = File(cacheDir, entry!!.name)
                                // Prevent path traversal
                                if (!file.canonicalPath.startsWith(cacheDir.canonicalPath)) {
                                    continue
                                }
                                FileOutputStream(file).use { fos ->
                                    zis.copyTo(fos)
                                }
                            }
                        }
                    }
                }

                // 2. Read Config
                val configFile = File(cacheDir, THEME_CONFIG_FILENAME)
                if (!configFile.exists()) return@withContext false
                
                val json = JSONObject(configFile.readText())
                val isBackgroundEnabled = json.optBoolean("isBackgroundEnabled", false)
                val backgroundOpacity = json.optDouble("backgroundOpacity", 0.5).toFloat()
                val backgroundBlur = json.optDouble("backgroundBlur", 0.0).toFloat()
                val backgroundDim = json.optDouble("backgroundDim", 0.2).toFloat()
                val isDualBackgroundDimEnabled = json.optBoolean("isDualBackgroundDimEnabled", false)
                val backgroundDayDim = json.optDouble("backgroundDayDim", backgroundDim.toDouble()).toFloat()
                val backgroundNightDim = json.optDouble("backgroundNightDim", backgroundDim.toDouble()).toFloat()
                val isFontEnabled = json.optBoolean("isFontEnabled", false)
                val customColor = json.optString("customColor", "blue")
                val homeLayoutStyle = json.optString("homeLayoutStyle", "default")
                val nightModeEnabled = json.optBoolean("nightModeEnabled", false)
                val nightModeFollowSys = json.optBoolean("nightModeFollowSys", true)
                val useSystemDynamicColor = json.optBoolean("useSystemDynamicColor", true)
                val appLanguage = json.optString("appLanguage", "")
                
                // Grid Working Card Background
                val isGridWorkingCardBackgroundEnabled = json.optBoolean("isGridWorkingCardBackgroundEnabled", false)
                val gridWorkingCardBackgroundOpacity = json.optDouble("gridWorkingCardBackgroundOpacity", 1.0).toFloat()
                val isGridDualOpacityEnabled = json.optBoolean("isGridDualOpacityEnabled", false)
                val gridWorkingCardBackgroundDayOpacity = json.optDouble("gridWorkingCardBackgroundDayOpacity", gridWorkingCardBackgroundOpacity.toDouble()).toFloat()
                val gridWorkingCardBackgroundNightOpacity = json.optDouble("gridWorkingCardBackgroundNightOpacity", gridWorkingCardBackgroundOpacity.toDouble()).toFloat()
                val gridWorkingCardBackgroundDim = json.optDouble("gridWorkingCardBackgroundDim", 0.3).toFloat()
                val isGridWorkingCardCheckHidden = json.optBoolean("isGridWorkingCardCheckHidden", false)
                val isGridWorkingCardTextHidden = json.optBoolean("isGridWorkingCardTextHidden", false)
                val isGridWorkingCardModeHidden = json.optBoolean("isGridWorkingCardModeHidden", false)
                val isListWorkingCardModeHidden = json.optBoolean("isListWorkingCardModeHidden", false)

                // Video Background
                val isVideoBackgroundEnabled = json.optBoolean("isVideoBackgroundEnabled", false)
                val videoVolume = json.optDouble("videoVolume", 0.0).toFloat()

                // Multi-Background Mode
                val isMultiBackgroundEnabled = json.optBoolean("isMultiBackgroundEnabled", false)

                // Music Config
                val isMusicEnabled = json.optBoolean("isMusicEnabled", false)
                val musicVolume = json.optDouble("musicVolume", 1.0).toFloat()
                val isAutoPlayEnabled = json.optBoolean("isAutoPlayEnabled", false)
                val isLoopingEnabled = json.optBoolean("isLoopingEnabled", false)
                val musicFilename = json.optString("musicFilename", null)

                // 3. Apply Background
                BackgroundConfig.setCustomBackgroundOpacityValue(backgroundOpacity)
                BackgroundConfig.setCustomBackgroundBlurValue(backgroundBlur)
                BackgroundConfig.setCustomBackgroundDimValue(backgroundDim)
                BackgroundConfig.setDualBackgroundDimEnabledState(isDualBackgroundDimEnabled)
                BackgroundConfig.setCustomBackgroundDayDimValue(backgroundDayDim)
                BackgroundConfig.setCustomBackgroundNightDimValue(backgroundNightDim)
                BackgroundConfig.setCustomBackgroundEnabledState(isBackgroundEnabled)

                if (isBackgroundEnabled) {
                    val extensions = listOf(".jpg", ".png", ".gif", ".webp")
                    var bgFound = false
                    for (ext in extensions) {
                        val bgFile = File(cacheDir, "background$ext")
                        if (bgFile.exists()) {
                            // Clear old background files first
                            for (oldExt in extensions) {
                                val oldFile = File(context.filesDir, "background$oldExt")
                                if (oldFile.exists()) oldFile.delete()
                            }
                            
                            val destFile = File(context.filesDir, "background$ext")
                            bgFile.copyTo(destFile, overwrite = true)
                            // Update URI to point to local file with timestamp to force refresh
                             val fileUri = Uri.fromFile(destFile).buildUpon()
                                .appendQueryParameter("t", System.currentTimeMillis().toString())
                                .build()
                             BackgroundConfig.updateCustomBackgroundUri(fileUri.toString())
                             bgFound = true
                             break
                        }
                    }
                    if (!bgFound) {
                        // Fallback logic if needed, or disable background
                    }
                } else {
                     // Maybe clear if we want to enforce theme state exactly
                     // But user might want to keep files.
                     // The requirement implies importing the theme as is.
                }
                
                // Apply Grid Working Card Background
                BackgroundConfig.setGridWorkingCardBackgroundOpacityValue(gridWorkingCardBackgroundOpacity)
                BackgroundConfig.setGridDualOpacityEnabledState(isGridDualOpacityEnabled)
                BackgroundConfig.setGridWorkingCardBackgroundDayOpacityValue(gridWorkingCardBackgroundDayOpacity)
                BackgroundConfig.setGridWorkingCardBackgroundNightOpacityValue(gridWorkingCardBackgroundNightOpacity)
                BackgroundConfig.setGridWorkingCardBackgroundDimValue(gridWorkingCardBackgroundDim)
                BackgroundConfig.setGridWorkingCardBackgroundEnabledState(isGridWorkingCardBackgroundEnabled)
                BackgroundConfig.setGridWorkingCardCheckHiddenState(isGridWorkingCardCheckHidden)
                BackgroundConfig.setGridWorkingCardTextHiddenState(isGridWorkingCardTextHidden)
                BackgroundConfig.setGridWorkingCardModeHiddenState(isGridWorkingCardModeHidden)
                BackgroundConfig.setListWorkingCardModeHiddenState(isListWorkingCardModeHidden)
                
                if (isGridWorkingCardBackgroundEnabled) {
                    val extensions = listOf(".jpg", ".png", ".gif", ".webp")
                    for (ext in extensions) {
                        val bgFile = File(cacheDir, "grid_working_card_background$ext")
                        if (bgFile.exists()) {
                            // Clear old files
                            for (oldExt in extensions) {
                                val oldFile = File(context.filesDir, "grid_working_card_background$oldExt")
                                if (oldFile.exists()) oldFile.delete()
                            }
                            
                            val destFile = File(context.filesDir, "grid_working_card_background$ext")
                            bgFile.copyTo(destFile, overwrite = true)
                            // Update URI
                             val fileUri = Uri.fromFile(destFile).buildUpon()
                                .appendQueryParameter("t", System.currentTimeMillis().toString())
                                .build()
                             BackgroundConfig.updateGridWorkingCardBackgroundUri(fileUri.toString())
                             break
                        }
                    }
                }
                
                // Apply Multi-Background Mode
                BackgroundConfig.setMultiBackgroundEnabledState(isMultiBackgroundEnabled)
                
                if (isMultiBackgroundEnabled) {
                    val multiBackgrounds = listOf(
                        "background_home" to { uri: String? -> BackgroundConfig.updateHomeBackgroundUri(uri) },
                        "background_kernel" to { uri: String? -> BackgroundConfig.updateKernelBackgroundUri(uri) },
                        "background_superuser" to { uri: String? -> BackgroundConfig.updateSuperuserBackgroundUri(uri) },
                        "background_system_module" to { uri: String? -> BackgroundConfig.updateSystemModuleBackgroundUri(uri) },
                        "background_settings" to { uri: String? -> BackgroundConfig.updateSettingsBackgroundUri(uri) }
                    )
                    val extensions = listOf(".jpg", ".png", ".gif", ".webp")

                    for ((bgName, updateAction) in multiBackgrounds) {
                        var bgFound = false
                        for (ext in extensions) {
                            val bgFile = File(cacheDir, "$bgName$ext")
                            if (bgFile.exists()) {
                                // Clear old files
                                for (oldExt in extensions) {
                                    val oldFile = File(context.filesDir, "$bgName$oldExt")
                                    if (oldFile.exists()) oldFile.delete()
                                }
                                
                                val destFile = File(context.filesDir, "$bgName$ext")
                                bgFile.copyTo(destFile, overwrite = true)
                                
                                val fileUri = Uri.fromFile(destFile).buildUpon()
                                    .appendQueryParameter("t", System.currentTimeMillis().toString())
                                    .build()
                                updateAction(fileUri.toString())
                                bgFound = true
                                break
                            }
                        }
                        
                        if (!bgFound) {
                             // Clear existing if not found in theme
                             for (oldExt in extensions) {
                                val oldFile = File(context.filesDir, "$bgName$oldExt")
                                if (oldFile.exists()) oldFile.delete()
                            }
                            updateAction(null)
                        }
                    }
                } else {
                    // If multi-background is disabled in the theme, disable it here.
                    // We might also want to clear the files or at least reset the URIs in config?
                    // BackgroundConfig.setMultiBackgroundEnabledState(false) is already called above.
                    // We don't necessarily delete the files, just like other background settings don't strictly delete files when disabled.
                }

                // Apply Video Background
                BackgroundConfig.setVideoBackgroundEnabledState(isVideoBackgroundEnabled)
                BackgroundConfig.setVideoVolumeValue(videoVolume)

                if (isVideoBackgroundEnabled) {
                    val extensions = listOf(".mp4", ".webm", ".mkv")
                    for (ext in extensions) {
                        val videoFile = File(cacheDir, "video_background$ext")
                        if (videoFile.exists()) {
                            // Clear old files
                            BackgroundManager.clearVideoBackground(context)
                            
                            val destFile = File(context.filesDir, "video_background$ext")
                            videoFile.copyTo(destFile, overwrite = true)
                            
                            val fileUri = Uri.fromFile(destFile).toString()
                            BackgroundConfig.updateVideoBackgroundUri(fileUri)
                            // Restore enabled state as clearVideoBackground resets it
                            BackgroundConfig.setVideoBackgroundEnabledState(true)
                            break
                        }
                    }
                }

                BackgroundConfig.save(context)

                // Apply Music Config
                // First clear existing music to remove old file
                MusicConfig.clearMusic(context)
                
                // Set new configuration
                MusicConfig.setMusicEnabledState(isMusicEnabled)
                MusicConfig.setVolumeValue(musicVolume)
                MusicConfig.setAutoPlayEnabledState(isAutoPlayEnabled)
                MusicConfig.setLoopingEnabledState(isLoopingEnabled)

                if (isMusicEnabled && musicFilename != null && musicFilename != "null") {
                    val musicFile = File(cacheDir, musicFilename)
                    if (musicFile.exists()) {
                         val destFile = File(MusicConfig.getMusicDir(context), musicFilename)
                         musicFile.copyTo(destFile, overwrite = true)
                         MusicConfig.setMusicFilenameValue(musicFilename)
                    }
                }
                MusicConfig.save(context)

                if (isMusicEnabled && isAutoPlayEnabled) {
                    withContext(Dispatchers.Main) {
                        MusicManager.reload()
                    }
                }

                // 4. Apply Font
                if (isFontEnabled) {
                     val fontFile = File(cacheDir, FONT_FILENAME)
                     if (fontFile.exists()) {
                         FontConfig.applyCustomFont(context, fontFile)
                     }
                } else {
                    FontConfig.clearFont(context)
                }
                
                // 5. Apply Color and Home Layout Style
                withContext(Dispatchers.Main) {
                    if (appLanguage.isNotEmpty()) {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(appLanguage))
                    } else {
                        // If empty, it might mean default/system or old theme file. 
                        // We can choose to leave it as is or reset to empty (system default).
                        // Let's assume we keep current user preference if theme doesn't specify.
                        // Or if explicit empty string was saved (system default), we apply it.
                        // But json.optString returns "" if key missing.
                        if (json.has("appLanguage")) {
                             AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                        }
                    }
                }

                APApplication.sharedPreferences.edit()
                    .putString("custom_color", customColor)
                    .putString("home_layout_style", homeLayoutStyle)
                    .putBoolean("night_mode_enabled", nightModeEnabled)
                    .putBoolean("night_mode_follow_sys", nightModeFollowSys)
                    .putBoolean("use_system_color_theme", useSystemDynamicColor)
                    .apply()
                
                // 6. Refresh Theme
                refreshTheme.postValue(true)
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                false
            } finally {
                cacheDir.deleteRecursively()
            }
        }
    }
}
