package me.bmax.apatch.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import me.bmax.apatch.ui.theme.VibrationConfig

object VibrationManager {

    fun vibrate(context: Context) {
        if (!VibrationConfig.isVibrationEnabled) return

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                // Map 0.0-1.0 to 1-255
                val intensity = (VibrationConfig.vibrationIntensity * 255).toInt().coerceIn(1, 255)
                // A short duration for a "tick" or "click" feel
                val duration = 30L

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, intensity))
                    } catch (e: Exception) {
                        // Fallback if amplitude control is not supported or other errors
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(duration)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            // Catch all exceptions including SecurityException to prevent crash
            android.util.Log.e("VibrationManager", "Failed to vibrate", e)
        }
    }
}
