package me.bmax.apatch.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import me.bmax.apatch.ui.component.FilePickerDialog
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RemoveFromQueue
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.Notifications
import me.bmax.apatch.ui.component.CheckboxItem
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.generated.destinations.ThemeStoreScreenDestination
import androidx.compose.material.icons.filled.Settings
import me.bmax.apatch.ui.theme.BackupConfig
import me.bmax.apatch.util.WebDavUtils
import me.bmax.apatch.util.BackupLogManager
import androidx.compose.material.icons.filled.Cloud

import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.APApplication
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SettingsCategory
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.component.rememberConfirmDialog
import me.bmax.apatch.ui.component.rememberLoadingDialog
import me.bmax.apatch.ui.theme.BackgroundConfig
import me.bmax.apatch.ui.theme.BackgroundManager
import me.bmax.apatch.ui.theme.FontConfig
import me.bmax.apatch.ui.theme.ThemeManager
import me.bmax.apatch.ui.theme.SoundEffectConfig
import me.bmax.apatch.util.SoundEffectManager
import me.bmax.apatch.ui.theme.refreshTheme
import me.bmax.apatch.util.APatchKeyHelper
import me.bmax.apatch.util.PermissionRequestHandler
import me.bmax.apatch.util.PermissionUtils
import me.bmax.apatch.util.getBugreportFile
import me.bmax.apatch.util.isGlobalNamespaceEnabled
import me.bmax.apatch.util.isMagicMountEnabled
import me.bmax.apatch.util.isOverlayFSModeEnabled
import me.bmax.apatch.util.outputStream
import me.bmax.apatch.util.rootShellForResult
import me.bmax.apatch.util.setGlobalNamespaceEnabled
import me.bmax.apatch.util.setMagicMountEnabled
import me.bmax.apatch.util.setOverlayFSModeEnabled
import me.bmax.apatch.util.getSELinuxMode
import me.bmax.apatch.util.setSELinuxMode
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils
import me.bmax.apatch.util.ui.LocalSnackbarHost
import me.bmax.apatch.util.ui.NavigationBarsSpacer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import me.bmax.apatch.ui.theme.MusicConfig
import me.bmax.apatch.util.MusicManager
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Dashboard
import me.bmax.apatch.util.UpdateChecker
import me.bmax.apatch.ui.component.UpdateDialog
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ViewList
import me.bmax.apatch.ui.component.SearchAppBar

