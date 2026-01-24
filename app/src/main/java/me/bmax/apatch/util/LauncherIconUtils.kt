package me.bmax.apatch.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import me.bmax.apatch.APApplication

enum class LauncherIconVariant(val aliasName: String) {
    DEFAULT(".ui.MainActivityAliasDefault"),
    CLASSIC(".ui.MainActivityAliasClassic"),
    APATCH(".ui.MainActivityAliasApatch"),
    KERNELSU(".ui.MainActivityAliasKernelSU"),
    KERNELSUNEXT(".ui.MainActivityAliasKernelSUNext"),
    KITSUNE(".ui.MainActivityAliasKitsune"),
    MAGISK(".ui.MainActivityAliasMagisk"),
    SUPERROOT(".ui.MainActivityAliasSuperRoot"),

    DEFAULT_FOLKSU(".ui.MainActivityAliasDefaultFolkSU"),
    CLASSIC_FOLKSU(".ui.MainActivityAliasClassicFolkSU"),
    APATCH_FOLKSU(".ui.MainActivityAliasApatchFolkSU"),
    KERNELSU_FOLKSU(".ui.MainActivityAliasKernelSUFolkSU"),
    KERNELSUNEXT_FOLKSU(".ui.MainActivityAliasKernelSUNextFolkSU"),
    KITSUNE_FOLKSU(".ui.MainActivityAliasKitsuneFolkSU"),
    MAGISK_FOLKSU(".ui.MainActivityAliasMagiskFolkSU"),
    SUPERROOT_FOLKSU(".ui.MainActivityAliasSuperRootFolkSU")
}

object LauncherIconUtils {
    private val aliases = LauncherIconVariant.values().toList()

    fun applyVariant(context: Context, variant: LauncherIconVariant) {
        val pm = context.packageManager
        val basePackage = APApplication::class.java.`package`?.name ?: "me.bmax.apatch"
        aliases.forEach { v ->
            val cn = ComponentName(context.packageName, basePackage + v.aliasName)
            val state = if (v == variant) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            try {
                pm.setComponentEnabledSetting(cn, state, PackageManager.DONT_KILL_APP)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun applySaved(context: Context, variantName: String? = null) {
        val prefs = APApplication.sharedPreferences
        val vName = variantName ?: prefs.getString("launcher_icon_variant", "default")
        val appName = prefs.getString("desktop_app_name", "FolkPatch")
        val isFolkSU = appName == "FolkSU"

        val variant = when (vName) {
            "default" -> if (isFolkSU) LauncherIconVariant.DEFAULT_FOLKSU else LauncherIconVariant.DEFAULT
            "classic" -> if (isFolkSU) LauncherIconVariant.CLASSIC_FOLKSU else LauncherIconVariant.CLASSIC
            "apatch" -> if (isFolkSU) LauncherIconVariant.APATCH_FOLKSU else LauncherIconVariant.APATCH
            "kernelsu" -> if (isFolkSU) LauncherIconVariant.KERNELSU_FOLKSU else LauncherIconVariant.KERNELSU
            "kernelsunext" -> if (isFolkSU) LauncherIconVariant.KERNELSUNEXT_FOLKSU else LauncherIconVariant.KERNELSUNEXT
            "kitsune" -> if (isFolkSU) LauncherIconVariant.KITSUNE_FOLKSU else LauncherIconVariant.KITSUNE
            "magisk" -> if (isFolkSU) LauncherIconVariant.MAGISK_FOLKSU else LauncherIconVariant.MAGISK
            "superroot" -> if (isFolkSU) LauncherIconVariant.SUPERROOT_FOLKSU else LauncherIconVariant.SUPERROOT
            else -> if (isFolkSU) LauncherIconVariant.DEFAULT_FOLKSU else LauncherIconVariant.DEFAULT
        }
        applyVariant(context, variant)
    }
}
