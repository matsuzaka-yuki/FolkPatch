package me.bmax.apatch.ui.screen.settings

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.launch
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SettingsCategory
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.component.rememberConfirmDialog
import me.bmax.apatch.ui.component.rememberLoadingDialog
import me.bmax.apatch.ui.theme.MusicConfig
import me.bmax.apatch.ui.theme.SoundEffectConfig
import me.bmax.apatch.ui.theme.VibrationConfig
import me.bmax.apatch.util.MusicManager
import me.bmax.apatch.util.SoundEffectManager
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils

@Composable
fun formatTime(millis: Int): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultimediaSettings(
    searchText: String,
    snackBarHost: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()

    // --- Launchers ---
    val pickMusicLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                loadingDialog.show()
                val success = MusicConfig.saveMusicFile(context, it)
                loadingDialog.hide()
                if (success) {
                    snackBarHost.showSnackbar(message = context.getString(R.string.settings_music_saved))
                    MusicManager.reload()
                } else {
                    snackBarHost.showSnackbar(message = context.getString(R.string.settings_music_save_error))
                }
            }
        }
    }

    val pickSoundEffectLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                loadingDialog.show()
                val success = SoundEffectConfig.saveSoundEffectFile(context, it)
                loadingDialog.hide()
                if (success) {
                    snackBarHost.showSnackbar(message = context.getString(R.string.settings_sound_effect_selected))
                } else {
                    snackBarHost.showSnackbar(message = "Failed to save sound effect")
                }
            }
        }
    }

    // Multimedia Category
    val multimediaTitle = stringResource(id = R.string.settings_category_multimedia)
    val matchMultimedia = shouldShow(searchText, multimediaTitle)

    val musicTitle = stringResource(id = R.string.settings_background_music)
    val musicSummary = stringResource(id = R.string.settings_background_music_summary)
    val musicEnabledText = stringResource(id = R.string.settings_background_music_enabled)
    val musicPlayingText = if (MusicConfig.musicFilename != null) stringResource(id = R.string.settings_background_music_playing, MusicConfig.musicFilename!!) else ""
    val showMusicSwitch = matchMultimedia || shouldShow(searchText, musicTitle, musicSummary, musicEnabledText, musicPlayingText)

    val selectMusicTitle = stringResource(id = R.string.settings_select_music_file)
    val musicSelectedText = stringResource(id = R.string.settings_music_selected)
    val showSelectMusic = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(searchText, selectMusicTitle, musicSelectedText))

    val autoPlayTitle = stringResource(id = R.string.settings_music_auto_play)
    val autoPlaySummary = stringResource(id = R.string.settings_music_auto_play_summary)
    val showAutoPlay = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(searchText, autoPlayTitle, autoPlaySummary))

    val loopingTitle = stringResource(id = R.string.settings_music_looping)
    val loopingSummary = stringResource(id = R.string.settings_music_looping_summary)
    val showLooping = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(searchText, loopingTitle, loopingSummary))

    val musicVolumeTitle = stringResource(id = R.string.settings_music_volume)
    val showMusicVolume = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(searchText, musicVolumeTitle))

    val playbackControlTitle = stringResource(id = R.string.settings_music_playback_control)
    val showPlaybackControl = MusicConfig.isMusicEnabled && MusicConfig.musicFilename != null && (matchMultimedia || shouldShow(searchText, playbackControlTitle))

    val clearMusicTitle = stringResource(id = R.string.settings_clear_music)
    val showClearMusic = MusicConfig.isMusicEnabled && MusicConfig.musicFilename != null && (matchMultimedia || shouldShow(searchText, clearMusicTitle))

    // Sound Effect Config
    val soundEffectTitle = stringResource(id = R.string.settings_sound_effect)
    val soundEffectSummary = stringResource(id = R.string.settings_sound_effect_summary)
    val soundEffectEnabledText = stringResource(id = R.string.settings_sound_effect_enabled)
    val soundEffectPlayingText = if (SoundEffectConfig.soundEffectFilename != null) stringResource(id = R.string.settings_sound_effect_playing, SoundEffectConfig.soundEffectFilename!!) else ""
    val showSoundEffectSwitch = matchMultimedia || shouldShow(searchText, soundEffectTitle, soundEffectSummary, soundEffectEnabledText, soundEffectPlayingText)

    val selectSoundEffectTitle = stringResource(id = R.string.settings_select_sound_effect)
    val soundEffectSelectedText = stringResource(id = R.string.settings_sound_effect_selected)
    val showSelectSoundEffect = SoundEffectConfig.isSoundEffectEnabled && (matchMultimedia || shouldShow(searchText, selectSoundEffectTitle, soundEffectSelectedText))
    
    val soundEffectScopeTitle = stringResource(id = R.string.settings_sound_effect_scope)
    val showSoundEffectScope = SoundEffectConfig.isSoundEffectEnabled && (matchMultimedia || shouldShow(searchText, soundEffectScopeTitle))

    // Vibration Config
    val vibrationTitle = stringResource(id = R.string.settings_vibration)
    val vibrationSummary = stringResource(id = R.string.settings_vibration_summary)
    val vibrationEnabledText = stringResource(id = R.string.settings_vibration_enabled)
    val showVibrationSwitch = matchMultimedia || shouldShow(searchText, vibrationTitle, vibrationSummary, vibrationEnabledText)

    val vibrationIntensityTitle = stringResource(id = R.string.settings_vibration_intensity)
    val showVibrationIntensity = VibrationConfig.isVibrationEnabled && (matchMultimedia || shouldShow(searchText, vibrationIntensityTitle))

    val vibrationScopeTitle = stringResource(id = R.string.settings_vibration_scope)
    val showVibrationScope = VibrationConfig.isVibrationEnabled && (matchMultimedia || shouldShow(searchText, vibrationScopeTitle))

    val showMultimediaCategory = showMusicSwitch || showSelectMusic || showAutoPlay || showLooping || showMusicVolume || showPlaybackControl || showClearMusic || showSoundEffectSwitch || showSelectSoundEffect || showSoundEffectScope || showVibrationSwitch || showVibrationIntensity || showVibrationScope

    if (showMultimediaCategory) {
        SettingsCategory(
            title = multimediaTitle,
            icon = Icons.Filled.Headset,
            isSearching = searchText.isNotEmpty()
        ) {
            // Background Music
            if (showMusicSwitch) {
                SwitchItem(
                    icon = Icons.Filled.Headset,
                    title = musicTitle,
                    summary = if (MusicConfig.isMusicEnabled) {
                        if (MusicConfig.musicFilename != null) {
                            musicPlayingText
                        } else {
                            musicEnabledText
                        }
                    } else {
                        musicSummary
                    },
                    checked = MusicConfig.isMusicEnabled
                ) {
                    MusicConfig.setMusicEnabledState(it)
                    MusicConfig.save(context)
                    MusicManager.reload()
                }
            }

            if (MusicConfig.isMusicEnabled) {
                // Select Music File
                if (showSelectMusic) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(text = selectMusicTitle) },
                        supportingContent = {
                            if (MusicConfig.musicFilename != null) {
                                Text(
                                    text = musicSelectedText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        leadingContent = { Icon(Icons.Filled.Headset, null) },
                        modifier = Modifier.clickable {
                            try {
                                pickMusicLauncher.launch("audio/*")
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Auto Play
                if (showAutoPlay) {
                    SwitchItem(
                        icon = Icons.Filled.TouchApp,
                        title = autoPlayTitle,
                        summary = autoPlaySummary,
                        checked = MusicConfig.isAutoPlayEnabled
                    ) {
                        MusicConfig.setAutoPlayEnabledState(it)
                        MusicConfig.save(context)
                    }
                }

                // Loop Play
                if (showLooping) {
                    SwitchItem(
                        icon = Icons.Filled.Refresh,
                        title = loopingTitle,
                        summary = loopingSummary,
                        checked = MusicConfig.isLoopingEnabled
                    ) {
                        MusicConfig.setLoopingEnabledState(it)
                        MusicConfig.save(context)
                        MusicManager.updateLooping(it)
                    }
                }

                // Volume
                if (showMusicVolume) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(musicVolumeTitle) },
                        supportingContent = {
                            androidx.compose.material3.Slider(
                                value = MusicConfig.volume,
                                onValueChange = { 
                                    MusicConfig.setVolumeValue(it)
                                    MusicManager.updateVolume(it)
                                },
                                onValueChangeFinished = { MusicConfig.save(context) },
                                valueRange = 0f..1f,
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                )
                            )
                        }
                    )
                }

                // Playback Progress
                val currentPosition by MusicManager.currentPosition.collectAsState(initial = 0)
                val duration by MusicManager.duration.collectAsState(initial = 0)
                val isPlaying by MusicManager.isPlaying.collectAsState(initial = false)

                if (MusicConfig.musicFilename != null) {
                        if (showPlaybackControl) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(playbackControlTitle) },
                            supportingContent = {
                                Column {
                                    androidx.compose.material3.Slider(
                                        value = currentPosition.toFloat(),
                                        onValueChange = { 
                                            MusicManager.seekTo(it.toInt())
                                        },
                                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatTime(currentPosition),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = formatTime(duration),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { MusicManager.toggle() }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }

                // Clear Music
                if (MusicConfig.musicFilename != null) {
                    val clearMusicDialog = rememberConfirmDialog(
                        onConfirm = {
                            MusicConfig.clearMusic(context)
                            MusicManager.stop()
                            scope.launch {
                                snackBarHost.showSnackbar(message = context.getString(R.string.settings_music_cleared))
                            }
                        }
                    )
                    if (showClearMusic) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = clearMusicTitle) },
                            leadingContent = { Icon(Icons.Filled.DeleteSweep, null) },
                            modifier = Modifier.clickable {
                                clearMusicDialog.showConfirm(
                                    title = context.getString(R.string.settings_clear_music),
                                    content = context.getString(R.string.settings_clear_music_confirm)
                                )
                            }
                        )
                    }
                }
            }

            // Sound Effect
            if (showSoundEffectSwitch) {
                SwitchItem(
                    icon = Icons.Filled.Audiotrack,
                    title = soundEffectTitle,
                    summary = if (SoundEffectConfig.isSoundEffectEnabled) {
                        if (SoundEffectConfig.soundEffectFilename != null) {
                            soundEffectPlayingText
                        } else {
                            soundEffectEnabledText
                        }
                    } else {
                        soundEffectSummary
                    },
                    checked = SoundEffectConfig.isSoundEffectEnabled
                ) {
                    SoundEffectConfig.setEnabledState(it)
                    SoundEffectConfig.save(context)
                }
            }

            if (SoundEffectConfig.isSoundEffectEnabled) {
                if (showSelectSoundEffect) {
                        ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(text = selectSoundEffectTitle) },
                        supportingContent = {
                            if (SoundEffectConfig.soundEffectFilename != null) {
                                Text(
                                    text = soundEffectSelectedText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        leadingContent = { Icon(Icons.Filled.Audiotrack, null) },
                        modifier = Modifier.clickable {
                            try {
                                pickSoundEffectLauncher.launch("audio/*")
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                val clearSoundEffectTitle = stringResource(id = R.string.settings_clear_sound_effect)
                val showClearSoundEffect = SoundEffectConfig.isSoundEffectEnabled && SoundEffectConfig.soundEffectFilename != null && (matchMultimedia || shouldShow(searchText, clearSoundEffectTitle))
                
                if (SoundEffectConfig.soundEffectFilename != null) {
                    val clearSoundEffectDialog = rememberConfirmDialog(
                        onConfirm = {
                            SoundEffectConfig.clearSoundEffect(context)
                            scope.launch {
                                snackBarHost.showSnackbar(message = context.getString(R.string.settings_sound_effect_cleared))
                            }
                        }
                    )
                    if (showClearSoundEffect) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = clearSoundEffectTitle) },
                            leadingContent = { Icon(Icons.Filled.DeleteSweep, null) },
                            modifier = Modifier.clickable {
                                clearSoundEffectDialog.showConfirm(
                                    title = context.getString(R.string.settings_clear_sound_effect),
                                    content = context.getString(R.string.settings_clear_sound_effect_confirm)
                                )
                            }
                        )
                    }
                }

                if (showSoundEffectScope) {
                    var showScopeDialog by remember { mutableStateOf(false) }
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(text = soundEffectScopeTitle) },
                        supportingContent = {
                            Text(
                                text = if (SoundEffectConfig.scope == SoundEffectConfig.SCOPE_GLOBAL) 
                                    stringResource(R.string.settings_sound_effect_scope_global)
                                else 
                                    stringResource(R.string.settings_sound_effect_scope_bottom_bar),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        },
                        leadingContent = { Icon(Icons.Filled.Tune, null) },
                        modifier = Modifier.clickable {
                            showScopeDialog = true
                        }
                    )

                    if (showScopeDialog) {
                        BasicAlertDialog(
                            onDismissRequest = { showScopeDialog = false },
                            properties = DialogProperties(
                                decorFitsSystemWindows = true,
                                usePlatformDefaultWidth = false,
                            )
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(310.dp)
                                    .wrapContentHeight(),
                                shape = RoundedCornerShape(30.dp),
                                tonalElevation = AlertDialogDefaults.TonalElevation,
                                color = AlertDialogDefaults.containerColor,
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = soundEffectScopeTitle,
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AlertDialogDefaults.containerColor,
                                        tonalElevation = 2.dp
                                    ) {
                                        Column {
                                            // Global Option
                                            ListItem(
                                                headlineContent = { Text(stringResource(R.string.settings_sound_effect_scope_global)) },
                                                leadingContent = {
                                                    RadioButton(
                                                        selected = SoundEffectConfig.scope == SoundEffectConfig.SCOPE_GLOBAL,
                                                        onClick = null
                                                    )
                                                },
                                                modifier = Modifier.clickable {
                                                    SoundEffectConfig.setScopeValue(SoundEffectConfig.SCOPE_GLOBAL)
                                                    SoundEffectConfig.save(context)
                                                    showScopeDialog = false
                                                }
                                            )

                                            // Bottom Bar Option
                                            ListItem(
                                                headlineContent = { Text(stringResource(R.string.settings_sound_effect_scope_bottom_bar)) },
                                                leadingContent = {
                                                    RadioButton(
                                                        selected = SoundEffectConfig.scope == SoundEffectConfig.SCOPE_BOTTOM_BAR,
                                                        onClick = null
                                                    )
                                                },
                                                modifier = Modifier.clickable {
                                                    SoundEffectConfig.setScopeValue(SoundEffectConfig.SCOPE_BOTTOM_BAR)
                                                    SoundEffectConfig.save(context)
                                                    showScopeDialog = false
                                                }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 24.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { showScopeDialog = false }) {
                                            Text(stringResource(id = android.R.string.cancel))
                                        }
                                    }
                                }
                                val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
                                APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
                            }
                        }
                    }
                }
            }

            // Vibration
            if (showVibrationSwitch) {
                SwitchItem(
                    icon = Icons.Filled.Vibration,
                    title = vibrationTitle,
                    summary = if (VibrationConfig.isVibrationEnabled) vibrationEnabledText else vibrationSummary,
                    checked = VibrationConfig.isVibrationEnabled
                ) {
                    VibrationConfig.setEnabledState(it)
                    VibrationConfig.save(context)
                }
            }

            if (VibrationConfig.isVibrationEnabled) {
                if (showVibrationScope) {
                    var showScopeDialog by remember { mutableStateOf(false) }
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(text = vibrationScopeTitle) },
                        supportingContent = {
                            Text(
                                text = if (VibrationConfig.scope == VibrationConfig.SCOPE_GLOBAL) 
                                    stringResource(R.string.settings_vibration_scope_global)
                                else 
                                    stringResource(R.string.settings_vibration_scope_bottom_bar),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        },
                        leadingContent = { Icon(Icons.Filled.Tune, null) },
                        modifier = Modifier.clickable {
                            showScopeDialog = true
                        }
                    )

                    if (showScopeDialog) {
                        BasicAlertDialog(
                            onDismissRequest = { showScopeDialog = false },
                            properties = DialogProperties(
                                decorFitsSystemWindows = true,
                                usePlatformDefaultWidth = false,
                            )
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(310.dp)
                                    .wrapContentHeight(),
                                shape = RoundedCornerShape(30.dp),
                                tonalElevation = AlertDialogDefaults.TonalElevation,
                                color = AlertDialogDefaults.containerColor,
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = vibrationScopeTitle,
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AlertDialogDefaults.containerColor,
                                        tonalElevation = 2.dp
                                    ) {
                                        Column {
                                            // Global Option
                                            ListItem(
                                                headlineContent = { Text(stringResource(R.string.settings_vibration_scope_global)) },
                                                leadingContent = {
                                                    RadioButton(
                                                        selected = VibrationConfig.scope == VibrationConfig.SCOPE_GLOBAL,
                                                        onClick = null
                                                    )
                                                },
                                                modifier = Modifier.clickable {
                                                    VibrationConfig.setScopeValue(VibrationConfig.SCOPE_GLOBAL)
                                                    VibrationConfig.save(context)
                                                    showScopeDialog = false
                                                }
                                            )

                                            // Bottom Bar Option
                                            ListItem(
                                                headlineContent = { Text(stringResource(R.string.settings_vibration_scope_bottom_bar)) },
                                                leadingContent = {
                                                    RadioButton(
                                                        selected = VibrationConfig.scope == VibrationConfig.SCOPE_BOTTOM_BAR,
                                                        onClick = null
                                                    )
                                                },
                                                modifier = Modifier.clickable {
                                                    VibrationConfig.setScopeValue(VibrationConfig.SCOPE_BOTTOM_BAR)
                                                    VibrationConfig.save(context)
                                                    showScopeDialog = false
                                                }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 24.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { showScopeDialog = false }) {
                                            Text(stringResource(id = android.R.string.cancel))
                                        }
                                    }
                                }
                                val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
                                APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
                            }
                        }
                    }
                }

                if (showVibrationIntensity) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(vibrationIntensityTitle) },
                        supportingContent = {
                            androidx.compose.material3.Slider(
                                value = VibrationConfig.vibrationIntensity,
                                onValueChange = { 
                                    VibrationConfig.setIntensityValue(it)
                                },
                                onValueChangeFinished = { VibrationConfig.save(context) },
                                valueRange = 0f..1f,
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                )
                            )
                        },
                        leadingContent = { Icon(Icons.Filled.Vibration, null) }
                    )
                }
            }
        }
    }
}