@Composable
fun formatTime(millis: Int): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Destination<RootGraph>
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingScreen(navigator: DestinationsNavigator) {
    val state by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = state != APApplication.State.UNKNOWN_STATE
    val aPatchReady =
        (state == APApplication.State.ANDROIDPATCH_INSTALLING || state == APApplication.State.ANDROIDPATCH_INSTALLED || state == APApplication.State.ANDROIDPATCH_NEED_UPDATE)
    var isGlobalNamespaceEnabled by rememberSaveable {
        mutableStateOf(false)
    }
    var isMagicMountEnabled by rememberSaveable {
        mutableStateOf(false)
    }
    var isOverlayFSModeEnabled by rememberSaveable {
        mutableStateOf(false)
    }
    var currentSELinuxMode by rememberSaveable {
        mutableStateOf("Unknown")
    }
    var bSkipStoreSuperKey by rememberSaveable {
        mutableStateOf(APatchKeyHelper.shouldSkipStoreSuperKey())
    }
    var searchText by rememberSaveable { mutableStateOf("") }

    if (kPatchReady && aPatchReady) {
        isGlobalNamespaceEnabled = isGlobalNamespaceEnabled()
        isMagicMountEnabled = isMagicMountEnabled()
        isOverlayFSModeEnabled = isOverlayFSModeEnabled()
        currentSELinuxMode = getSELinuxMode()
    }

    val snackBarHost = LocalSnackbarHost.current

    Scaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.settings)) },
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onClearClick = { searchText = "" },
                dropdownContent = {
                }
            )
        },
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackBarHost) }
    ) { paddingValues ->

        fun shouldShow(vararg texts: String?): Boolean {
            if (searchText.isEmpty()) return true
            return texts.any { it?.contains(searchText, ignoreCase = true) == true }
        }

        val loadingDialog = rememberLoadingDialog()
        val clearKeyDialog = rememberConfirmDialog(
            onConfirm = {
                APatchKeyHelper.clearConfigKey()
                APApplication.superKey = ""
            }
        )

        val showLanguageDialog = rememberSaveable { mutableStateOf(false) }
        LanguageDialog(showLanguageDialog)

        val showResetSuPathDialog = remember { mutableStateOf(false) }
        if (showResetSuPathDialog.value) {
            ResetSUPathDialog(showResetSuPathDialog)
        }

        val showSELinuxModeDialog = remember { mutableStateOf(false) }
        if (showSELinuxModeDialog.value) {
            SELinuxModeDialog(
                showDialog = showSELinuxModeDialog,
                currentMode = currentSELinuxMode,
                onModeChanged = { newMode -> currentSELinuxMode = newMode }
            )
        }

        val showThemeChooseDialog = remember { mutableStateOf(false) }
        if (showThemeChooseDialog.value) {
            ThemeChooseDialog(showThemeChooseDialog)
        }

        val showIconChooseDialog = remember { mutableStateOf(false) }
        if (showIconChooseDialog.value) {
            IconChooseDialog(showIconChooseDialog)
        }

        val showDesktopAppNameDialog = remember { mutableStateOf(false) }
        if (showDesktopAppNameDialog.value) {
            DesktopAppNameChooseDialog(showDesktopAppNameDialog)
        }

        val showHomeLayoutChooseDialog = remember { mutableStateOf(false) }
        if (showHomeLayoutChooseDialog.value) {
            HomeLayoutChooseDialog(showHomeLayoutChooseDialog)
        }

        val showAppTitleDialog = remember { mutableStateOf(false) }
        if (showAppTitleDialog.value) {
            AppTitleChooseDialog(showAppTitleDialog)
        }

        val showFolkXAnimationTypeDialog = remember { mutableStateOf(false) }
        if (showFolkXAnimationTypeDialog.value) {
            FolkXAnimationTypeDialog(showFolkXAnimationTypeDialog)
        }

        val showFolkXAnimationSpeedDialog = remember { mutableStateOf(false) }
        if (showFolkXAnimationSpeedDialog.value) {
            FolkXAnimationSpeedDialog(showFolkXAnimationSpeedDialog)
        }

        val showAppListLoadingSchemeDialog = remember { mutableStateOf(false) }
        if (showAppListLoadingSchemeDialog.value) {
            AppListLoadingSchemeDialog(showAppListLoadingSchemeDialog)
        }

        var showLogBottomSheet by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val logSavedMessage = stringResource(R.string.log_saved)
        val exportBugreportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/gzip")
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch(Dispatchers.IO) {
                    loadingDialog.show()
                    uri.outputStream().use { output ->
                        getBugreportFile(context).inputStream().use {
                            it.copyTo(output)
                        }
                    }

                    loadingDialog.hide()
                    snackBarHost.showSnackbar(message = logSavedMessage)
                }
            }
        }

        // --- Hoisted State & Launchers ---
        val prefs = APApplication.sharedPreferences

        // General
        var autoUpdateCheck by rememberSaveable { mutableStateOf(prefs.getBoolean("auto_update_check", true)) }
        val showUpdateDialog = remember { mutableStateOf(false) }
        val showDpiDialog = remember { mutableStateOf(false) }

        // Appearance
        val isNightModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        var nightModeFollowSys by rememberSaveable { mutableStateOf(prefs.getBoolean("night_mode_follow_sys", false)) }
        var nightModeEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("night_mode_enabled", true)) }
        
        val isDynamicColorSupport = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        var useSystemDynamicColor by rememberSaveable { mutableStateOf(prefs.getBoolean("use_system_color_theme", false)) }

        val refreshThemeObserver by refreshTheme.observeAsState(false)
        if (refreshThemeObserver) {
            nightModeFollowSys = prefs.getBoolean("night_mode_follow_sys", false)
            nightModeEnabled = prefs.getBoolean("night_mode_enabled", true)
            useSystemDynamicColor = prefs.getBoolean("use_system_color_theme", false)
        }

        // Background Launchers
        var pickingType by remember { mutableStateOf<String?>(null) }
        val pickImageLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                scope.launch {
                    loadingDialog.show()
                    val success = when (pickingType) {
                        "home" -> BackgroundManager.saveAndApplyHomeBackground(context, it)
                        "kernel" -> BackgroundManager.saveAndApplyKernelBackground(context, it)
                        "superuser" -> BackgroundManager.saveAndApplySuperuserBackground(context, it)
                        "system" -> BackgroundManager.saveAndApplySystemModuleBackground(context, it)
                        "settings" -> BackgroundManager.saveAndApplySettingsBackground(context, it)
                        else -> BackgroundManager.saveAndApplyCustomBackground(context, it)
                    }
                    loadingDialog.hide()
                    if (success) {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_custom_background_saved))
                        refreshTheme.value = true
                    } else {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_custom_background_error))
                    }
                    pickingType = null
                }
            }
        }

        val pickVideoLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                scope.launch {
                    loadingDialog.show()
                    val success = BackgroundManager.saveAndApplyVideoBackground(context, it)
                    loadingDialog.hide()
                    if (success) {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_video_selected))
                        refreshTheme.value = true
                    } else {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_custom_background_error))
                    }
                }
            }
        }

        val pickGridImageLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                scope.launch {
                    loadingDialog.show()
                    val success = BackgroundManager.saveAndApplyGridWorkingCardBackground(context, it)
                    loadingDialog.hide()
                    if (success) {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_grid_working_card_background_saved))
                    } else {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_grid_working_card_background_error))
                    }
                }
            }
        }

        // Font Launcher
        val pickFontLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                scope.launch {
                    loadingDialog.show()
                    val success = FontConfig.saveFontFile(context, it)
                    loadingDialog.hide()
                    if (success) {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_custom_font_saved))
                        refreshTheme.value = true
                    } else {
                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_custom_font_error))
                    }
                }
            }
        }

        // Music Launcher
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

        // Sound Effect Launcher
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
                        // No need to reload manager as it plays on demand, but maybe clear cache if we had one
                    } else {
                        snackBarHost.showSnackbar(message = "Failed to save sound effect")
                    }
                }
            }
        }

        // Theme Export/Import
        var pendingExportMetadata by remember { mutableStateOf<ThemeManager.ThemeMetadata?>(null) }
        val showExportDialog = remember { mutableStateOf(false) }

        // Removed exportThemeLauncher as we are now saving directly to a fixed path

        var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
        var pendingImportMetadata by remember { mutableStateOf<ThemeManager.ThemeMetadata?>(null) }
        val showImportDialog = remember { mutableStateOf(false) }
        val importThemeLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    loadingDialog.show()
                    val metadata = ThemeManager.readThemeMetadata(context, uri)
                    loadingDialog.hide()
                    
                    if (metadata != null) {
                        pendingImportUri = uri
                        pendingImportMetadata = metadata
                        showImportDialog.value = true
                    } else {
                        loadingDialog.show()
                        val success = ThemeManager.importTheme(context, uri)
                        loadingDialog.hide()
                        snackBarHost.showSnackbar(
                            message = if (success) context.getString(R.string.settings_theme_imported) else context.getString(R.string.settings_theme_import_failed)
                        )
                    }
                }
            }
        }
        
        val showFilePicker = remember { mutableStateOf(false) }
        if (showFilePicker.value) {
            FilePickerDialog(
                onDismissRequest = { showFilePicker.value = false },
                onFileSelected = { file ->
                    showFilePicker.value = false
                    val uri = Uri.fromFile(file)
                    scope.launch {
                        loadingDialog.show()
                        val metadata = ThemeManager.readThemeMetadata(context, uri)
                        loadingDialog.hide()
                        
                        if (metadata != null) {
                            pendingImportUri = uri
                            pendingImportMetadata = metadata
                            showImportDialog.value = true
                        } else {
                            loadingDialog.show()
                            val success = ThemeManager.importTheme(context, uri)
                            loadingDialog.hide()
                            snackBarHost.showSnackbar(
                                message = if (success) context.getString(R.string.settings_theme_imported) else context.getString(R.string.settings_theme_import_failed)
                            )
                        }
                    }
                }
            )
        }

        // Behavior
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
        
        var biometricLogin by rememberSaveable { mutableStateOf(prefs.getBoolean("biometric_login", false)) }
        var enableWebDebugging by rememberSaveable { mutableStateOf(prefs.getBoolean("enable_web_debugging", false)) }
        var showMoreModuleInfo by rememberSaveable { mutableStateOf(prefs.getBoolean("show_more_module_info", true)) }
        var installConfirm by rememberSaveable { mutableStateOf(prefs.getBoolean("apm_install_confirm_enabled", true)) }
        var showDisableAllModules by rememberSaveable { mutableStateOf(prefs.getBoolean("show_disable_all_modules", false)) }
        var stayOnPage by rememberSaveable { mutableStateOf(prefs.getBoolean("apm_action_stay_on_page", true)) }
        var hideApatchCard by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_apatch_card", false)) }
        var hideSuPath by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_su_path", false)) }
        var hideKpatchVersion by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_kpatch_version", false)) }
        var hideFingerprint by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_fingerprint", false)) }
        // Individual badge count settings - default enabled
        var enableSuperUserBadge by rememberSaveable { mutableStateOf(prefs.getBoolean("badge_superuser", true)) }
        var enableApmBadge by rememberSaveable { mutableStateOf(prefs.getBoolean("badge_apm", true)) }
        var enableKernelBadge by rememberSaveable { mutableStateOf(prefs.getBoolean("badge_kernel", true)) }
        var folkXEngineEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("folkx_engine_enabled", true)) }

        // Module
        var autoBackupModule by rememberSaveable { mutableStateOf(prefs.getBoolean("auto_backup_module", false)) }


        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            
            // General Category
            val generalTitle = stringResource(R.string.settings_category_general)
            val matchGeneral = shouldShow(generalTitle)
            
            val languageTitle = stringResource(id = R.string.settings_app_language)
            val languageValue = AppCompatDelegate.getApplicationLocales()[0]?.displayLanguage?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            } ?: stringResource(id = R.string.system_default)
            val showLanguage = matchGeneral || shouldShow(languageTitle, languageValue)

            val updateTitle = stringResource(id = R.string.settings_check_update)
            val showUpdate = matchGeneral || shouldShow(updateTitle)

            val autoUpdateTitle = stringResource(id = R.string.settings_auto_update_check)
            val autoUpdateSummary = stringResource(id = R.string.settings_auto_update_check_summary)
            val showAutoUpdate = matchGeneral || shouldShow(autoUpdateTitle, autoUpdateSummary)

            val globalNamespaceTitle = stringResource(id = R.string.settings_global_namespace_mode)
            val globalNamespaceSummary = stringResource(id = R.string.settings_global_namespace_mode_summary)
            val showGlobalNamespace = (kPatchReady && aPatchReady) && (matchGeneral || shouldShow(globalNamespaceTitle, globalNamespaceSummary))

            val magicMountTitle = stringResource(id = R.string.settings_magic_mount)
            val magicMountSummary = stringResource(id = R.string.settings_magic_mount_summary)
            val showMagicMount = (kPatchReady && aPatchReady) && (matchGeneral || shouldShow(magicMountTitle, magicMountSummary))

            val overlayFSModeTitle = stringResource(id = R.string.settings_overlayfs_mode)
            val overlayFSModeSummary = stringResource(id = R.string.settings_overlayfs_mode_summary)
            val showOverlayFSMode = (kPatchReady && aPatchReady && isMagicMountEnabled) && (matchGeneral || shouldShow(overlayFSModeTitle, overlayFSModeSummary))

            val selinuxModeTitle = stringResource(id = R.string.settings_selinux_mode)
            val selinuxModeSummary = stringResource(id = R.string.settings_selinux_mode_summary)
            val selinuxModeValue = when (currentSELinuxMode) {
                "Enforcing" -> stringResource(R.string.settings_selinux_mode_enforcing)
                "Permissive" -> stringResource(R.string.settings_selinux_mode_permissive)
                else -> stringResource(R.string.home_selinux_status_unknown)
            }
            val showSELinuxMode = (kPatchReady && aPatchReady) && (matchGeneral || shouldShow(selinuxModeTitle, selinuxModeValue))

            val resetSuPathTitle = stringResource(id = R.string.setting_reset_su_path)
            val showResetSuPath = kPatchReady && (matchGeneral || shouldShow(resetSuPathTitle))

            val appTitleTitle = stringResource(id = R.string.settings_app_title)
            val currentAppTitleRaw = prefs.getString("app_title", "folkpatch")
            val currentAppTitle = stringResource(appTitleNameToString(currentAppTitleRaw.toString()))
            val showAppTitle = matchGeneral || shouldShow(appTitleTitle, currentAppTitle)

            val launcherIconTitle = stringResource(id = R.string.settings_launcher_icon)
            val currentIconRaw = prefs.getString("launcher_icon_variant", "classic")
            val currentIcon = stringResource(iconNameToString(currentIconRaw.toString()))
            val showLauncherIcon = matchGeneral || shouldShow(launcherIconTitle, currentIcon)

            val desktopAppNameTitle = stringResource(id = R.string.desktop_app_name)
            val currentDesktopAppName = prefs.getString("desktop_app_name", "FolkPatch")
            val showDesktopAppName = matchGeneral || shouldShow(desktopAppNameTitle, currentDesktopAppName.toString())

            val dpiTitle = stringResource(id = R.string.settings_app_dpi)
            val currentDpiVal = me.bmax.apatch.util.DPIUtils.currentDpi
            val dpiValue = if (currentDpiVal == -1) stringResource(id = R.string.system_default) else "$currentDpiVal DPI"
            val showDpi = matchGeneral || shouldShow(dpiTitle, dpiValue)

            val logTitle = stringResource(id = R.string.send_log)
            val showLog = matchGeneral || shouldShow(logTitle)

            val folkXEngineTitle = stringResource(id = R.string.settings_folkx_engine_title)
            val folkXEngineSummary = stringResource(id = R.string.settings_folkx_engine_summary)
            val showFolkXEngine = matchGeneral || shouldShow(folkXEngineTitle, folkXEngineSummary)

            val appListLoadingSchemeTitle = stringResource(id = R.string.settings_app_list_loading_scheme)
            val currentScheme = prefs.getString("app_list_loading_scheme", "root_service")
            val currentSchemeLabel = if (currentScheme == "root_service") stringResource(R.string.app_list_loading_scheme_root_service) else stringResource(R.string.app_list_loading_scheme_package_manager)
            val showAppListLoadingScheme = matchGeneral || shouldShow(appListLoadingSchemeTitle, currentSchemeLabel)

            val showGeneralCategory = showLanguage || showUpdate || showAutoUpdate || showGlobalNamespace || showMagicMount || showOverlayFSMode || showResetSuPath || showAppTitle || showLauncherIcon || showDesktopAppName || showDpi || showLog || showFolkXEngine || showAppListLoadingScheme || showSELinuxMode

            if (showGeneralCategory) {
                SettingsCategory(icon = Icons.Filled.Tune, title = generalTitle, isSearching = searchText.isNotEmpty()) {
                    // Language
                    if (showLanguage) {
                        ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                            Text(text = languageTitle)
                        }, modifier = Modifier.clickable {
                            showLanguageDialog.value = true
                        }, supportingContent = {
                            Text(text = languageValue,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline)
                        }, leadingContent = { Icon(Icons.Filled.Translate, null) })
                    }

                  

                    // Check Update
                    if (showUpdate) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(updateTitle) },
                            modifier = Modifier.clickable {
                                scope.launch {
                                    loadingDialog.show()
                                    val hasUpdate = UpdateChecker.checkUpdate()
                                    loadingDialog.hide()
                                    if (hasUpdate) {
                                        showUpdateDialog.value = true
                                    } else {
                                        Toast.makeText(context, R.string.update_latest, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            leadingContent = { Icon(Icons.Filled.Refresh, null) }
                        )
                    }

                    // Auto Update Check
                    if (showAutoUpdate) {
                        SwitchItem(
                            icon = Icons.Filled.Update,
                            title = autoUpdateTitle,
                            summary = autoUpdateSummary,
                            checked = autoUpdateCheck,
                            onCheckedChange = {
                                prefs.edit { putBoolean("auto_update_check", it) }
                                autoUpdateCheck = it
                            })
                    }

                      // FolkX Engine
                    if (showFolkXEngine) {
                        SwitchItem(
                            icon = Icons.Filled.AutoAwesome,
                            title = folkXEngineTitle,
                            summary = folkXEngineSummary,
                            checked = folkXEngineEnabled
                        ) {
                            prefs.edit { putBoolean("folkx_engine_enabled", it) }
                            folkXEngineEnabled = it
                        }
                    }

                    AnimatedVisibility(
                        visible = folkXEngineEnabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(stringResource(R.string.settings_folkx_animation_type)) },
                                supportingContent = {
                                    val currentType = prefs.getString("folkx_animation_type", "linear")
                                    Text(
                                        text = when (currentType) {
                                            "spatial" -> stringResource(R.string.settings_folkx_animation_spatial)
                                            "fade" -> stringResource(R.string.settings_folkx_animation_fade)
                                            "vertical" -> stringResource(R.string.settings_folkx_animation_vertical)
                                            "diagonal" -> stringResource(R.string.settings_folkx_animation_diagonal)
                                            else -> stringResource(R.string.settings_folkx_animation_linear)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                },
                                leadingContent = { Icon(Icons.Filled.Animation, null) },
                                modifier = Modifier.clickable { showFolkXAnimationTypeDialog.value = true }
                            )

                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(stringResource(R.string.settings_folkx_animation_speed)) },
                                supportingContent = {
                                    val currentSpeed = prefs.getFloat("folkx_animation_speed", 1.0f)
                                    Text(
                                        text = "${currentSpeed}x",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                },
                                leadingContent = { Icon(Icons.Filled.Speed, null) },
                                modifier = Modifier.clickable { showFolkXAnimationSpeedDialog.value = true }
                            )
                        }
                    }

                    // App List Loading Scheme
                    if (showAppListLoadingScheme) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(appListLoadingSchemeTitle) },
                            modifier = Modifier.clickable {
                                showAppListLoadingSchemeDialog.value = true
                            },
                            supportingContent = {
                                Text(
                                    text = currentSchemeLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            leadingContent = { Icon(Icons.Filled.FilterList, null) }
                        )
                    }

                    // SELinux Mode
                    if (showSELinuxMode) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(selinuxModeTitle) },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.settings_selinux_current_mode, selinuxModeValue),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            leadingContent = { Icon(Icons.Filled.Lock, null) },
                            modifier = Modifier.clickable { showSELinuxModeDialog.value = true }
                        )
                    }

                    // Global Namespace
                    if (showGlobalNamespace) {
                        SwitchItem(
                            icon = Icons.Filled.Engineering,
                            title = globalNamespaceTitle,
                            summary = globalNamespaceSummary,
                            checked = isGlobalNamespaceEnabled,
                            onCheckedChange = {
                                setGlobalNamespaceEnabled(if (isGlobalNamespaceEnabled) "0" else "1")
                                isGlobalNamespaceEnabled = it
                            })
                    }

                    // Magic Mount
                    if (showMagicMount) {
                        SwitchItem(
                            icon = Icons.Filled.Extension,
                            title = magicMountTitle,
                            summary = magicMountSummary,
                            checked = isMagicMountEnabled,
                            onCheckedChange = {
                                setMagicMountEnabled(it)
                                isMagicMountEnabled = it
                            })
                    }

                    // OverlayFS Mode
                    if (showOverlayFSMode) {
                        SwitchItem(
                            icon = Icons.Filled.FilterList,
                            title = overlayFSModeTitle,
                            summary = overlayFSModeSummary,
                            checked = isOverlayFSModeEnabled,
                            onCheckedChange = {
                                setOverlayFSModeEnabled(it)
                                isOverlayFSModeEnabled = it
                            })
                    }

                    // Reset SU Path
                    if (showResetSuPath) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = { Icon(Icons.Filled.Commit, resetSuPathTitle) },
                            supportingContent = {},
                            headlineContent = { Text(resetSuPathTitle) },
                            modifier = Modifier.clickable { showResetSuPathDialog.value = true })
                    }

                    // App Title
                    if (showAppTitle) {
                        ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                            Text(text = appTitleTitle)
                        }, modifier = Modifier.clickable {
                            showAppTitleDialog.value = true
                        }, supportingContent = {
                            Text(
                                text = currentAppTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }, leadingContent = { Icon(Icons.Filled.Label, null) })
                    }

                    // Launcher Icon
                    if (showLauncherIcon) {
                        ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                            Text(text = launcherIconTitle)
                        }, modifier = Modifier.clickable {
                            showIconChooseDialog.value = true
                        }, supportingContent = {
                            Text(
                                text = currentIcon,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }, leadingContent = { Icon(painterResource(id = R.drawable.settings), null) })
                    }

                    // Desktop App Name
                    if (showDesktopAppName) {
                        ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                            Text(text = desktopAppNameTitle)
                        }, modifier = Modifier.clickable {
                            showDesktopAppNameDialog.value = true
                        }, supportingContent = {
                            Text(
                                text = currentDesktopAppName.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }, leadingContent = { Icon(Icons.Filled.TextFields, null) })
                    }

                    // DPI
                    if (showDpi) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(dpiTitle) },
                            modifier = Modifier.clickable {
                                showDpiDialog.value = true
                            },
                            supportingContent = {
                                Text(
                                    text = dpiValue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            leadingContent = { Icon(Icons.Filled.AspectRatio, null) }
                        )
                    }
                    
                    // Log
                    if (showLog) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = { Icon(Icons.Filled.BugReport, logTitle) },
                            headlineContent = { Text(logTitle) },
                            modifier = Modifier.clickable { showLogBottomSheet = true }
                        )
                    }
                }
            }

            // Appearance Category
            val appearanceTitle = stringResource(R.string.settings_category_appearance)
            val matchAppearance = shouldShow(appearanceTitle)

            // Night Mode
            val nightModeFollowSysTitle = stringResource(id = R.string.settings_night_mode_follow_sys)
            val nightModeFollowSysSummary = stringResource(id = R.string.settings_night_mode_follow_sys_summary)
            val showNightModeFollowSys = isNightModeSupported && (matchAppearance || shouldShow(nightModeFollowSysTitle, nightModeFollowSysSummary))

            val nightModeEnabledTitle = stringResource(id = R.string.settings_night_theme_enabled)
            val showNightModeEnabled = isNightModeSupported && !nightModeFollowSys && (matchAppearance || shouldShow(nightModeEnabledTitle))

            // Theme Color
            val useSystemColorTitle = stringResource(id = R.string.settings_use_system_color_theme)
            val useSystemColorSummary = stringResource(id = R.string.settings_use_system_color_theme_summary)
            val showUseSystemColor = isDynamicColorSupport && (matchAppearance || shouldShow(useSystemColorTitle, useSystemColorSummary))

            val customColorTitle = stringResource(id = R.string.settings_custom_color_theme)
            val colorMode = prefs.getString("custom_color", "light_blue")
            val customColorValue = stringResource(colorNameToString(colorMode.toString()))
            val showCustomColor = (!isDynamicColorSupport || !useSystemDynamicColor) && (matchAppearance || shouldShow(customColorTitle, customColorValue))

            // Home Layout
            val homeLayoutTitle = stringResource(id = R.string.settings_home_layout_style)
            val currentStyle = prefs.getString("home_layout_style", "focus")
            val homeLayoutValue = stringResource(homeLayoutStyleToString(currentStyle.toString()))
            val showHomeLayout = matchAppearance || shouldShow(homeLayoutTitle, homeLayoutValue)

            // Navigation Layout Settings
            val navLayoutTitle = stringResource(id = R.string.settings_nav_layout_title)
            val navLayoutSummary = stringResource(id = R.string.settings_nav_layout_summary)
            val showNavApmTitle = stringResource(id = R.string.settings_show_apm)
            val showNavKpmTitle = stringResource(id = R.string.settings_show_kpm)
            val showNavSuperUserTitle = stringResource(id = R.string.settings_show_superuser)

            val showNavLayout = matchAppearance || shouldShow(navLayoutTitle, navLayoutSummary)

            var showNavApm by rememberSaveable { mutableStateOf(prefs.getBoolean("show_nav_apm", true)) }
            var showNavKpm by rememberSaveable { mutableStateOf(prefs.getBoolean("show_nav_kpm", true)) }
            var showNavSuperUser by rememberSaveable { mutableStateOf(prefs.getBoolean("show_nav_superuser", true)) }

            // Grid Layout Background
            val isKernelSuStyle = prefs.getString("home_layout_style", "focus") == "kernelsu"
            val gridBackgroundTitle = stringResource(id = R.string.settings_grid_working_card_background)
            val gridBackgroundSummary = stringResource(id = R.string.settings_grid_working_card_background_summary)
            val gridBackgroundEnabledText = stringResource(id = R.string.settings_grid_working_card_background_enabled)
            val gridSelectImageText = stringResource(id = R.string.settings_select_background_image)
            
            val showGridBackgroundSwitch = isKernelSuStyle && (matchAppearance || shouldShow(gridBackgroundTitle, gridBackgroundSummary, gridBackgroundEnabledText, gridSelectImageText))
            
            val gridOpacityTitle = stringResource(id = R.string.settings_custom_background_opacity)
            val showGridOpacity = isKernelSuStyle && BackgroundConfig.isGridWorkingCardBackgroundEnabled && (matchAppearance || shouldShow(gridOpacityTitle))
            val gridDualOpacityTitle = stringResource(id = R.string.settings_grid_working_card_dual_opacity)
            val gridDayOpacityTitle = stringResource(id = R.string.settings_grid_working_card_day_opacity)
            val gridNightOpacityTitle = stringResource(id = R.string.settings_grid_working_card_night_opacity)
            val showGridDualOpacitySwitch = isKernelSuStyle && BackgroundConfig.isGridWorkingCardBackgroundEnabled && (matchAppearance || shouldShow(gridDualOpacityTitle))
            val showGridDayOpacity = isKernelSuStyle && BackgroundConfig.isGridWorkingCardBackgroundEnabled && BackgroundConfig.isGridDualOpacityEnabled && (matchAppearance || shouldShow(gridDayOpacityTitle))
            val showGridNightOpacity = isKernelSuStyle && BackgroundConfig.isGridWorkingCardBackgroundEnabled && BackgroundConfig.isGridDualOpacityEnabled && (matchAppearance || shouldShow(gridNightOpacityTitle))
            
            val gridDimTitle = stringResource(id = R.string.settings_custom_background_dim)
            val showGridDim = isKernelSuStyle && BackgroundConfig.isGridWorkingCardBackgroundEnabled && (matchAppearance || shouldShow(gridDimTitle))
            
            val gridSelectTitle = stringResource(id = R.string.settings_select_background_image)
            val gridSelectedText = stringResource(id = R.string.settings_grid_working_card_background_selected)
            val showGridPicker = isKernelSuStyle && BackgroundConfig.isGridWorkingCardBackgroundEnabled && (matchAppearance || shouldShow(gridSelectTitle, gridSelectedText))
            
            val gridClearTitle = stringResource(id = R.string.settings_clear_grid_working_card_background)
            val showGridClear = isKernelSuStyle && BackgroundConfig.isGridWorkingCardBackgroundEnabled && (matchAppearance || shouldShow(gridClearTitle))

            val gridCheckHiddenTitle = stringResource(id = R.string.settings_grid_working_card_hide_check)
            val gridCheckHiddenSummary = stringResource(id = R.string.settings_grid_working_card_hide_check_summary)
            val showGridCheckHidden = isKernelSuStyle && (matchAppearance || shouldShow(gridCheckHiddenTitle, gridCheckHiddenSummary))

            val gridTextHiddenTitle = stringResource(id = R.string.settings_grid_working_card_hide_text)
            val gridTextHiddenSummary = stringResource(id = R.string.settings_grid_working_card_hide_text_summary)
            val showGridTextHidden = isKernelSuStyle && (matchAppearance || shouldShow(gridTextHiddenTitle, gridTextHiddenSummary))

            val gridModeHiddenTitle = stringResource(id = R.string.settings_grid_working_card_hide_mode)
            val gridModeHiddenSummary = stringResource(id = R.string.settings_grid_working_card_hide_mode_summary)
            val showGridModeHidden = isKernelSuStyle && (matchAppearance || shouldShow(gridModeHiddenTitle, gridModeHiddenSummary))

            // List Layout Customization
            val isListStyle = currentStyle != "kernelsu" && currentStyle != "focus"
            val listModeHiddenTitle = stringResource(id = R.string.settings_list_working_card_hide_mode)
            val listModeHiddenSummary = stringResource(id = R.string.settings_list_working_card_hide_mode_summary)
            val showListModeHidden = isListStyle && (matchAppearance || shouldShow(listModeHiddenTitle, listModeHiddenSummary))

            // Custom Background (Single/Multi)
            val customBackgroundTitle = stringResource(id = R.string.settings_custom_background)
            val customBackgroundSummary = stringResource(id = R.string.settings_custom_background_summary)
            val customBackgroundEnabledText = stringResource(id = R.string.settings_custom_background_enabled)
            val showCustomBackgroundSwitch = matchAppearance || shouldShow(customBackgroundTitle, customBackgroundSummary, customBackgroundEnabledText)
            
            val customOpacityTitle = stringResource(id = R.string.settings_custom_background_opacity)
            val showCustomOpacity = BackgroundConfig.isCustomBackgroundEnabled && (matchAppearance || shouldShow(customOpacityTitle))
            
            val customBlurTitle = stringResource(id = R.string.settings_custom_background_blur)
            // Blur is only supported on Android 12+ (API 31+)
            val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val showCustomBlur = BackgroundConfig.isCustomBackgroundEnabled && isBlurSupported && (matchAppearance || shouldShow(customBlurTitle))

            val customDimTitle = stringResource(id = R.string.settings_custom_background_dim)
            val showCustomDim = BackgroundConfig.isCustomBackgroundEnabled && !BackgroundConfig.isDualBackgroundDimEnabled && (matchAppearance || shouldShow(customDimTitle))

            val customDualDimTitle = stringResource(id = R.string.settings_custom_background_dual_dim)
            val customDayDimTitle = stringResource(id = R.string.settings_custom_background_day_dim)
            val customNightDimTitle = stringResource(id = R.string.settings_custom_background_night_dim)
            val showCustomDualDimSwitch = BackgroundConfig.isCustomBackgroundEnabled && (matchAppearance || shouldShow(customDualDimTitle))
            val showCustomDayDim = BackgroundConfig.isCustomBackgroundEnabled && BackgroundConfig.isDualBackgroundDimEnabled && (matchAppearance || shouldShow(customDayDimTitle))
            val showCustomNightDim = BackgroundConfig.isCustomBackgroundEnabled && BackgroundConfig.isDualBackgroundDimEnabled && (matchAppearance || shouldShow(customNightDimTitle))
            
            val videoBackgroundTitle = stringResource(id = R.string.settings_video_background)
            val videoBackgroundSummary = stringResource(id = R.string.settings_video_background_summary)
            val showVideoBackgroundSwitch = BackgroundConfig.isCustomBackgroundEnabled && (matchAppearance || shouldShow(videoBackgroundTitle, videoBackgroundSummary))
            
            val videoSelectTitle = stringResource(id = R.string.settings_select_video)
            val videoSelectedText = stringResource(id = R.string.settings_video_selected)
            val showVideoPicker = BackgroundConfig.isCustomBackgroundEnabled && BackgroundConfig.isVideoBackgroundEnabled && (matchAppearance || shouldShow(videoSelectTitle, videoSelectedText))
            
            val videoVolumeTitle = stringResource(id = R.string.settings_video_volume)
            val showVideoVolume = BackgroundConfig.isCustomBackgroundEnabled && BackgroundConfig.isVideoBackgroundEnabled && (matchAppearance || shouldShow(videoVolumeTitle))
            
            val videoClearTitle = stringResource(id = R.string.settings_clear_video_background)
            val showVideoClear = BackgroundConfig.isCustomBackgroundEnabled && BackgroundConfig.isVideoBackgroundEnabled && !BackgroundConfig.videoBackgroundUri.isNullOrEmpty() && (matchAppearance || shouldShow(videoClearTitle))
            
            val multiBackgroundTitle = stringResource(id = R.string.settings_multi_background_mode)
            val multiBackgroundSummary = stringResource(id = R.string.settings_multi_background_mode_summary)
            val showMultiBackgroundSwitch = BackgroundConfig.isCustomBackgroundEnabled && !BackgroundConfig.isVideoBackgroundEnabled && (matchAppearance || shouldShow(multiBackgroundTitle, multiBackgroundSummary))
            
            // Single Background Selector
            val singleSelectTitle = stringResource(id = R.string.settings_select_background_image)
            val singleSelectedText = stringResource(id = R.string.settings_background_selected)
            val showSinglePicker = BackgroundConfig.isCustomBackgroundEnabled && !BackgroundConfig.isVideoBackgroundEnabled && !BackgroundConfig.isMultiBackgroundEnabled && (matchAppearance || shouldShow(singleSelectTitle, singleSelectedText))
            
            val singleClearTitle = stringResource(id = R.string.settings_clear_background)
            val showSingleClear = BackgroundConfig.isCustomBackgroundEnabled && !BackgroundConfig.isVideoBackgroundEnabled && !BackgroundConfig.isMultiBackgroundEnabled && !BackgroundConfig.customBackgroundUri.isNullOrEmpty() && (matchAppearance || shouldShow(singleClearTitle))

            // Custom Font
            val customFontTitle = stringResource(id = R.string.settings_custom_font)
            val customFontSummary = stringResource(id = R.string.settings_custom_font_summary)
            val customFontEnabledText = stringResource(id = R.string.settings_custom_font_enabled)
            val customFontSelectedText = stringResource(id = R.string.settings_font_selected)
            val showCustomFontSwitch = matchAppearance || shouldShow(customFontTitle, customFontSummary, customFontEnabledText, customFontSelectedText)
            
            val selectFontTitle = stringResource(id = R.string.settings_select_font_file)
            val showSelectFont = FontConfig.isCustomFontEnabled && (matchAppearance || shouldShow(selectFontTitle))
            
            val clearFontTitle = stringResource(id = R.string.settings_clear_font)
            val showClearFont = FontConfig.isCustomFontEnabled && FontConfig.customFontFilename != null && (matchAppearance || shouldShow(clearFontTitle))

            // Theme Store
            val themeStoreTitle = stringResource(id = R.string.theme_store_title)
            val showThemeStore = matchAppearance || shouldShow(themeStoreTitle)
            
            val saveThemeTitle = stringResource(id = R.string.settings_save_theme)
            val showSaveTheme = matchAppearance || shouldShow(saveThemeTitle)
            
            val importThemeTitle = stringResource(id = R.string.settings_import_theme)
            val showImportTheme = matchAppearance || shouldShow(importThemeTitle)

            val showAppearanceCategory = showNightModeFollowSys || showNightModeEnabled || showUseSystemColor || showCustomColor || showHomeLayout || showGridBackgroundSwitch || showGridCheckHidden || showGridTextHidden || showGridModeHidden || showListModeHidden || showCustomBackgroundSwitch || showCustomFontSwitch || showThemeStore || showSaveTheme || showImportTheme

            if (showAppearanceCategory) {
                SettingsCategory(icon = Icons.Filled.Palette, title = appearanceTitle, isSearching = searchText.isNotEmpty()) {
                    // Night Mode
                    if (showNightModeFollowSys) {
                        SwitchItem(
                            icon = Icons.Filled.DarkMode,
                            title = nightModeFollowSysTitle,
                            summary = nightModeFollowSysSummary,
                            checked = nightModeFollowSys
                        ) {
                            prefs.edit { putBoolean("night_mode_follow_sys", it) }
                            nightModeFollowSys = it
                            refreshTheme.value = true
                        }
                    }

                    if (showNightModeEnabled) {
                        SwitchItem(
                            icon = if (nightModeEnabled) Icons.Filled.DarkMode else Icons.Filled.VisibilityOff,
                            title = nightModeEnabledTitle,
                            summary = null,
                            checked = nightModeEnabled
                        ) {
                            prefs.edit { putBoolean("night_mode_enabled", it) }
                            nightModeEnabled = it
                            refreshTheme.value = true
                        }
                    }

                    // Theme Color
                    if (showUseSystemColor) {
                        SwitchItem(
                            icon = Icons.Filled.ColorLens,
                            title = useSystemColorTitle,
                            summary = useSystemColorSummary,
                            checked = useSystemDynamicColor
                        ) {
                            prefs.edit { putBoolean("use_system_color_theme", it) }
                            useSystemDynamicColor = it
                            refreshTheme.value = true
                        }
                    }

                    if (showCustomColor) {
                        ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                            Text(text = customColorTitle)
                        }, modifier = Modifier.clickable {
                            showThemeChooseDialog.value = true
                        }, supportingContent = {
                            Text(
                                text = customColorValue,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }, leadingContent = { Icon(Icons.Filled.FormatColorFill, null) })
                    }

                    // Home Layout Style
                    if (showHomeLayout) {
                        ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                            Text(text = homeLayoutTitle)
                        }, modifier = Modifier.clickable {
                            showHomeLayoutChooseDialog.value = true
                        }, supportingContent = {
                            Text(
                                text = homeLayoutValue,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }, leadingContent = { Icon(Icons.Filled.Dashboard, null) })
                    }

                    // Navigation Layout Settings (New)
                    if (showNavLayout) {
                        SettingsCategory(
                            icon = Icons.Filled.ViewQuilt,
                            title = navLayoutTitle,
                            summary = navLayoutSummary,
                            isSearching = searchText.isNotEmpty()
                        ) {
                            CheckboxItem(
                                icon = Icons.Filled.Extension,
                                title = showNavApmTitle,
                                summary = null,
                                checked = showNavApm
                            ) {
                                showNavApm = it
                                prefs.edit().putBoolean("show_nav_apm", it).apply()
                            }

                            CheckboxItem(
                                icon = Icons.Filled.Archive,
                                title = showNavKpmTitle,
                                summary = null,
                                checked = showNavKpm
                            ) {
                                showNavKpm = it
                                prefs.edit().putBoolean("show_nav_kpm", it).apply()
                            }

                            CheckboxItem(
                                icon = Icons.Filled.Security,
                                title = showNavSuperUserTitle,
                                summary = null,
                                checked = showNavSuperUser
                            ) {
                                showNavSuperUser = it
                                prefs.edit().putBoolean("show_nav_superuser", it).apply()
                            }
                        }
                    }

                    // Grid Layout Background
                    if (showGridBackgroundSwitch) {
                        SwitchItem(
                            icon = Icons.Filled.Image,
                            title = gridBackgroundTitle,
                            summary = if (BackgroundConfig.isGridWorkingCardBackgroundEnabled) {
                                if (!BackgroundConfig.gridWorkingCardBackgroundUri.isNullOrEmpty()) {
                                    gridBackgroundEnabledText
                                } else {
                                    gridSelectImageText
                                }
                            } else {
                                gridBackgroundSummary
                            },
                            checked = BackgroundConfig.isGridWorkingCardBackgroundEnabled,
                            onCheckedChange = {
                                BackgroundConfig.setGridWorkingCardBackgroundEnabledState(it)
                                BackgroundConfig.save(context)
                            }
                        )
                    }

                    if (BackgroundConfig.isGridWorkingCardBackgroundEnabled && isKernelSuStyle) {
                        if (showGridDualOpacitySwitch) {
                            SwitchItem(
                                icon = Icons.Filled.DarkMode,
                                title = gridDualOpacityTitle,
                                summary = null,
                                checked = BackgroundConfig.isGridDualOpacityEnabled,
                                onCheckedChange = {
                                    BackgroundConfig.setGridDualOpacityEnabledState(it)
                                    BackgroundConfig.save(context)
                                }
                            )
                        }
                        // Opacity
                        if (!BackgroundConfig.isGridDualOpacityEnabled && showGridOpacity) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(gridOpacityTitle) },
                                supportingContent = {
                                    androidx.compose.material3.Slider(
                                        value = BackgroundConfig.gridWorkingCardBackgroundOpacity,
                                        onValueChange = { BackgroundConfig.setGridWorkingCardBackgroundOpacityValue(it) },
                                        onValueChangeFinished = { BackgroundConfig.save(context) },
                                        valueRange = 0f..1f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        )
                                    )
                                }
                            )
                        } else {
                            if (BackgroundConfig.isGridDualOpacityEnabled && showGridDayOpacity) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(gridDayOpacityTitle) },
                                    supportingContent = {
                                        androidx.compose.material3.Slider(
                                            value = BackgroundConfig.gridWorkingCardBackgroundDayOpacity,
                                            onValueChange = { BackgroundConfig.setGridWorkingCardBackgroundDayOpacityValue(it) },
                                            onValueChangeFinished = { BackgroundConfig.save(context) },
                                            valueRange = 0f..1f,
                                            colors = androidx.compose.material3.SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                            )
                                        )
                                    }
                                )
                            }
                            if (BackgroundConfig.isGridDualOpacityEnabled && showGridNightOpacity) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(gridNightOpacityTitle) },
                                    supportingContent = {
                                        androidx.compose.material3.Slider(
                                            value = BackgroundConfig.gridWorkingCardBackgroundNightOpacity,
                                            onValueChange = { BackgroundConfig.setGridWorkingCardBackgroundNightOpacityValue(it) },
                                            onValueChangeFinished = { BackgroundConfig.save(context) },
                                            valueRange = 0f..1f,
                                            colors = androidx.compose.material3.SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                            )
                                        )
                                    }
                                )
                            }
                        }
                        // Dim
                        if (showGridDim) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(gridDimTitle) },
                                supportingContent = {
                                    androidx.compose.material3.Slider(
                                        value = BackgroundConfig.gridWorkingCardBackgroundDim,
                                        onValueChange = { BackgroundConfig.setGridWorkingCardBackgroundDimValue(it) },
                                        onValueChangeFinished = { BackgroundConfig.save(context) },
                                        valueRange = 0f..1f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        )
                                    )
                                }
                            )
                        }
                        // Picker
                         if (showGridPicker) {
                             ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(text = gridSelectTitle) },
                                supportingContent = {
                                    if (!BackgroundConfig.gridWorkingCardBackgroundUri.isNullOrEmpty()) {
                                        Text(
                                            text = gridSelectedText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                },
                                leadingContent = { Icon(painterResource(id = R.drawable.ic_custom_background), null) },
                                modifier = Modifier.clickable {
                                    if (PermissionUtils.hasExternalStoragePermission(context)) {
                                        try {
                                            pickGridImageLauncher.launch("image/*")
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "请先授予存储权限才能选择背景图片", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        // Clear
                        val clearGridBackgroundDialog = rememberConfirmDialog(
                            onConfirm = {
                                scope.launch {
                                    loadingDialog.show()
                                    BackgroundManager.clearGridWorkingCardBackground(context)
                                    loadingDialog.hide()
                                    snackBarHost.showSnackbar(message = context.getString(R.string.settings_grid_working_card_background_cleared))
                                }
                            }
                        )
                        if (showGridClear) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(text = gridClearTitle) },
                                leadingContent = { Icon(painterResource(id = R.drawable.ic_clear_background), null) },
                                modifier = Modifier.clickable {
                                    clearGridBackgroundDialog.showConfirm(
                                        title = context.getString(R.string.settings_clear_grid_working_card_background),
                                        content = context.getString(R.string.settings_clear_grid_working_card_background_confirm),
                                        markdown = false
                                    )
                                }
                            )
                        }
                    }

                    if (isKernelSuStyle) {
                        // Hide Checkmark
                         if (showGridCheckHidden) {
                             SwitchItem(
                                icon = Icons.Filled.VisibilityOff,
                                title = gridCheckHiddenTitle,
                                summary = gridCheckHiddenSummary,
                                checked = BackgroundConfig.isGridWorkingCardCheckHidden,
                                onCheckedChange = {
                                    BackgroundConfig.setGridWorkingCardCheckHiddenState(it)
                                    BackgroundConfig.save(context)
                                }
                            )
                        }

                        // Hide Text
                         if (showGridTextHidden) {
                             SwitchItem(
                                icon = Icons.Filled.VisibilityOff,
                                title = gridTextHiddenTitle,
                                summary = gridTextHiddenSummary,
                                checked = BackgroundConfig.isGridWorkingCardTextHidden,
                                onCheckedChange = {
                                    BackgroundConfig.setGridWorkingCardTextHiddenState(it)
                                    BackgroundConfig.save(context)
                                }
                            )
                        }

                        // Hide Mode
                         if (showGridModeHidden) {
                             SwitchItem(
                                icon = Icons.Filled.VisibilityOff,
                                title = gridModeHiddenTitle,
                                summary = gridModeHiddenSummary,
                                checked = BackgroundConfig.isGridWorkingCardModeHidden,
                                onCheckedChange = {
                                    BackgroundConfig.setGridWorkingCardModeHiddenState(it)
                                    BackgroundConfig.save(context)
                                }
                            )
                        }
                    }

                    if (isListStyle) {
                         if (showListModeHidden) {
                             SwitchItem(
                                icon = Icons.Filled.VisibilityOff,
                                title = listModeHiddenTitle,
                                summary = listModeHiddenSummary,
                                checked = BackgroundConfig.isListWorkingCardModeHidden,
                                onCheckedChange = {
                                    BackgroundConfig.setListWorkingCardModeHiddenState(it)
                                    BackgroundConfig.save(context)
                                }
                            )
                        }
                    }

                    // Custom Background (Single/Multi)
                    if (showCustomBackgroundSwitch) {
                        SwitchItem(
                            icon = Icons.Filled.FormatColorFill,
                            title = customBackgroundTitle,
                            summary = if (BackgroundConfig.isCustomBackgroundEnabled) {
                                if (!BackgroundConfig.customBackgroundUri.isNullOrEmpty()) {
                                    customBackgroundEnabledText
                                } else {
                                    gridSelectImageText
                                }
                            } else {
                                customBackgroundSummary
                            },
                            checked = BackgroundConfig.isCustomBackgroundEnabled
                        ) {
                            BackgroundConfig.setCustomBackgroundEnabledState(it)
                            BackgroundConfig.save(context)
                            refreshTheme.value = true
                        }
                    }

                    if (BackgroundConfig.isCustomBackgroundEnabled) {
                        if (showCustomDualDimSwitch) {
                            SwitchItem(
                                icon = Icons.Filled.DarkMode,
                                title = customDualDimTitle,
                                summary = null,
                                checked = BackgroundConfig.isDualBackgroundDimEnabled
                            ) {
                                BackgroundConfig.setDualBackgroundDimEnabledState(it)
                                BackgroundConfig.save(context)
                                refreshTheme.value = true
                            }
                        }

                        // Sliders
                        if (showCustomOpacity) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(customOpacityTitle) },
                                supportingContent = {
                                    androidx.compose.material3.Slider(
                                        value = BackgroundConfig.customBackgroundOpacity,
                                        onValueChange = { BackgroundConfig.setCustomBackgroundOpacityValue(it) },
                                        onValueChangeFinished = { BackgroundConfig.save(context) },
                                        valueRange = 0f..1f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        )
                                    )
                                }
                            )
                        }
                        
                        if (showCustomBlur) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(customBlurTitle) },
                                supportingContent = {
                                    androidx.compose.material3.Slider(
                                        value = BackgroundConfig.customBackgroundBlur,
                                        onValueChange = { BackgroundConfig.setCustomBackgroundBlurValue(it) },
                                        onValueChangeFinished = { BackgroundConfig.save(context) },
                                        valueRange = 0f..50f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        )
                                    )
                                }
                            )
                        }

                        if (!BackgroundConfig.isDualBackgroundDimEnabled && showCustomDim) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(customDimTitle) },
                                supportingContent = {
                                    androidx.compose.material3.Slider(
                                        value = BackgroundConfig.customBackgroundDim,
                                        onValueChange = { BackgroundConfig.setCustomBackgroundDimValue(it) },
                                        onValueChangeFinished = { BackgroundConfig.save(context) },
                                        valueRange = 0f..1f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                            activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        )
                                    )
                                }
                            )
                        } else {
                            if (BackgroundConfig.isDualBackgroundDimEnabled && showCustomDayDim) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(customDayDimTitle) },
                                    supportingContent = {
                                        androidx.compose.material3.Slider(
                                            value = BackgroundConfig.customBackgroundDayDim,
                                            onValueChange = { BackgroundConfig.setCustomBackgroundDayDimValue(it) },
                                            onValueChangeFinished = { BackgroundConfig.save(context) },
                                            valueRange = 0f..1f,
                                            colors = androidx.compose.material3.SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                            )
                                        )
                                    }
                                )
                            }
                            if (BackgroundConfig.isDualBackgroundDimEnabled && showCustomNightDim) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(customNightDimTitle) },
                                    supportingContent = {
                                        androidx.compose.material3.Slider(
                                            value = BackgroundConfig.customBackgroundNightDim,
                                            onValueChange = { BackgroundConfig.setCustomBackgroundNightDimValue(it) },
                                            onValueChangeFinished = { BackgroundConfig.save(context) },
                                            valueRange = 0f..1f,
                                            colors = androidx.compose.material3.SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        // Video Background
                        if (showVideoBackgroundSwitch) {
                            SwitchItem(
                                icon = Icons.Filled.PlayArrow,
                                title = videoBackgroundTitle,
                                summary = videoBackgroundSummary,
                                checked = BackgroundConfig.isVideoBackgroundEnabled
                            ) {
                                BackgroundConfig.setVideoBackgroundEnabledState(it)
                                BackgroundConfig.save(context)
                                refreshTheme.value = true
                            }
                        }

                        if (BackgroundConfig.isVideoBackgroundEnabled) {
                            if (showVideoPicker) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(text = videoSelectTitle) },
                                    supportingContent = {
                                        if (!BackgroundConfig.videoBackgroundUri.isNullOrEmpty()) {
                                            Text(
                                                text = videoSelectedText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    leadingContent = { Icon(Icons.Filled.PlayArrow, null) },
                                    modifier = Modifier.clickable {
                                        if (PermissionUtils.hasExternalStoragePermission(context)) {
                                            try {
                                                pickVideoLauncher.launch("video/*")
                                            } catch (e: ActivityNotFoundException) {
                                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "请先授予存储权限才能选择背景视频", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                            
                            val clearVideoDialog = rememberConfirmDialog(
                                onConfirm = {
                                    scope.launch {
                                        loadingDialog.show()
                                        BackgroundManager.clearVideoBackground(context)
                                        loadingDialog.hide()
                                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_background_image_cleared))
                                        refreshTheme.value = true
                                    }
                                }
                            )

                            if (showVideoClear) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(text = videoClearTitle) },
                                    leadingContent = { Icon(painterResource(id = R.drawable.ic_clear_background), null) },
                                    modifier = Modifier.clickable {
                                        clearVideoDialog.showConfirm(
                                            title = videoClearTitle,
                                            content = context.getString(R.string.settings_clear_video_background_confirm),
                                            markdown = false
                                        )
                                    }
                                )
                            }
                            
                            if (showVideoVolume) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(videoVolumeTitle) },
                                    supportingContent = {
                                        androidx.compose.material3.Slider(
                                            value = BackgroundConfig.videoVolume,
                                            onValueChange = { BackgroundConfig.setVideoVolumeValue(it) },
                                            onValueChangeFinished = { BackgroundConfig.save(context) },
                                            valueRange = 0f..1f,
                                            colors = androidx.compose.material3.SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                            )
                                        )
                                    },
                                    leadingContent = { Icon(Icons.Filled.VolumeUp, null) }
                                )
                            }
                        }

                        if (!BackgroundConfig.isVideoBackgroundEnabled) {
                            // Multi-background Mode
                            if (showMultiBackgroundSwitch) {
                                SwitchItem(
                                    icon = Icons.Filled.Dashboard,
                                    title = multiBackgroundTitle,
                                    summary = multiBackgroundSummary,
                                    checked = BackgroundConfig.isMultiBackgroundEnabled
                                ) {
                                    BackgroundConfig.setMultiBackgroundEnabledState(it)
                                    BackgroundConfig.save(context)
                                    refreshTheme.value = true
                                }
                            }
                            
                            if (BackgroundConfig.isMultiBackgroundEnabled) {
                                // Multi selectors
                                val items = listOf(
                                    Triple(R.string.settings_select_home_background, "home", BackgroundConfig.homeBackgroundUri),
                                    Triple(R.string.settings_select_kernel_background, "kernel", BackgroundConfig.kernelBackgroundUri),
                                    Triple(R.string.settings_select_superuser_background, "superuser", BackgroundConfig.superuserBackgroundUri),
                                    Triple(R.string.settings_select_system_module_background, "system", BackgroundConfig.systemModuleBackgroundUri),
                                    Triple(R.string.settings_select_settings_background, "settings", BackgroundConfig.settingsBackgroundUri)
                                )
                                items.forEach { (titleRes, type, uri) ->
                                    val title = stringResource(id = titleRes)
                                    if (matchAppearance || shouldShow(title)) {
                                        ListItem(
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            headlineContent = { Text(text = title) },
                                            supportingContent = {
                                                if (!uri.isNullOrEmpty()) {
                                                    Text(
                                                        text = stringResource(id = R.string.settings_background_selected),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            },
                                            leadingContent = { Icon(painterResource(id = R.drawable.ic_custom_background), null) },
                                            modifier = Modifier.clickable {
                                                if (PermissionUtils.hasExternalStoragePermission(context) && 
                                                    PermissionUtils.hasWriteExternalStoragePermission(context)) {
                                                    pickingType = type
                                                    try {
                                                        pickImageLauncher.launch("image/*")
                                                    } catch (e: ActivityNotFoundException) {
                                                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "请先授予存储权限才能选择背景图片", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                // Single Background Selector
                                if (showSinglePicker) {
                                    ListItem(
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        headlineContent = { Text(text = singleSelectTitle) },
                                        supportingContent = {
                                            if (!BackgroundConfig.customBackgroundUri.isNullOrEmpty()) {
                                                Text(
                                                    text = singleSelectedText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        },
                                        leadingContent = { Icon(painterResource(id = R.drawable.ic_custom_background), null) },
                                        modifier = Modifier.clickable {
                                            if (PermissionUtils.hasExternalStoragePermission(context) && 
                                                PermissionUtils.hasWriteExternalStoragePermission(context)) {
                                                pickingType = "default"
                                                try {
                                                    pickImageLauncher.launch("image/*")
                                                } catch (e: ActivityNotFoundException) {
                                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "请先授予存储权限才能选择背景图片", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                // Clear button (Single mode only)
                                if (!BackgroundConfig.customBackgroundUri.isNullOrEmpty()) {
                                    val clearBackgroundDialog = rememberConfirmDialog(
                                        onConfirm = {
                                            scope.launch {
                                                loadingDialog.show()
                                                BackgroundManager.clearCustomBackground(context)
                                                loadingDialog.hide()
                                                snackBarHost.showSnackbar(message = context.getString(R.string.settings_background_image_cleared))
                                                refreshTheme.value = true
                                            }
                                        }
                                    )
                                    if (showSingleClear) {
                                        ListItem(
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            headlineContent = { Text(text = singleClearTitle) },
                                            leadingContent = { Icon(painterResource(id = R.drawable.ic_clear_background), null) },
                                            modifier = Modifier.clickable {
                                                clearBackgroundDialog.showConfirm(title = singleClearTitle, content = context.getString(R.string.settings_clear_background_confirm), markdown = false)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Custom Font
                    if (showCustomFontSwitch) {
                        SwitchItem(
                            icon = Icons.Filled.TextFields,
                            title = customFontTitle,
                            summary = if (FontConfig.isCustomFontEnabled) {
                                if (FontConfig.customFontFilename != null) {
                                    customFontSelectedText
                                } else {
                                    customFontEnabledText
                                }
                            } else {
                                customFontSummary
                            },
                            checked = FontConfig.isCustomFontEnabled
                        ) {
                            FontConfig.setCustomFontEnabledState(it)
                            FontConfig.save(context)
                            refreshTheme.value = true
                        }
                    }
                    
                    if (FontConfig.isCustomFontEnabled) {
                        if (showSelectFont) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(text = selectFontTitle) },
                                supportingContent = {
                                    Text(
                                        text = stringResource(id = R.string.settings_font_select_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                },
                                leadingContent = { Icon(Icons.Filled.TextFields, null) },
                                modifier = Modifier.clickable {
                                    try {
                                        pickFontLauncher.launch("*/*")
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        
                        if (FontConfig.customFontFilename != null) {
                            val clearFontDialog = rememberConfirmDialog(
                                onConfirm = {
                                    FontConfig.clearFont(context)
                                    refreshTheme.value = true
                                    scope.launch {
                                        snackBarHost.showSnackbar(message = context.getString(R.string.settings_font_cleared))
                                    }
                                }
                            )
                            if (showClearFont) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(text = clearFontTitle) },
                                    leadingContent = { Icon(Icons.Filled.RemoveFromQueue, null) },
                                    modifier = Modifier.clickable {
                                        clearFontDialog.showConfirm(
                                            title = clearFontTitle,
                                            content = context.getString(R.string.settings_clear_font_confirm)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Theme Store/Import/Export
                    if (showThemeStore) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = themeStoreTitle) },
                            modifier = Modifier.clickable { navigator.navigate(ThemeStoreScreenDestination) },
                            leadingContent = { Icon(Icons.Filled.ShoppingCart, null) }
                        )
                    }
                    
                    if (showSaveTheme) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = saveThemeTitle) },
                            modifier = Modifier.clickable { showExportDialog.value = true },
                            leadingContent = { Icon(Icons.Filled.Save, null) }
                        )
                    }
                    
                    if (showImportTheme) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = importThemeTitle) },
                            modifier = Modifier.clickable {
                                showFilePicker.value = true
                            },
                            leadingContent = { Icon(Icons.Filled.Folder, null) }
                        )
                    }
                }
            }
            
            // Multimedia (Moved)


            // Behavior Category
            val behaviorTitle = stringResource(R.string.settings_category_behavior)
            val matchBehavior = shouldShow(behaviorTitle)

            val webDebuggingTitle = stringResource(id = R.string.enable_web_debugging)
            val webDebuggingSummary = stringResource(id = R.string.enable_web_debugging_summary)
            val showWebDebugging = aPatchReady && (matchBehavior || shouldShow(webDebuggingTitle, webDebuggingSummary))

            val installConfirmTitle = stringResource(id = R.string.settings_apm_install_confirm)
            val installConfirmSummary = stringResource(id = R.string.settings_apm_install_confirm_summary)
            val showInstallConfirm = aPatchReady && (matchBehavior || shouldShow(installConfirmTitle, installConfirmSummary))

            val disableModulesTitle = stringResource(id = R.string.settings_show_disable_all_modules)
            val disableModulesSummary = stringResource(id = R.string.settings_show_disable_all_modules_summary)
            val showDisableModules = aPatchReady && (matchBehavior || shouldShow(disableModulesTitle, disableModulesSummary))

            val stayOnPageTitle = stringResource(id = R.string.settings_apm_stay_on_page)
            val stayOnPageSummary = stringResource(id = R.string.settings_apm_stay_on_page_summary)
            val showStayOnPage = aPatchReady && (matchBehavior || shouldShow(stayOnPageTitle, stayOnPageSummary))

            val hideApatchTitle = stringResource(id = R.string.settings_hide_apatch_card)
            val hideApatchSummary = stringResource(id = R.string.settings_hide_apatch_card_summary)
            val showHideApatch = matchBehavior || shouldShow(hideApatchTitle, hideApatchSummary)

            val hideSuTitle = stringResource(id = R.string.home_hide_su_path)
            val hideSuSummary = stringResource(id = R.string.home_hide_su_path_summary)
            val showHideSu = matchBehavior || shouldShow(hideSuTitle, hideSuSummary)

            val hideKpatchTitle = stringResource(id = R.string.home_hide_kpatch_version)
            val hideKpatchSummary = stringResource(id = R.string.home_hide_kpatch_version_summary)
            val showHideKpatch = matchBehavior || shouldShow(hideKpatchTitle, hideKpatchSummary)

            val hideFingerprintTitle = stringResource(id = R.string.home_hide_fingerprint)
            val hideFingerprintSummary = stringResource(id = R.string.home_hide_fingerprint_summary)
            val showHideFingerprint = matchBehavior || shouldShow(hideFingerprintTitle, hideFingerprintSummary)

            // Badge Count Settings
            val badgeCountTitle = stringResource(id = R.string.enable_badge_count)
            val badgeCountSummary = stringResource(id = R.string.enable_badge_count_summary)
            val showSuperUserBadgeTitle = stringResource(id = R.string.badge_superuser)
            val showApmBadgeTitle = stringResource(id = R.string.badge_apm)
            val showKernelBadgeTitle = stringResource(id = R.string.badge_kernel)

            val showBadgeCount = matchBehavior ||
                shouldShow(badgeCountTitle, badgeCountSummary) ||
                shouldShow(showSuperUserBadgeTitle) ||
                shouldShow(showApmBadgeTitle) ||
                shouldShow(showKernelBadgeTitle)

            val showBehaviorCategory = showWebDebugging || showInstallConfirm || showDisableModules || showStayOnPage || showHideApatch || showHideSu || showHideKpatch || showHideFingerprint || showBadgeCount

            if (showBehaviorCategory) {
                SettingsCategory(icon = Icons.Filled.TouchApp, title = behaviorTitle, isSearching = searchText.isNotEmpty()) {
                    // Web Debugging
                    if (showWebDebugging) {
                        SwitchItem(
                            icon = Icons.Filled.DeveloperMode,
                            title = webDebuggingTitle,
                            summary = webDebuggingSummary,
                            checked = enableWebDebugging
                        ) {
                            APApplication.sharedPreferences.edit { putBoolean("enable_web_debugging", it) }
                            enableWebDebugging = it
                        }
                    }

                    // Install Confirm
                    if (showInstallConfirm) {
                        SwitchItem(
                            icon = Icons.Filled.Save,
                            title = installConfirmTitle,
                            summary = installConfirmSummary,
                            checked = installConfirm
                        ) {
                            prefs.edit { putBoolean("apm_install_confirm_enabled", it) }
                            installConfirm = it
                        }
                    }
                    
                    // Show Disable All Modules
                    if (showDisableModules) {
                        SwitchItem(
                            icon = Icons.Filled.DeleteSweep,
                            title = disableModulesTitle,
                            summary = disableModulesSummary,
                            checked = showDisableAllModules
                        ) {
                            prefs.edit { putBoolean("show_disable_all_modules", it) }
                            showDisableAllModules = it
                        }
                    }
                    
                    // Stay on Page
                    if (showStayOnPage) {
                        SwitchItem(
                            icon = Icons.Filled.AspectRatio,
                            title = stayOnPageTitle,
                            summary = stayOnPageSummary,
                            checked = stayOnPage
                        ) {
                            prefs.edit { putBoolean("apm_action_stay_on_page", it) }
                            stayOnPage = it
                        }
                    }
                
                    // Hide Cards/Info
                    if (showHideApatch) {
                        SwitchItem(
                            icon = Icons.Filled.Info,
                            title = hideApatchTitle,
                            summary = hideApatchSummary,
                            checked = hideApatchCard
                        ) {
                            prefs.edit { putBoolean("hide_apatch_card", it) }
                            hideApatchCard = it
                        }
                    }
                    
                    if (showHideSu) {
                        SwitchItem(
                            icon = Icons.Filled.Visibility,
                            title = hideSuTitle,
                            summary = hideSuSummary,
                            checked = hideSuPath
                        ) {
                            prefs.edit { putBoolean("hide_su_path", it) }
                            hideSuPath = it
                        }
                    }
                    
                    if (showHideKpatch) {
                        SwitchItem(
                            icon = Icons.Filled.Visibility,
                            title = hideKpatchTitle,
                            summary = hideKpatchSummary,
                            checked = hideKpatchVersion
                        ) {
                            prefs.edit { putBoolean("hide_kpatch_version", it) }
                            hideKpatchVersion = it
                        }
                    }
                    
                    if (showHideFingerprint) {
                        SwitchItem(
                            icon = Icons.Filled.Visibility,
                            title = hideFingerprintTitle,
                            summary = hideFingerprintSummary,
                            checked = hideFingerprint
                        ) {
                            prefs.edit { putBoolean("hide_fingerprint", it) }
                            hideFingerprint = it
                        }
                    }

                    // Badge Count Settings
                    if (showBadgeCount) {
                        SettingsCategory(
                            icon = Icons.Filled.Notifications,
                            title = badgeCountTitle,
                            summary = badgeCountSummary,
                            isSearching = searchText.isNotEmpty()
                        ) {
                            CheckboxItem(
                                icon = Icons.Filled.Security,
                                title = showSuperUserBadgeTitle,
                                summary = null,
                                checked = enableSuperUserBadge
                            ) {
                                prefs.edit().putBoolean("badge_superuser", it).apply()
                                enableSuperUserBadge = it
                            }

                            CheckboxItem(
                                icon = Icons.Filled.Extension,
                                title = showApmBadgeTitle,
                                summary = null,
                                checked = enableApmBadge
                            ) {
                                prefs.edit().putBoolean("badge_apm", it).apply()
                                enableApmBadge = it
                            }

                            CheckboxItem(
                                icon = Icons.Filled.Archive,
                                title = showKernelBadgeTitle,
                                summary = null,
                                checked = enableKernelBadge
                            ) {
                                prefs.edit().putBoolean("badge_kernel", it).apply()
                                enableKernelBadge = it
                            }
                        }
                    }
                }
            }

            // Security Category
            val securityTitle = stringResource(R.string.settings_category_security)
            val matchSecurity = shouldShow(securityTitle)

            val biometricTitle = stringResource(id = R.string.settings_biometric_login)
            val biometricSummary = stringResource(id = R.string.settings_biometric_login_summary)
            val showBiometric = canAuthenticate && (matchSecurity || shouldShow(biometricTitle, biometricSummary))

            val clearKeyTitle = stringResource(id = R.string.clear_super_key)
            val superKeyText = stringResource(id = R.string.super_key)
            val showClearKey = kPatchReady && (matchSecurity || shouldShow(clearKeyTitle, superKeyText))

            val storeKeyTitle = stringResource(id = R.string.settings_donot_store_superkey)
            val storeKeySummary = stringResource(id = R.string.settings_donot_store_superkey_summary)
            val showStoreKey = matchSecurity || shouldShow(storeKeyTitle, storeKeySummary)

            val showSecurityCategory = showBiometric || showClearKey || showStoreKey

            if (showSecurityCategory) {
                SettingsCategory(icon = Icons.Filled.Security, title = securityTitle, isSearching = searchText.isNotEmpty()) {
                    // Biometric
                    if (showBiometric) {
                        val scope = rememberCoroutineScope()
                        val strongBiometricTitle = stringResource(id = R.string.settings_strong_biometric)
                        val strongBiometricSummary = stringResource(id = R.string.settings_strong_biometric_summary)
                        var strongBiometric by rememberSaveable { mutableStateOf(prefs.getBoolean("strong_biometric", false)) }

                        SwitchItem(
                            icon = Icons.Filled.Fingerprint,
                            title = biometricTitle,
                            summary = biometricSummary,
                            checked = biometricLogin,
                            onCheckedChange = { checked ->
                                if (!checked && biometricLogin) {
                                    scope.launch {
                                        val activity = context as? androidx.fragment.app.FragmentActivity
                                        if (activity != null) {
                                            if (me.bmax.apatch.util.BiometricUtils.authenticate(activity)) {
                                                prefs.edit { putBoolean("biometric_login", false) }
                                                biometricLogin = false
                                            }
                                        } else {
                                            prefs.edit { putBoolean("biometric_login", false) }
                                            biometricLogin = false
                                        }
                                    }
                                } else {
                                    prefs.edit { putBoolean("biometric_login", checked) }
                                    biometricLogin = checked
                                }
                            })

                        if (biometricLogin) {
                            SwitchItem(
                                icon = Icons.Filled.VerifiedUser,
                                title = strongBiometricTitle,
                                summary = strongBiometricSummary,
                                checked = strongBiometric,
                                onCheckedChange = {
                                    prefs.edit { putBoolean("strong_biometric", it) }
                                    strongBiometric = it
                                }
                            )
                        }
                    }

                    // Clear Key
                    if (showClearKey) {
                        val clearKeyDialogTitle = stringResource(id = R.string.clear_super_key)
                        val clearKeyDialogContent = stringResource(id = R.string.settings_clear_super_key_dialog)
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = { Icon(Icons.Filled.Key, superKeyText) },
                            headlineContent = { Text(clearKeyTitle) },
                            modifier = Modifier.clickable {
                                clearKeyDialog.showConfirm(
                                    title = clearKeyDialogTitle,
                                    content = clearKeyDialogContent,
                                    markdown = false,
                                )
                            })
                    }
                    
                    // Store Key
                    if (showStoreKey) {
                        SwitchItem(
                            icon = Icons.Filled.Key,
                            title = storeKeyTitle,
                            summary = storeKeySummary,
                            checked = bSkipStoreSuperKey,
                            onCheckedChange = {
                                bSkipStoreSuperKey = it
                                APatchKeyHelper.setShouldSkipStoreSuperKey(bSkipStoreSuperKey)
                            })
                    }
                }
            }

            // Module Category
            val moduleTitle = stringResource(R.string.settings_category_module)
            val matchModule = shouldShow(moduleTitle)

            val showMoreInfoTitle = stringResource(id = R.string.settings_show_more_module_info)
            val showMoreInfoSummary = stringResource(id = R.string.settings_show_more_module_info_summary)
            val showMoreInfo = aPatchReady && (matchModule || shouldShow(showMoreInfoTitle, showMoreInfoSummary))

            val moduleSortOptimizationTitle = stringResource(id = R.string.settings_module_sort_optimization)
            val moduleSortOptimizationSummary = stringResource(id = R.string.settings_module_sort_optimization_summary)
            var moduleSortOptimization by rememberSaveable { mutableStateOf(prefs.getBoolean("module_sort_optimization", true)) }
            val showModuleSortOptimization = aPatchReady && (matchModule || shouldShow(moduleSortOptimizationTitle, moduleSortOptimizationSummary))

            val disableModuleUpdateCheckTitle = stringResource(id = R.string.settings_disable_module_update_check)
            val disableModuleUpdateCheckSummary = stringResource(id = R.string.settings_disable_module_update_check_summary)
            var disableModuleUpdateCheck by rememberSaveable { mutableStateOf(prefs.getBoolean("disable_module_update_check", false)) }
            val showDisableModuleUpdateCheck = aPatchReady && (matchModule || shouldShow(disableModuleUpdateCheckTitle, disableModuleUpdateCheckSummary))

            val foldSystemModuleTitle = stringResource(id = R.string.settings_fold_system_module)
            val foldSystemModuleSummary = stringResource(id = R.string.settings_fold_system_module_summary)
            var foldSystemModule by rememberSaveable { mutableStateOf(prefs.getBoolean("fold_system_module", false)) }
            val showFoldSystemModule = aPatchReady && (matchModule || shouldShow(foldSystemModuleTitle, foldSystemModuleSummary))

            val apmBatchInstallFullProcessTitle = stringResource(id = R.string.apm_batch_install_full_process)
            val apmBatchInstallFullProcessSummary = stringResource(id = R.string.apm_batch_install_full_process_summary)
            var apmBatchInstallFullProcess by rememberSaveable { mutableStateOf(prefs.getBoolean("apm_batch_install_full_process", false)) }
            val showApmBatchInstallFullProcess = aPatchReady && (matchModule || shouldShow(apmBatchInstallFullProcessTitle, apmBatchInstallFullProcessSummary))

            val showModuleCategory = showMoreInfo || showModuleSortOptimization || showDisableModuleUpdateCheck || showFoldSystemModule || showApmBatchInstallFullProcess

            if (showModuleCategory) {
                SettingsCategory(icon = Icons.Filled.Extension, title = moduleTitle, isSearching = searchText.isNotEmpty()) {
                    if (showDisableModuleUpdateCheck) {
                        SwitchItem(
                            icon = Icons.Filled.CloudOff,
                            title = disableModuleUpdateCheckTitle,
                            summary = disableModuleUpdateCheckSummary,
                            checked = disableModuleUpdateCheck
                        ) {
                            prefs.edit { putBoolean("disable_module_update_check", it) }
                            disableModuleUpdateCheck = it
                        }
                    }

                    if (showFoldSystemModule) {
                        SwitchItem(
                            icon = Icons.Filled.Settings,
                            title = foldSystemModuleTitle,
                            summary = foldSystemModuleSummary,
                            checked = foldSystemModule
                        ) {
                            prefs.edit { putBoolean("fold_system_module", it) }
                            foldSystemModule = it
                        }
                    }

                    if (showApmBatchInstallFullProcess) {
                        SwitchItem(
                            icon = Icons.Filled.ViewList,
                            title = apmBatchInstallFullProcessTitle,
                            summary = apmBatchInstallFullProcessSummary,
                            checked = apmBatchInstallFullProcess
                        ) {
                            prefs.edit { putBoolean("apm_batch_install_full_process", it) }
                            apmBatchInstallFullProcess = it
                        }
                    }

                    if (showModuleSortOptimization) {
                        SwitchItem(
                            icon = Icons.Filled.FilterList,
                            title = moduleSortOptimizationTitle,
                            summary = moduleSortOptimizationSummary,
                            checked = moduleSortOptimization
                        ) {
                            prefs.edit { putBoolean("module_sort_optimization", it) }
                            moduleSortOptimization = it
                        }
                    }

                    if (showMoreInfo) {
                        SwitchItem(
                            icon = Icons.Filled.Info,
                            title = showMoreInfoTitle,
                            summary = showMoreInfoSummary,
                            checked = showMoreModuleInfo
                        ) {
                            prefs.edit { putBoolean("show_more_module_info", it) }
                            showMoreModuleInfo = it
                        }
                    }
                }
            }

            // Backup Category
            val backupTitle = stringResource(id = R.string.settings_category_backup)
            val matchBackup = shouldShow(backupTitle)
            
            val showWebDavDialog = remember { mutableStateOf(false) }
            
            val enableCloudBackupTitle = stringResource(id = R.string.settings_enable_cloud_backup)
            val enableCloudBackupSummary = stringResource(id = R.string.settings_enable_cloud_backup_summary)
            val showEnableCloudBackup = matchBackup || shouldShow(enableCloudBackupTitle, enableCloudBackupSummary)
            
            val configureWebDavTitle = stringResource(id = R.string.settings_configure_webdav)
            val showConfigureWebDav = BackupConfig.isBackupEnabled && (matchBackup || shouldShow(configureWebDavTitle))

            val enableLocalBackupTitle = stringResource(id = R.string.settings_enable_local_backup)
            val enableLocalBackupSummary = stringResource(id = R.string.settings_enable_local_backup_summary)
            val showEnableLocalBackup = matchBackup || shouldShow(enableLocalBackupTitle, enableLocalBackupSummary)

            val enableBootBackupTitle = stringResource(id = R.string.settings_auto_backup_boot)
            val enableBootBackupSummary = stringResource(id = R.string.settings_auto_backup_boot_summary)
            val showEnableBootBackup = matchBackup || shouldShow(enableBootBackupTitle, enableBootBackupSummary)

            val openBackupDirTitle = stringResource(id = R.string.settings_open_backup_dir)
            val showOpenBackupDir = autoBackupModule && (matchBackup || shouldShow(openBackupDirTitle))
            
            val showBackupCategory = showEnableCloudBackup || showConfigureWebDav || showEnableLocalBackup || showEnableBootBackup || showOpenBackupDir
            
            if (showBackupCategory) {
                SettingsCategory(
                    title = backupTitle,
                    icon = Icons.Filled.Cloud,
                    isSearching = searchText.isNotEmpty()
                ) {
                     if (showEnableLocalBackup) {
                        SwitchItem(
                            icon = Icons.Filled.Save,
                            title = enableLocalBackupTitle,
                            summary = enableLocalBackupSummary,
                            checked = autoBackupModule
                        ) {
                            prefs.edit { putBoolean("auto_backup_module", it) }
                            autoBackupModule = it
                        }
                     }

                     if (showEnableBootBackup) {
                        var autoBackupBoot by rememberSaveable { mutableStateOf(prefs.getBoolean("auto_backup_boot", false)) }
                        SwitchItem(
                            icon = Icons.Filled.Save,
                            title = enableBootBackupTitle,
                            summary = enableBootBackupSummary,
                            checked = autoBackupBoot
                        ) {
                            prefs.edit { putBoolean("auto_backup_boot", it) }
                            autoBackupBoot = it
                        }
                     }

                     if (showOpenBackupDir) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(openBackupDirTitle) },
                            modifier = Modifier.clickable {
                                val backupDir = java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "FolkPatch/ModuleBackups")
                                if (!backupDir.exists()) backupDir.mkdirs()

                                try {
                                    val intent = Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", backupDir)
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.setDataAndType(uri, "resource/folder")
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e2: Exception) {
                                            val intent2 = Intent(Intent.ACTION_VIEW)
                                            intent2.setDataAndType(uri, "*/*")
                                            intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(Intent.createChooser(intent2, context.getString(R.string.settings_open_backup_dir)))
                                        }
                                    } catch (e3: Exception) {
                                        Toast.makeText(context, R.string.backup_dir_open_failed, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            leadingContent = { Icon(Icons.Filled.Folder, null) }
                        )
                     }

                     if (showEnableCloudBackup) {
                        SwitchItem(
                            icon = Icons.Filled.Cloud,
                            title = enableCloudBackupTitle,
                            summary = enableCloudBackupSummary,
                            checked = BackupConfig.isBackupEnabled
                        ) {
                            BackupConfig.isBackupEnabled = it
                            BackupConfig.save(context)
                        }
                     }
                     
                     if (showConfigureWebDav) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = configureWebDavTitle) },
                            leadingContent = { Icon(Icons.Filled.Settings, null) },
                            modifier = Modifier.clickable {
                                showWebDavDialog.value = true
                            }
                        )
                     }
                }
            }
            
            if (showWebDavDialog.value) {
                WebDavConfigDialog(showWebDavDialog)
            }

            // Multimedia Category
            val multimediaTitle = stringResource(id = R.string.settings_category_multimedia)
            val matchMultimedia = shouldShow(multimediaTitle)

            val musicTitle = stringResource(id = R.string.settings_background_music)
            val musicSummary = stringResource(id = R.string.settings_background_music_summary)
            val musicEnabledText = stringResource(id = R.string.settings_background_music_enabled)
            val musicPlayingText = if (MusicConfig.musicFilename != null) stringResource(id = R.string.settings_background_music_playing, MusicConfig.musicFilename!!) else ""
            val showMusicSwitch = matchMultimedia || shouldShow(musicTitle, musicSummary, musicEnabledText, musicPlayingText)

            val selectMusicTitle = stringResource(id = R.string.settings_select_music_file)
            val musicSelectedText = stringResource(id = R.string.settings_music_selected)
            val showSelectMusic = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(selectMusicTitle, musicSelectedText))

            val autoPlayTitle = stringResource(id = R.string.settings_music_auto_play)
            val autoPlaySummary = stringResource(id = R.string.settings_music_auto_play_summary)
            val showAutoPlay = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(autoPlayTitle, autoPlaySummary))

            val loopingTitle = stringResource(id = R.string.settings_music_looping)
            val loopingSummary = stringResource(id = R.string.settings_music_looping_summary)
            val showLooping = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(loopingTitle, loopingSummary))

            val musicVolumeTitle = stringResource(id = R.string.settings_music_volume)
            val showMusicVolume = MusicConfig.isMusicEnabled && (matchMultimedia || shouldShow(musicVolumeTitle))

            val playbackControlTitle = stringResource(id = R.string.settings_music_playback_control)
            val showPlaybackControl = MusicConfig.isMusicEnabled && MusicConfig.musicFilename != null && (matchMultimedia || shouldShow(playbackControlTitle))

            val clearMusicTitle = stringResource(id = R.string.settings_clear_music)
            val showClearMusic = MusicConfig.isMusicEnabled && MusicConfig.musicFilename != null && (matchMultimedia || shouldShow(clearMusicTitle))

            // Sound Effect Config
            val soundEffectTitle = stringResource(id = R.string.settings_sound_effect)
            val soundEffectSummary = stringResource(id = R.string.settings_sound_effect_summary)
            val soundEffectEnabledText = stringResource(id = R.string.settings_sound_effect_enabled)
            val soundEffectPlayingText = if (SoundEffectConfig.soundEffectFilename != null) stringResource(id = R.string.settings_sound_effect_playing, SoundEffectConfig.soundEffectFilename!!) else ""
            val showSoundEffectSwitch = matchMultimedia || shouldShow(soundEffectTitle, soundEffectSummary, soundEffectEnabledText, soundEffectPlayingText)

            val selectSoundEffectTitle = stringResource(id = R.string.settings_select_sound_effect)
            val soundEffectSelectedText = stringResource(id = R.string.settings_sound_effect_selected)
            val showSelectSoundEffect = SoundEffectConfig.isSoundEffectEnabled && (matchMultimedia || shouldShow(selectSoundEffectTitle, soundEffectSelectedText))
            
            val soundEffectScopeTitle = stringResource(id = R.string.settings_sound_effect_scope)
            val showSoundEffectScope = SoundEffectConfig.isSoundEffectEnabled && (matchMultimedia || shouldShow(soundEffectScopeTitle))

            val showMultimediaCategory = showMusicSwitch || showSelectMusic || showAutoPlay || showLooping || showMusicVolume || showPlaybackControl || showClearMusic || showSoundEffectSwitch || showSelectSoundEffect || showSoundEffectScope

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
                        val showClearSoundEffect = SoundEffectConfig.isSoundEffectEnabled && SoundEffectConfig.soundEffectFilename != null && (matchMultimedia || shouldShow(clearSoundEffectTitle))
                        
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
                }
            }
            NavigationBarsSpacer(modifier = Modifier.align(Alignment.CenterHorizontally))
        } // End Column

        // --- Dialogs outside Column (to ensure they aren't clipped or affected by category collapse) ---

        if (showUpdateDialog.value) {
            UpdateDialog(
                onDismiss = { showUpdateDialog.value = false },
                onUpdate = {
                    showUpdateDialog.value = false
                    UpdateChecker.openUpdateUrl(context)
                }
            )
        }

        if (showDpiDialog.value) {
            DpiChooseDialog(showDpiDialog)
        }

        if (showExportDialog.value) {
            ThemeExportDialog(
                showDialog = showExportDialog,
                onConfirm = { metadata ->
                    pendingExportMetadata = metadata
                    scope.launch {
                        loadingDialog.show()
                        try {
                            val exportDir = java.io.File("/storage/emulated/0/Download/FolkPatch/Themes/")
                             if (!exportDir.exists()) {
                                 exportDir.mkdirs()
                             }
                             val safeName = metadata.name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
                             val fileName = "$safeName.fpt"
                             val file = java.io.File(exportDir, fileName)
                             val uri = Uri.fromFile(file)
                            
                            val success = ThemeManager.exportTheme(context, uri, metadata)
                            
                            loadingDialog.hide()
                            snackBarHost.showSnackbar(
                                message = if (success) context.getString(R.string.settings_theme_saved) + ": ${file.absolutePath}" else context.getString(R.string.settings_theme_save_failed)
                            )
                        } catch (e: Exception) {
                            loadingDialog.hide()
                            snackBarHost.showSnackbar(message = context.getString(R.string.settings_theme_save_failed) + ": ${e.message}")
                        }
                        pendingExportMetadata = null
                    }
                }
            )
        }

        if (showImportDialog.value && pendingImportMetadata != null) {
            ThemeImportDialog(
                showDialog = showImportDialog,
                metadata = pendingImportMetadata!!,
                onConfirm = {
                    pendingImportUri?.let { uri ->
                        scope.launch {
                            loadingDialog.show()
                            val success = ThemeManager.importTheme(context, uri)
                            loadingDialog.hide()
                            snackBarHost.showSnackbar(
                                message = if (success) context.getString(R.string.settings_theme_imported) else context.getString(R.string.settings_theme_import_failed)
                            )
                            pendingImportUri = null
                            pendingImportMetadata = null
                        }
                    }
                }
            )
        }

        if (showLogBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLogBottomSheet = false },
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                content = {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally)

                    ) {
                        Box {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clickable {
                                        scope.launch {
                                            val formatter =
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm")
                                            val current = LocalDateTime.now().format(formatter)
                                            try {
                                                exportBugreportLauncher.launch("APatch_bugreport_${current}.tar.gz")
                                            } catch (e: ActivityNotFoundException) {
                                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                            }
                                            showLogBottomSheet = false
                                        }
                                    }
                            ) {
                                Icon(
                                    Icons.Filled.Save,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    text = stringResource(id = R.string.save_log),
                                    modifier = Modifier.padding(top = 16.dp),
                                    textAlign = TextAlign.Center.also {
                                        LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.None
                                        )
                                    }

                                )
                            }

                        }
                        Box {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clickable {
                                        scope.launch {
                                            val bugreport = loadingDialog.withLoading {
                                                withContext(Dispatchers.IO) {
                                                    getBugreportFile(context)
                                                }
                                            }

                                            val uri: Uri = FileProvider.getUriForFile(
                                                context,
                                                "${BuildConfig.APPLICATION_ID}.fileprovider",
                                                bugreport
                                            )

                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                setDataAndType(uri, "application/gzip")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }

                                            context.startActivity(
                                                Intent.createChooser(
                                                    shareIntent,
                                                    context.getString(R.string.send_log)
                                                )
                                            )
                                            showLogBottomSheet = false
                                        }
                                    }) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    text = stringResource(id = R.string.send_log),
                                    modifier = Modifier.padding(top = 16.dp),
                                    textAlign = TextAlign.Center.also {
                                        LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.None
                                        )
                                    }

                                )
                            }

                        }
                    }
                    NavigationBarsSpacer()
                })
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListLoadingSchemeDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
                    text = stringResource(R.string.settings_app_list_loading_scheme),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val currentScheme = prefs.getString("app_list_loading_scheme", "root_service")

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = 2.dp
                ) {
                    Column {
                        val schemes = listOf(
                            "root_service" to R.string.app_list_loading_scheme_root_service,
                            "package_manager" to R.string.app_list_loading_scheme_package_manager
                        )

                        schemes.forEach { (scheme, labelId) ->
                            ListItem(
                                headlineContent = { Text(stringResource(labelId)) },
                                leadingContent = {
                                    RadioButton(
                                        selected = currentScheme == scheme,
                                        onClick = null
                                    )
                                },
                                modifier = Modifier.clickable {
                                    prefs.edit { putString("app_list_loading_scheme", scheme) }
                                    showDialog.value = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(id = android.R.string.cancel))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDavConfigDialog(showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(BackupConfig.webdavUrl) }
    var username by remember { mutableStateOf(BackupConfig.webdavUsername) }
    var password by remember { mutableStateOf(BackupConfig.webdavPassword) }
    var path by remember { mutableStateOf(BackupConfig.webdavPath) }
    var isTesting by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false },
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 1f) // Ensure opaque surface color
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.webdav_config_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.webdav_url)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.webdav_username)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.webdav_password)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text(stringResource(R.string.webdav_path_label)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    
                    TextButton(onClick = { showLogDialog = true }) {
                        Text(stringResource(R.string.webdav_view_logs))
                    }
                    
                    TextButton(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                val result = WebDavUtils.testConnection(url, username, password)
                                isTesting = false
                                if (result.isSuccess) {
                                    Toast.makeText(context, context.getString(R.string.webdav_test_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.webdav_test_failed, result.exceptionOrNull()?.message), Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isTesting
                    ) {
                        Text(stringResource(R.string.test))
                    }
                    
                    Button(onClick = {
                        BackupConfig.webdavUrl = url
                        BackupConfig.webdavUsername = username
                        BackupConfig.webdavPassword = password
                        BackupConfig.webdavPath = path
                        BackupConfig.save(context)
                        showDialog.value = false
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
    
    if (showLogDialog) {
        BackupLogDialog(showDialog = remember { mutableStateOf(true) }, onDismiss = { showLogDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupLogDialog(showDialog: MutableState<Boolean>, onDismiss: () -> Unit) {
    var logs by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Load logs on start
    scope.launch {
        logs = BackupLogManager.readLogs()
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier
                .width(350.dp)
                .height(500.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 1f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.webdav_backup_logs_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val scrollState = rememberScrollState()
                    Text(
                        text = logs.ifEmpty { stringResource(R.string.webdav_no_logs) },
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(scrollState),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        scope.launch {
                            BackupLogManager.clearLogs()
                            logs = ""
                        }
                    }) {
                        Text(stringResource(R.string.webdav_clear_logs))
                    }
                    Button(onClick = onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolkXAnimationTypeDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
                    text = stringResource(R.string.settings_folkx_animation_type),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                val currentType = prefs.getString("folkx_animation_type", "linear")
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = 2.dp
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_folkx_animation_linear)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentType == "linear",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("folkx_animation_type", "linear").apply()
                                showDialog.value = false
                            }
                        )
                        
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_folkx_animation_spatial)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentType == "spatial",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("folkx_animation_type", "spatial").apply()
                                showDialog.value = false
                            }
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_folkx_animation_fade)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentType == "fade",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("folkx_animation_type", "fade").apply()
                                showDialog.value = false
                            }
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_folkx_animation_vertical)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentType == "vertical",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("folkx_animation_type", "vertical").apply()
                                showDialog.value = false
                            }
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_folkx_animation_diagonal)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentType == "diagonal",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("folkx_animation_type", "diagonal").apply()
                                showDialog.value = false
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
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(id = android.R.string.cancel))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolkXAnimationSpeedDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
                    text = stringResource(R.string.settings_folkx_animation_speed),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val currentSpeed = prefs.getFloat("folkx_animation_speed", 1.0f)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = 2.dp
                ) {
                    Column {
                        val speeds = listOf(
                            0.5f to "0.5x",
                            0.75f to "0.75x",
                            1.0f to "1.0x",
                            1.25f to "1.25x",
                            1.5f to "1.5x",
                            2.0f to "2.0x"
                        )

                        speeds.forEach { (speed, label) ->
                            ListItem(
                                headlineContent = { Text(label) },
                                leadingContent = {
                                    RadioButton(
                                        selected = currentSpeed == speed,
                                        onClick = null
                                    )
                                },
                                modifier = Modifier.clickable {
                                    prefs.edit().putFloat("folkx_animation_speed", speed).apply()
                                    showDialog.value = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(id = android.R.string.cancel))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeChooseDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
            LazyColumn {
                items(colorsList()) {
                    ListItem(
                        headlineContent = { Text(text = stringResource(it.nameId)) },
                        modifier = Modifier.clickable {
                            showDialog.value = false
                            prefs.edit { putString("custom_color", it.name) }
                            refreshTheme.value = true
                        })
                }

            }

            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }

}

private data class APColor(
    val name: String, @param:StringRes val nameId: Int
)

private fun colorsList(): List<APColor> {
    return listOf(
        APColor("amber", R.string.amber_theme),
        APColor("blue_grey", R.string.blue_grey_theme),
        APColor("blue", R.string.blue_theme),
        APColor("brown", R.string.brown_theme),
        APColor("cyan", R.string.cyan_theme),
        APColor("deep_orange", R.string.deep_orange_theme),
        APColor("deep_purple", R.string.deep_purple_theme),
        APColor("green", R.string.green_theme),
        APColor("indigo", R.string.indigo_theme),
        APColor("light_blue", R.string.light_blue_theme),
        APColor("light_green", R.string.light_green_theme),
        APColor("lime", R.string.lime_theme),
        APColor("orange", R.string.orange_theme),
        APColor("pink", R.string.pink_theme),
        APColor("purple", R.string.purple_theme),
        APColor("red", R.string.red_theme),
        APColor("sakura", R.string.sakura_theme),
        APColor("teal", R.string.teal_theme),
        APColor("yellow", R.string.yellow_theme),
    )
}

@Composable
private fun colorNameToString(colorName: String): Int {
    return colorsList().find { it.name == colorName }?.nameId ?: R.string.blue_theme
}

val suPathChecked: (path: String) -> Boolean = {
    it.startsWith("/") && it.trim().length > 1
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetSUPathDialog(showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    var suPath by remember { mutableStateOf(Natives.suPath()) }
    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
            Column(modifier = Modifier.padding(PaddingValues(all = 24.dp))) {
                Box(
                    Modifier
                        .padding(PaddingValues(bottom = 16.dp))
                        .align(Alignment.Start)
                ) {
                    Text(
                        text = stringResource(id = R.string.setting_reset_su_path),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Box(
                    Modifier
                        .weight(weight = 1f, fill = false)
                        .padding(PaddingValues(bottom = 12.dp))
                        .align(Alignment.Start)
                ) {
                    OutlinedTextField(
                        value = suPath,
                        onValueChange = {
                            suPath = it
                        },
                        label = { Text(stringResource(id = R.string.setting_reset_su_new_path)) },
                        visualTransformation = VisualTransformation.None,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {

                        Text(stringResource(id = android.R.string.cancel))
                    }

                    Button(enabled = suPathChecked(suPath), onClick = {
                        showDialog.value = false
                        val success = Natives.resetSuPath(suPath)
                        Toast.makeText(
                            context,
                            if (success) R.string.success else R.string.failure,
                            Toast.LENGTH_SHORT
                        ).show()
                        rootShellForResult("echo $suPath > ${APApplication.SU_PATH_FILE}")
                    }) {
                        Text(stringResource(id = android.R.string.ok))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTitleChooseDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
            LazyColumn {
                items(appTitleList()) { title ->
                    ListItem(
                        headlineContent = { Text(text = stringResource(title.nameId)) },
                        modifier = Modifier.clickable {
                            showDialog.value = false
                            prefs.edit { putString("app_title", title.name) }
                        })
                }
            }

            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

private data class AppTitle(
    val name: String, @param:StringRes val nameId: Int
)

private fun appTitleList(): List<AppTitle> {
    return listOf(
        AppTitle("folkpatch", R.string.app_title_folkpatch),
        AppTitle("fpatch", R.string.app_title_fpatch),
        AppTitle("apatch_folk", R.string.app_title_apatch_folk),
        AppTitle("apatchx", R.string.app_title_apatchx),
        AppTitle("apatch", R.string.app_title_apatch),
        AppTitle("kernelpatch", R.string.app_title_kernelpatch),
        AppTitle("supersu", R.string.app_title_supersu),
        AppTitle("folksu", R.string.app_title_folksu),
        AppTitle("superuser", R.string.app_title_superuser),
        AppTitle("superpatch", R.string.app_title_superpatch),
        AppTitle("magicpatch", R.string.app_title_magicpatch),
    )
}

@Composable
private fun appTitleNameToString(titleName: String): Int {
    return appTitleList().find { it.name == titleName }?.nameId ?: R.string.app_title_folkpatch
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDialog(showLanguageDialog: MutableState<Boolean>) {

    val languages = stringArrayResource(id = R.array.languages)
    val languagesValues = stringArrayResource(id = R.array.languages_values)

    if (showLanguageDialog.value) {
        BasicAlertDialog(
            onDismissRequest = { showLanguageDialog.value = false }
        ) {
            Surface(
                modifier = Modifier
                    .width(150.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                color = AlertDialogDefaults.containerColor,
            ) {
                LazyColumn {
                    itemsIndexed(languages) { index, item ->
                        ListItem(
                            headlineContent = { Text(item) },
                            modifier = Modifier.clickable {
                                showLanguageDialog.value = false
                                if (index == 0) {
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.getEmptyLocaleList()
                                    )
                                } else {
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags(
                                            languagesValues[index]
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }

            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DpiChooseDialog(showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    val activity = context as? Activity

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
            LazyColumn {
                items(me.bmax.apatch.util.DPIUtils.presets) { preset ->
                    ListItem(
                        headlineContent = { Text(text = preset.name) },
                        modifier = Modifier.clickable {
                            showDialog.value = false
                            me.bmax.apatch.util.DPIUtils.setDpi(context, preset.value)
                            // Restart activity to apply changes
                            activity?.recreate()
                        })
                }
            }

            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconChooseDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences
    val context = LocalContext.current

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
            LazyColumn {
                items(iconPresetList()) { preset ->
                    ListItem(
                        headlineContent = { Text(text = stringResource(preset.nameId)) },
                        modifier = Modifier.clickable {
                            showDialog.value = false
                            prefs.edit { putString("launcher_icon_variant", preset.name) }
                            me.bmax.apatch.util.LauncherIconUtils.applySaved(context, preset.name)
                        })
                }
            }

            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SELinuxModeDialog(
    showDialog: MutableState<Boolean>,
    currentMode: String,
    onModeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(currentMode) }

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false },
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
                    text = stringResource(R.string.settings_selinux_mode),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = 2.dp
                ) {
                    Column {
                        // Enforcing Mode
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_selinux_mode_enforcing)) },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.settings_selinux_mode_enforcing_summary),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = selectedMode == "Enforcing",
                                    onClick = { selectedMode = "Enforcing" }
                                )
                            },
                            modifier = Modifier.clickable { selectedMode = "Enforcing" }
                        )

                        // Permissive Mode
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_selinux_mode_permissive)) },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.settings_selinux_mode_permissive_summary),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = selectedMode == "Permissive",
                                    onClick = { selectedMode = "Permissive" }
                                )
                            },
                            modifier = Modifier.clickable { selectedMode = "Permissive" }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(id = android.R.string.cancel))
                    }

                    Button(
                        onClick = {
                            val success = setSELinuxMode(selectedMode == "Enforcing")
                            if (success) {
                                onModeChanged(selectedMode)
                            }
                            showDialog.value = false
                        },
                        enabled = selectedMode != currentMode
                    ) {
                        Text(stringResource(id = android.R.string.ok))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopAppNameChooseDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences
    val context = LocalContext.current
    val currentName = prefs.getString("desktop_app_name", "FolkPatch")

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text(text = "FolkPatch") },
                        modifier = Modifier.clickable {
                            showDialog.value = false
                            prefs.edit { putString("desktop_app_name", "FolkPatch") }
                            me.bmax.apatch.util.LauncherIconUtils.applySaved(context)
                        },
                        trailingContent = {
                            if (currentName == "FolkPatch" || currentName == null) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                            }
                        }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text(text = "FolkSU") },
                        modifier = Modifier.clickable {
                            showDialog.value = false
                            prefs.edit { putString("desktop_app_name", "FolkSU") }
                            me.bmax.apatch.util.LauncherIconUtils.applySaved(context)
                        },
                        trailingContent = {
                            if (currentName == "FolkSU") {
                                Icon(Icons.Filled.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }

            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

private data class IconPreset(
    val name: String, @param:StringRes val nameId: Int
)

private fun iconPresetList(): List<IconPreset> {
    return listOf(
        IconPreset("default", R.string.launcher_icon_default),
        IconPreset("classic", R.string.launcher_icon_classic),
        IconPreset("apatch", R.string.launcher_icon_apatch),
        IconPreset("kernelsu", R.string.launcher_icon_kernelsu),
        IconPreset("kernelsunext", R.string.launcher_icon_kernelsu_next),
        IconPreset("kitsune", R.string.launcher_icon_kitsune),
        IconPreset("magisk", R.string.launcher_icon_magisk),
        IconPreset("superroot", R.string.launcher_icon_superroot),
    )
}

@Composable
private fun iconNameToString(iconName: String): Int {
    return iconPresetList().find { it.name == iconName }?.nameId ?: R.string.launcher_icon_default
}

@Composable
private fun homeLayoutStyleToString(style: String): Int {
    return when (style) {
        "kernelsu" -> R.string.settings_home_layout_grid
        "focus" -> R.string.settings_home_layout_focus
        "sign" -> R.string.settings_home_layout_sign
        else -> R.string.settings_home_layout_default
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayoutChooseDialog(showDialog: MutableState<Boolean>) {
    val prefs = APApplication.sharedPreferences

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
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
                    text = stringResource(R.string.settings_home_layout_style),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                val currentStyle = prefs.getString("home_layout_style", "focus")
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = 2.dp
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_home_layout_default)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentStyle == "default",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("home_layout_style", "default").apply()
                                showDialog.value = false
                            }
                        )
                        
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_home_layout_grid)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentStyle == "kernelsu",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("home_layout_style", "kernelsu").apply()
                                showDialog.value = false
                            }
                        )
                        
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_home_layout_focus)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentStyle == "focus",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("home_layout_style", "focus").apply()
                                showDialog.value = false
                            }
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_home_layout_sign)) },
                            leadingContent = {
                                RadioButton(
                                    selected = currentStyle == "sign",
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                prefs.edit().putString("home_layout_style", "sign").apply()
                                showDialog.value = false
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
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(id = android.R.string.cancel))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeExportDialog(
    showDialog: MutableState<Boolean>,
    onConfirm: (ThemeManager.ThemeMetadata) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("phone") }
    var version by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false },
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.theme_export_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.theme_name)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.theme_type),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "phone" }
                    ) {
                        RadioButton(
                            selected = type == "phone",
                            onClick = { type = "phone" }
                        )
                        Text(
                            text = stringResource(R.string.theme_type_phone),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { type = "tablet" }
                            .padding(start = 16.dp)
                    ) {
                        RadioButton(
                            selected = type == "tablet",
                            onClick = { type = "tablet" }
                        )
                        Text(
                            text = stringResource(R.string.theme_type_tablet),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text(stringResource(R.string.theme_version)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(stringResource(R.string.theme_author)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.theme_description)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                showDialog.value = false
                                onConfirm(
                                    ThemeManager.ThemeMetadata(
                                        name = name,
                                        type = type,
                                        version = version,
                                        author = author,
                                        description = description
                                    )
                                )
                            }
                        },
                        enabled = name.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.theme_export_action))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeImportDialog(
    showDialog: MutableState<Boolean>,
    metadata: ThemeManager.ThemeMetadata,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false },
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.theme_import_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.theme_import_confirm),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.theme_info),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(text = "${stringResource(R.string.theme_name)}: ${metadata.name}")
                        Text(text = "${stringResource(R.string.theme_type)}: ${if (metadata.type == "tablet") stringResource(R.string.theme_type_tablet) else stringResource(R.string.theme_type_phone)}")
                        if (metadata.version.isNotEmpty()) {
                            Text(text = "${stringResource(R.string.theme_version)}: ${metadata.version}")
                        }
                        if (metadata.author.isNotEmpty()) {
                            Text(text = "${stringResource(R.string.theme_author)}: ${metadata.author}")
                        }
                        if (metadata.description.isNotEmpty()) {
                            Text(
                                text = "${stringResource(R.string.theme_description)}: ${metadata.description}",
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    Button(onClick = {
                        showDialog.value = false
                        onConfirm()
                    }) {
                        Text(stringResource(R.string.theme_import_action))
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}
