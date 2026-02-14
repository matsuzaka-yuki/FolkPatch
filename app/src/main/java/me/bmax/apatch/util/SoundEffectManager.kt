package me.bmax.apatch.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.bmax.apatch.ui.theme.SoundEffectConfig
import java.io.File

object SoundEffectManager {
    private const val TAG = "SoundEffectManager"
    private var mediaPlayer: MediaPlayer? = null
    
    // Use Main dispatcher as MediaPlayer must be created/accessed on same thread or handle synch
    // But prepare can be async.
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun play(context: Context) {
        if (!SoundEffectConfig.isSoundEffectEnabled) return
        
        // Scope check is done by the caller (onClick listener)
        
        val sourceType = SoundEffectConfig.sourceType
        
        scope.launch {
            playSound(context, sourceType, SoundEffectConfig.presetName, SoundEffectConfig.soundEffectFilename, "sound")
        }
    }

    fun playStartup(context: Context) {
        if (!SoundEffectConfig.isStartupSoundEnabled) return
        
        val sourceType = SoundEffectConfig.startupSourceType
        
        scope.launch {
            playSound(context, sourceType, SoundEffectConfig.startupPresetName, SoundEffectConfig.startupSoundFilename, "start")
        }
    }

    private fun playSound(context: Context, sourceType: String, presetName: String, filename: String?, assetDir: String) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            } else {
                mediaPlayer?.reset()
            }

            mediaPlayer?.apply {
                if (sourceType == SoundEffectConfig.SOURCE_TYPE_PRESET) {
                    val afd = context.assets.openFd("$assetDir/$presetName.wav")
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } else {
                    if (filename == null) return
                    val file = File(SoundEffectConfig.getSoundEffectDir(context), filename)
                    if (!file.exists()) return
                    setDataSource(context, Uri.fromFile(file))
                }
                
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: $what, $extra")
                    mp.reset()
                    true
                }
                prepareAsync() // Use async to avoid blocking UI
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound effect", e)
            mediaPlayer = null // Reset on hard failure
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
