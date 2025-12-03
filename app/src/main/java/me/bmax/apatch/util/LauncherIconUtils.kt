package me.bmax.apatch.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

enum class LauncherIconVariant(val aliasName: String) {
    DEFAULT(".ui.MainActivityAliasDefault"),
    CLASSIC(".ui.MainActivityAliasClassic"),
    APATCH(".ui.MainActivityAliasApatch"),
    KERNELSU(".ui.MainActivityAliasKernelSU"),
    KERNELSUNEXT(".ui.MainActivityAliasKernelSUNext"),
    KITSUNE(".ui.MainActivityAliasKitsune"),
    MAGISK(".ui.MainActivityAliasMagisk"),
    SUPERROOT(".ui.MainActivityAliasSuperRoot")
}

object LauncherIconUtils {
    private val aliases = listOf(
        LauncherIconVariant.DEFAULT,
        LauncherIconVariant.CLASSIC,
        LauncherIconVariant.APATCH,
        LauncherIconVariant.KERNELSU,
        LauncherIconVariant.KERNELSUNEXT,
        LauncherIconVariant.KITSUNE,
        LauncherIconVariant.MAGISK,
        LauncherIconVariant.SUPERROOT
    )

    fun applyVariant(context: Context, variant: LauncherIconVariant) {
        val pm = context.packageManager
        aliases.forEach { v ->
            val cn = ComponentName(context.packageName, context.packageName + v.aliasName)
            val state = if (v == variant) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            pm.setComponentEnabledSetting(cn, state, PackageManager.DONT_KILL_APP)
        }
    }

    fun applySaved(context: Context, variantName: String?) {
        val variant = when (variantName) {
            "default" -> LauncherIconVariant.DEFAULT
            "classic" -> LauncherIconVariant.CLASSIC
            "apatch" -> LauncherIconVariant.APATCH
            "kernelsu" -> LauncherIconVariant.KERNELSU
            "kernelsunext" -> LauncherIconVariant.KERNELSUNEXT
            "kitsune" -> LauncherIconVariant.KITSUNE
            "magisk" -> LauncherIconVariant.MAGISK
            "superroot" -> LauncherIconVariant.SUPERROOT
            else -> LauncherIconVariant.DEFAULT
        }
        applyVariant(context, variant)
    }
}

