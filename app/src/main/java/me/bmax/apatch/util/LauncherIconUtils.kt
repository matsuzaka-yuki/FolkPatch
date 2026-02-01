package me.bmax.apatch.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import me.bmax.apatch.APApplication

object LauncherIconUtils {
    private const val MAIN_ACTIVITY = ".ui.MainActivity"
    private const val ALIAS_ACTIVITY = ".ui.MainActivityAlias"

    fun toggleLauncherIcon(context: Context, useAlt: Boolean) {
        val pm = context.packageManager
        val basePackage = APApplication::class.java.`package`?.name ?: "me.bmax.apatch"
        
        val mainComponent = ComponentName(context.packageName, basePackage + MAIN_ACTIVITY)
        val aliasComponent = ComponentName(context.packageName, basePackage + ALIAS_ACTIVITY)

        try {
            pm.setComponentEnabledSetting(
                if (useAlt) aliasComponent else mainComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            pm.setComponentEnabledSetting(
                if (useAlt) mainComponent else aliasComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applySaved(context: Context) {
        val prefs = APApplication.sharedPreferences
        val useAlt = prefs.getBoolean("use_alt_icon", false)
        toggleLauncherIcon(context, useAlt)
    }
}
