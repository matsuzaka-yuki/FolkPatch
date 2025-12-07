package me.bmax.apatch.ui.theme

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import java.io.File

object FontConfig {
    private const val PREFS_NAME = "font_settings"
    private const val KEY_CUSTOM_FONT_ENABLED = "custom_font_enabled"
    private const val KEY_CUSTOM_FONT_PATH = "custom_font_path"
    private const val TAG = "FontConfig"

    var isCustomFontEnabled: Boolean by mutableStateOf(false)
        private set
        
    var customFontFilename: String? by mutableStateOf(null)
        private set

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isCustomFontEnabled = prefs.getBoolean(KEY_CUSTOM_FONT_ENABLED, false)
        customFontFilename = prefs.getString(KEY_CUSTOM_FONT_PATH, null)
        
        // Migration: If enabled but no filename, try to migrate from old fixed filename
        if (isCustomFontEnabled && customFontFilename == null) {
            val oldFixedFile = File(context.filesDir, "custom_font.ttf")
            if (oldFixedFile.exists()) {
                val newName = "custom_font_${System.currentTimeMillis()}.ttf"
                if (oldFixedFile.renameTo(File(context.filesDir, newName))) {
                    customFontFilename = newName
                    save(context)
                }
            }
        }
        
        // Validate if file exists
        if (isCustomFontEnabled && customFontFilename != null) {
            val file = File(context.filesDir, customFontFilename!!)
            if (!file.exists()) {
                isCustomFontEnabled = false
                customFontFilename = null
                save(context)
            }
        }
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_CUSTOM_FONT_ENABLED, isCustomFontEnabled)
            .putString(KEY_CUSTOM_FONT_PATH, customFontFilename)
            .apply()
    }

    fun saveFontFile(context: Context, uri: Uri): Boolean {
        return try {
            val newFilename = "custom_font_${System.currentTimeMillis()}.ttf"
            val oldFilename = customFontFilename
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                val file = File(context.filesDir, newFilename)
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Delete old file if it exists and is different
            if (oldFilename != null && oldFilename != newFilename) {
                val oldFile = File(context.filesDir, oldFilename)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            }
            
            isCustomFontEnabled = true
            customFontFilename = newFilename
            save(context)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save font file", e)
            false
        }
    }

    fun clearFont(context: Context) {
        if (customFontFilename != null) {
            val file = File(context.filesDir, customFontFilename!!)
            if (file.exists()) {
                file.delete()
            }
        }
        isCustomFontEnabled = false
        customFontFilename = null
        save(context)
    }

    fun getFontFamily(context: Context): FontFamily {
        if (isCustomFontEnabled && customFontFilename != null) {
            val file = File(context.filesDir, customFontFilename!!)
            if (file.exists()) {
                try {
                    val typeface = Typeface.createFromFile(file)
                    return FontFamily(typeface)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load custom font", e)
                }
            }
        }
        return FontFamily.Default
    }
}
