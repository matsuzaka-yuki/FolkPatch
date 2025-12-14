package me.bmax.apatch.ui.screen

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
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ShoppingCart
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.generated.destinations.ThemeStoreScreenDestination

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
import me.bmax.apatch.ui.theme.refreshTheme
import me.bmax.apatch.util.APatchKeyHelper
import me.bmax.apatch.util.PermissionRequestHandler
import me.bmax.apatch.util.PermissionUtils
import me.bmax.apatch.util.getBugreportFile
import me.bmax.apatch.util.isForceUsingOverlayFS
import me.bmax.apatch.util.isGlobalNamespaceEnabled
import me.bmax.apatch.util.isLiteModeEnabled
import me.bmax.apatch.util.outputStream
import me.bmax.apatch.util.overlayFsAvailable
import me.bmax.apatch.util.rootShellForResult
import me.bmax.apatch.util.setForceUsingOverlayFS
import me.bmax.apatch.util.setGlobalNamespaceEnabled
import me.bmax.apatch.util.setLiteMode
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils
import me.bmax.apatch.util.ui.LocalSnackbarHost
import me.bmax.apatch.util.ui.NavigationBarsSpacer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import me.bmax.apatch.ui.theme.MusicConfig
import me.bmax.apatch.util.MusicManager
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Dashboard
import me.bmax.apatch.util.UpdateChecker
import me.bmax.apatch.ui.component.UpdateDialog
import androidx.compose.material.icons.filled.MusicNote

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
    var isLiteModeEnabled by rememberSaveable {
        mutableStateOf(false)
    }
    var forceUsingOverlayFS by rememberSaveable {
        mutableStateOf(false)
    }
    var bSkipStoreSuperKey by rememberSaveable {
        mutableStateOf(APatchKeyHelper.shouldSkipStoreSuperKey())
    }
    val isOverlayFSAvailable by rememberSaveable {
        mutableStateOf(overlayFsAvailable())
    }
    if (kPatchReady && aPatchReady) {
        isGlobalNamespaceEnabled = isGlobalNamespaceEnabled()
        isLiteModeEnabled = isLiteModeEnabled()
        forceUsingOverlayFS = isForceUsingOverlayFS()
    }

    val snackBarHost = LocalSnackbarHost.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
            )
        },
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackBarHost) }
    ) { paddingValues ->

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

        val showThemeChooseDialog = remember { mutableStateOf(false) }
        if (showThemeChooseDialog.value) {
            ThemeChooseDialog(showThemeChooseDialog)
        }

        val showIconChooseDialog = remember { mutableStateOf(false) }
        if (showIconChooseDialog.value) {
            IconChooseDialog(showIconChooseDialog)
        }

        val showHomeLayoutChooseDialog = remember { mutableStateOf(false) }
        if (showHomeLayoutChooseDialog.value) {
            HomeLayoutChooseDialog(showHomeLayoutChooseDialog)
        }

        val showAppTitleDialog = remember { mutableStateOf(false) }
        if (showAppTitleDialog.value) {
            AppTitleChooseDialog(showAppTitleDialog)
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
        var nightModeFollowSys by rememberSaveable { mutableStateOf(prefs.getBoolean("night_mode_follow_sys", true)) }
        var nightModeEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("night_mode_enabled", false)) }
        
        val isDynamicColorSupport = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        var useSystemDynamicColor by rememberSaveable { mutableStateOf(prefs.getBoolean("use_system_color_theme", true)) }

        val refreshThemeObserver by refreshTheme.observeAsState(false)
        if (refreshThemeObserver) {
            nightModeFollowSys = prefs.getBoolean("night_mode_follow_sys", true)
            nightModeEnabled = prefs.getBoolean("night_mode_enabled", false)
            useSystemDynamicColor = prefs.getBoolean("use_system_color_theme", true)
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
        var installConfirm by rememberSaveable { mutableStateOf(prefs.getBoolean("apm_install_confirm_enabled", true)) }
        var showDisableAllModules by rememberSaveable { mutableStateOf(prefs.getBoolean("show_disable_all_modules", false)) }
        var stayOnPage by rememberSaveable { mutableStateOf(prefs.getBoolean("apm_action_stay_on_page", true)) }
        var hideApatchCard by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_apatch_card", true)) }
        var hideSuPath by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_su_path", false)) }
        var hideKpatchVersion by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_kpatch_version", false)) }
        var hideFingerprint by rememberSaveable { mutableStateOf(prefs.getBoolean("hide_fingerprint", false)) }

        // Module
        var autoBackupModule by rememberSaveable { mutableStateOf(prefs.getBoolean("auto_backup_module", false)) }


        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            
            // General Category
            SettingsCategory(icon = Icons.Filled.Tune, title = stringResource(R.string.settings_category_general)) {
                // Language
                ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                    Text(text = stringResource(id = R.string.settings_app_language))
                }, modifier = Modifier.clickable {
                    showLanguageDialog.value = true
                }, supportingContent = {
                    Text(text = AppCompatDelegate.getApplicationLocales()[0]?.displayLanguage?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    } ?: stringResource(id = R.string.system_default),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline)
                }, leadingContent = { Icon(Icons.Filled.Translate, null) })

                // Check Update
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(stringResource(id = R.string.settings_check_update)) },
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

                // Auto Update Check
                SwitchItem(
                    icon = Icons.Filled.Update,
                    title = stringResource(id = R.string.settings_auto_update_check),
                    summary = stringResource(id = R.string.settings_auto_update_check_summary),
                    checked = autoUpdateCheck,
                    onCheckedChange = {
                        prefs.edit { putBoolean("auto_update_check", it) }
                        autoUpdateCheck = it
                    })

                // Global Namespace
                if (kPatchReady && aPatchReady) {
                    SwitchItem(
                        icon = Icons.Filled.Engineering,
                        title = stringResource(id = R.string.settings_global_namespace_mode),
                        summary = stringResource(id = R.string.settings_global_namespace_mode_summary),
                        checked = isGlobalNamespaceEnabled,
                        onCheckedChange = {
                            setGlobalNamespaceEnabled(if (isGlobalNamespaceEnabled) "0" else "1")
                            isGlobalNamespaceEnabled = it
                        })
                }

                // Lite Mode
                if (kPatchReady && aPatchReady) {
                    SwitchItem(
                        icon = Icons.Filled.RemoveFromQueue,
                        title = stringResource(id = R.string.settings_lite_mode),
                        summary = stringResource(id = R.string.settings_lite_mode_mode_summary),
                        checked = isLiteModeEnabled,
                        onCheckedChange = {
                            setLiteMode(it)
                            isLiteModeEnabled = it
                        })
                }

                // OverlayFS
                if (kPatchReady && aPatchReady && isOverlayFSAvailable) {
                    SwitchItem(
                        icon = Icons.Filled.FilePresent,
                        title = stringResource(id = R.string.settings_force_overlayfs_mode),
                        summary = stringResource(id = R.string.settings_force_overlayfs_mode_summary),
                        checked = forceUsingOverlayFS,
                        onCheckedChange = {
                            setForceUsingOverlayFS(it)
                            forceUsingOverlayFS = it
                        })
                }

                // Reset SU Path
                if (kPatchReady) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.Commit, stringResource(id = R.string.setting_reset_su_path)) },
                        supportingContent = {},
                        headlineContent = { Text(stringResource(id = R.string.setting_reset_su_path)) },
                        modifier = Modifier.clickable { showResetSuPathDialog.value = true })
                }

                // App Title
                ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                    Text(text = stringResource(id = R.string.settings_app_title))
                }, modifier = Modifier.clickable {
                    showAppTitleDialog.value = true
                }, supportingContent = {
                    val currentTitle = prefs.getString("app_title", "folkpatch")
                    Text(
                        text = stringResource(appTitleNameToString(currentTitle.toString())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }, leadingContent = { Icon(Icons.Filled.Label, null) })

                // Launcher Icon
                ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                    Text(text = stringResource(id = R.string.settings_launcher_icon))
                }, modifier = Modifier.clickable {
                    showIconChooseDialog.value = true
                }, supportingContent = {
                    val currentIcon = prefs.getString("launcher_icon_variant", "default")
                    Text(
                        text = stringResource(iconNameToString(currentIcon.toString())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }, leadingContent = { Icon(painterResource(id = R.drawable.settings), null) })

                // DPI
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(stringResource(id = R.string.settings_app_dpi)) },
                    modifier = Modifier.clickable {
                        showDpiDialog.value = true
                    },
                    supportingContent = {
                        val currentDpi = me.bmax.apatch.util.DPIUtils.currentDpi
                        val dpiText = if (currentDpi == -1) {
                            stringResource(id = R.string.system_default)
                        } else {
                            "$currentDpi DPI"
                        }
                        Text(
                            text = dpiText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    leadingContent = { Icon(Icons.Filled.AspectRatio, null) }
                )
                
                // Log
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(Icons.Filled.BugReport, stringResource(id = R.string.send_log)) },
                    headlineContent = { Text(stringResource(id = R.string.send_log)) },
                    modifier = Modifier.clickable { showLogBottomSheet = true }
                )
            }

            // Appearance Category
            SettingsCategory(icon = Icons.Filled.Palette, title = stringResource(R.string.settings_category_appearance)) {
                // Night Mode
                if (isNightModeSupported) {
                    SwitchItem(
                        icon = Icons.Filled.DarkMode,
                        title = stringResource(id = R.string.settings_night_mode_follow_sys),
                        summary = stringResource(id = R.string.settings_night_mode_follow_sys_summary),
                        checked = nightModeFollowSys
                    ) {
                        prefs.edit { putBoolean("night_mode_follow_sys", it) }
                        nightModeFollowSys = it
                        refreshTheme.value = true
                    }

                    if (!nightModeFollowSys) {
                        SwitchItem(
                            icon = if (nightModeEnabled) Icons.Filled.DarkMode else Icons.Filled.VisibilityOff,
                            title = stringResource(id = R.string.settings_night_theme_enabled),
                            summary = null,
                            checked = nightModeEnabled
                        ) {
                            prefs.edit { putBoolean("night_mode_enabled", it) }
                            nightModeEnabled = it
                            refreshTheme.value = true
                        }
                    }
                }

                // Theme Color
                if (isDynamicColorSupport) {
                    SwitchItem(
                        icon = Icons.Filled.ColorLens,
                        title = stringResource(id = R.string.settings_use_system_color_theme),
                        summary = stringResource(id = R.string.settings_use_system_color_theme_summary),
                        checked = useSystemDynamicColor
                    ) {
                        prefs.edit { putBoolean("use_system_color_theme", it) }
                        useSystemDynamicColor = it
                        refreshTheme.value = true
                    }

                    if (!useSystemDynamicColor) {
                        ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                            Text(text = stringResource(id = R.string.settings_custom_color_theme))
                        }, modifier = Modifier.clickable {
                            showThemeChooseDialog.value = true
                        }, supportingContent = {
                            val colorMode = prefs.getString("custom_color", "blue")
                            Text(
                                text = stringResource(colorNameToString(colorMode.toString())),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }, leadingContent = { Icon(Icons.Filled.FormatColorFill, null) })
                    }
                } else {
                    ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                        Text(text = stringResource(id = R.string.settings_custom_color_theme))
                    }, modifier = Modifier.clickable {
                        showThemeChooseDialog.value = true
                    }, supportingContent = {
                        val colorMode = prefs.getString("custom_color", "blue")
                        Text(
                            text = stringResource(colorNameToString(colorMode.toString())),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }, leadingContent = { Icon(Icons.Filled.FormatColorFill, null) })
                }

                // Home Layout Style
                ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                    Text(text = stringResource(id = R.string.settings_home_layout_style))
                }, modifier = Modifier.clickable {
                    showHomeLayoutChooseDialog.value = true
                }, supportingContent = {
                    val currentStyle = prefs.getString("home_layout_style", "focus")
                    Text(
                        text = stringResource(homeLayoutStyleToString(currentStyle.toString())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }, leadingContent = { Icon(Icons.Filled.Dashboard, null) })

                // Grid Layout Background
                if (prefs.getString("home_layout_style", "kernelsu") == "kernelsu") {
                    SwitchItem(
                        icon = Icons.Filled.Image,
                        title = stringResource(id = R.string.settings_grid_working_card_background),
                        summary = if (BackgroundConfig.isGridWorkingCardBackgroundEnabled) {
                            if (!BackgroundConfig.gridWorkingCardBackgroundUri.isNullOrEmpty()) {
                                stringResource(id = R.string.settings_grid_working_card_background_enabled)
                            } else {
                                stringResource(id = R.string.settings_select_background_image)
                            }
                        } else {
                            stringResource(id = R.string.settings_grid_working_card_background_summary)
                        },
                        checked = BackgroundConfig.isGridWorkingCardBackgroundEnabled,
                        onCheckedChange = {
                            BackgroundConfig.setGridWorkingCardBackgroundEnabledState(it)
                            BackgroundConfig.save(context)
                        }
                    )

                    if (BackgroundConfig.isGridWorkingCardBackgroundEnabled) {
                        // Opacity
                         ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(stringResource(id = R.string.settings_custom_background_opacity)) },
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
                        // Dim
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(stringResource(id = R.string.settings_custom_background_dim)) },
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
                        // Picker
                         ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = stringResource(id = R.string.settings_select_background_image)) },
                            supportingContent = {
                                if (!BackgroundConfig.gridWorkingCardBackgroundUri.isNullOrEmpty()) {
                                    Text(
                                        text = stringResource(id = R.string.settings_grid_working_card_background_selected),
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
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = stringResource(id = R.string.settings_clear_grid_working_card_background)) },
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

                // Custom Background (Single/Multi)
                SwitchItem(
                    icon = Icons.Filled.FormatColorFill,
                    title = stringResource(id = R.string.settings_custom_background),
                    summary = if (BackgroundConfig.isCustomBackgroundEnabled) {
                        if (!BackgroundConfig.customBackgroundUri.isNullOrEmpty()) {
                            stringResource(id = R.string.settings_custom_background_enabled)
                        } else {
                            stringResource(id = R.string.settings_select_background_image)
                        }
                    } else {
                        stringResource(id = R.string.settings_custom_background_summary)
                    },
                    checked = BackgroundConfig.isCustomBackgroundEnabled
                ) {
                    BackgroundConfig.setCustomBackgroundEnabledState(it)
                    BackgroundConfig.save(context)
                    refreshTheme.value = true
                }

                if (BackgroundConfig.isCustomBackgroundEnabled) {
                    // Sliders
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(stringResource(id = R.string.settings_custom_background_opacity)) },
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
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(stringResource(id = R.string.settings_custom_background_dim)) },
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

                    // Video Background
                    SwitchItem(
                        icon = Icons.Filled.PlayArrow,
                        title = stringResource(id = R.string.settings_video_background),
                        summary = stringResource(id = R.string.settings_video_background_summary),
                        checked = BackgroundConfig.isVideoBackgroundEnabled
                    ) {
                        BackgroundConfig.setVideoBackgroundEnabledState(it)
                        BackgroundConfig.save(context)
                        refreshTheme.value = true
                    }

                    if (BackgroundConfig.isVideoBackgroundEnabled) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = stringResource(id = R.string.settings_select_video)) },
                            supportingContent = {
                                if (!BackgroundConfig.videoBackgroundUri.isNullOrEmpty()) {
                                    Text(
                                        text = stringResource(id = R.string.settings_video_selected),
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
                        
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(stringResource(id = R.string.settings_video_volume)) },
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

                    if (!BackgroundConfig.isVideoBackgroundEnabled) {
                        // Multi-background Mode
                        SwitchItem(
                            icon = Icons.Filled.Dashboard,
                            title = stringResource(id = R.string.settings_multi_background_mode),
                            summary = stringResource(id = R.string.settings_multi_background_mode_summary),
                            checked = BackgroundConfig.isMultiBackgroundEnabled
                        ) {
                            BackgroundConfig.setMultiBackgroundEnabledState(it)
                            BackgroundConfig.save(context)
                            refreshTheme.value = true
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
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(text = stringResource(id = titleRes)) },
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
                        } else {
                            // Single Background Selector
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(text = stringResource(id = R.string.settings_select_background_image)) },
                                supportingContent = {
                                    if (!BackgroundConfig.customBackgroundUri.isNullOrEmpty()) {
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
                                val clearTitle = stringResource(id = R.string.settings_clear_background)
                                val clearConfirm = stringResource(id = R.string.settings_clear_background_confirm)
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    headlineContent = { Text(text = clearTitle) },
                                    leadingContent = { Icon(painterResource(id = R.drawable.ic_clear_background), null) },
                                    modifier = Modifier.clickable {
                                        clearBackgroundDialog.showConfirm(title = clearTitle, content = clearConfirm, markdown = false)
                                    }
                                )
                            }
                        }
                    }
                }

                // Custom Font
                SwitchItem(
                    icon = Icons.Filled.TextFields,
                    title = stringResource(id = R.string.settings_custom_font),
                    summary = if (FontConfig.isCustomFontEnabled) {
                        if (FontConfig.customFontFilename != null) {
                            stringResource(id = R.string.settings_font_selected)
                        } else {
                            stringResource(id = R.string.settings_custom_font_enabled)
                        }
                    } else {
                        stringResource(id = R.string.settings_custom_font_summary)
                    },
                    checked = FontConfig.isCustomFontEnabled
                ) {
                    FontConfig.setCustomFontEnabledState(it)
                    FontConfig.save(context)
                    refreshTheme.value = true
                }
                
                if (FontConfig.isCustomFontEnabled) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(text = stringResource(id = R.string.settings_select_font_file)) },
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
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = stringResource(id = R.string.settings_clear_font)) },
                            leadingContent = { Icon(Icons.Filled.RemoveFromQueue, null) },
                            modifier = Modifier.clickable {
                                clearFontDialog.showConfirm(
                                    title = context.getString(R.string.settings_clear_font),
                                    content = context.getString(R.string.settings_clear_font_confirm)
                                )
                            }
                        )
                    }
                }

                // Background Music (Removed from here)
                
                // Theme Store/Import/Export
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(text = stringResource(id = R.string.theme_store_title)) },
                    modifier = Modifier.clickable { navigator.navigate(ThemeStoreScreenDestination) },
                    leadingContent = { Icon(Icons.Filled.ShoppingCart, null) }
                )
                
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(text = stringResource(id = R.string.settings_save_theme)) },
                    modifier = Modifier.clickable { showExportDialog.value = true },
                    leadingContent = { Icon(Icons.Filled.Save, null) }
                )
                
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(text = stringResource(id = R.string.settings_import_theme)) },
                    modifier = Modifier.clickable {
                        showFilePicker.value = true
                    },
                    leadingContent = { Icon(Icons.Filled.Folder, null) }
                )
            }
            
            // Multimedia (Moved)


            // Behavior Category
            SettingsCategory(icon = Icons.Filled.TouchApp, title = stringResource(R.string.settings_category_behavior)) {
                if (aPatchReady) {
                    // Web Debugging
                    SwitchItem(
                        icon = Icons.Filled.DeveloperMode,
                        title = stringResource(id = R.string.enable_web_debugging),
                        summary = stringResource(id = R.string.enable_web_debugging_summary),
                        checked = enableWebDebugging
                    ) {
                        APApplication.sharedPreferences.edit { putBoolean("enable_web_debugging", it) }
                        enableWebDebugging = it
                    }
                    
                    // Install Confirm
                    SwitchItem(
                        icon = Icons.Filled.Save,
                        title = stringResource(id = R.string.settings_apm_install_confirm),
                        summary = stringResource(id = R.string.settings_apm_install_confirm_summary),
                        checked = installConfirm
                    ) {
                        prefs.edit { putBoolean("apm_install_confirm_enabled", it) }
                        installConfirm = it
                    }
                    
                    // Show Disable All Modules
                    SwitchItem(
                        icon = Icons.Filled.DeleteSweep,
                        title = stringResource(id = R.string.settings_show_disable_all_modules),
                        summary = stringResource(id = R.string.settings_show_disable_all_modules_summary),
                        checked = showDisableAllModules
                    ) {
                        prefs.edit { putBoolean("show_disable_all_modules", it) }
                        showDisableAllModules = it
                    }
                    
                    // Stay on Page
                    SwitchItem(
                        icon = Icons.Filled.AspectRatio,
                        title = stringResource(id = R.string.settings_apm_stay_on_page),
                        summary = stringResource(id = R.string.settings_apm_stay_on_page_summary),
                        checked = stayOnPage
                    ) {
                        prefs.edit { putBoolean("apm_action_stay_on_page", it) }
                        stayOnPage = it
                    }
                }
                
                // Hide Cards/Info
                SwitchItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(id = R.string.settings_hide_apatch_card),
                    summary = stringResource(id = R.string.settings_hide_apatch_card_summary),
                    checked = hideApatchCard
                ) {
                    prefs.edit { putBoolean("hide_apatch_card", it) }
                    hideApatchCard = it
                }
                
                SwitchItem(
                    icon = Icons.Filled.Visibility,
                    title = stringResource(id = R.string.home_hide_su_path),
                    summary = stringResource(id = R.string.home_hide_su_path_summary),
                    checked = hideSuPath
                ) {
                    prefs.edit { putBoolean("hide_su_path", it) }
                    hideSuPath = it
                }
                
                SwitchItem(
                    icon = Icons.Filled.Visibility,
                    title = stringResource(id = R.string.home_hide_kpatch_version),
                    summary = stringResource(id = R.string.home_hide_kpatch_version_summary),
                    checked = hideKpatchVersion
                ) {
                    prefs.edit { putBoolean("hide_kpatch_version", it) }
                    hideKpatchVersion = it
                }
                
                SwitchItem(
                    icon = Icons.Filled.Visibility,
                    title = stringResource(id = R.string.home_hide_fingerprint),
                    summary = stringResource(id = R.string.home_hide_fingerprint_summary),
                    checked = hideFingerprint
                ) {
                    prefs.edit { putBoolean("hide_fingerprint", it) }
                    hideFingerprint = it
                }
            }

            // Security Category
            SettingsCategory(icon = Icons.Filled.Security, title = stringResource(R.string.settings_category_security)) {
                // Biometric
                if (canAuthenticate) {
                    SwitchItem(
                        icon = Icons.Filled.Fingerprint,
                        title = stringResource(id = R.string.settings_biometric_login),
                        summary = stringResource(id = R.string.settings_biometric_login_summary),
                        checked = biometricLogin,
                        onCheckedChange = {
                            prefs.edit { putBoolean("biometric_login", it) }
                            biometricLogin = it
                        })
                }

                // Clear Key
                if (kPatchReady) {
                    val clearKeyDialogTitle = stringResource(id = R.string.clear_super_key)
                    val clearKeyDialogContent = stringResource(id = R.string.settings_clear_super_key_dialog)
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { Icon(Icons.Filled.Key, stringResource(id = R.string.super_key)) },
                        headlineContent = { Text(stringResource(id = R.string.clear_super_key)) },
                        modifier = Modifier.clickable {
                            clearKeyDialog.showConfirm(
                                title = clearKeyDialogTitle,
                                content = clearKeyDialogContent,
                                markdown = false,
                            )
                        })
                }
                
                // Store Key
                SwitchItem(
                    icon = Icons.Filled.Key,
                    title = stringResource(id = R.string.settings_donot_store_superkey),
                    summary = stringResource(id = R.string.settings_donot_store_superkey_summary),
                    checked = bSkipStoreSuperKey,
                    onCheckedChange = {
                        bSkipStoreSuperKey = it
                        APatchKeyHelper.setShouldSkipStoreSuperKey(bSkipStoreSuperKey)
                    })
            }



            // Module Category
            SettingsCategory(icon = Icons.Filled.Extension, title = stringResource(R.string.settings_category_module)) {
                if (aPatchReady) {
                    SwitchItem(
                        icon = Icons.Filled.Save,
                        title = stringResource(id = R.string.settings_auto_backup_module),
                        summary = stringResource(id = R.string.settings_auto_backup_module_summary) + "\n" + android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).absolutePath + "/FolkPatch/ModuleBackups",
                        checked = autoBackupModule
                    ) {
                        prefs.edit { putBoolean("auto_backup_module", it) }
                        autoBackupModule = it
                    }

                    if (autoBackupModule) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(stringResource(id = R.string.settings_open_backup_dir)) },
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
                }
            }

            // Multimedia Category
            SettingsCategory(
                title = stringResource(id = R.string.settings_category_multimedia),
                icon = Icons.Filled.Headset
            ) {
                // Background Music
                SwitchItem(
                    icon = Icons.Filled.Headset,
                    title = stringResource(id = R.string.settings_background_music),
                    summary = if (MusicConfig.isMusicEnabled) {
                        if (MusicConfig.musicFilename != null) {
                            stringResource(id = R.string.settings_background_music_playing, MusicConfig.musicFilename!!)
                        } else {
                            stringResource(id = R.string.settings_background_music_enabled)
                        }
                    } else {
                        stringResource(id = R.string.settings_background_music_summary)
                    },
                    checked = MusicConfig.isMusicEnabled
                ) {
                    MusicConfig.setMusicEnabledState(it)
                    MusicConfig.save(context)
                    MusicManager.reload()
                }

                if (MusicConfig.isMusicEnabled) {
                    // Select Music File
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(text = stringResource(id = R.string.settings_select_music_file)) },
                        supportingContent = {
                            if (MusicConfig.musicFilename != null) {
                                Text(
                                    text = stringResource(id = R.string.settings_music_selected),
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

                    // Auto Play
                    SwitchItem(
                        icon = Icons.Filled.TouchApp,
                        title = stringResource(id = R.string.settings_music_auto_play),
                        summary = stringResource(id = R.string.settings_music_auto_play_summary),
                        checked = MusicConfig.isAutoPlayEnabled
                    ) {
                        MusicConfig.setAutoPlayEnabledState(it)
                        MusicConfig.save(context)
                    }

                    // Loop Play
                    SwitchItem(
                        icon = Icons.Filled.Refresh,
                        title = stringResource(id = R.string.settings_music_looping),
                        summary = stringResource(id = R.string.settings_music_looping_summary),
                        checked = MusicConfig.isLoopingEnabled
                    ) {
                        MusicConfig.setLoopingEnabledState(it)
                        MusicConfig.save(context)
                        MusicManager.updateLooping(it)
                    }

                    // Volume
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(stringResource(id = R.string.settings_music_volume)) },
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

                    // Playback Progress
                    val currentPosition by MusicManager.currentPosition.collectAsState(initial = 0)
                    val duration by MusicManager.duration.collectAsState(initial = 0)
                    val isPlaying by MusicManager.isPlaying.collectAsState(initial = false)

                    if (MusicConfig.musicFilename != null) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(stringResource(id = R.string.settings_music_playback_control)) },
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
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = stringResource(id = R.string.settings_clear_music)) },
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
