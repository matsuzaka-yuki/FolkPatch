package me.bmax.apatch.ui.screen.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.APApplication
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SettingsCategory
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.component.UpdateDialog
import me.bmax.apatch.ui.component.rememberLoadingDialog
import me.bmax.apatch.util.*
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils
import me.bmax.apatch.util.ui.NavigationBarsSpacer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun GeneralSettings(
    searchText: String,
    kPatchReady: Boolean,
    aPatchReady: Boolean,
    currentSELinuxMode: String,
    onSELinuxModeChange: (String) -> Unit,
    isGlobalNamespaceEnabled: Boolean,
    onGlobalNamespaceChange: (Boolean) -> Unit,
    isMagicMountEnabled: Boolean,
    onMagicMountChange: (Boolean) -> Unit,
    snackBarHost: SnackbarHostState
) {
    val context = LocalContext.current
    val prefs = APApplication.sharedPreferences
    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()
    
    // General Category
    val generalTitle = stringResource(R.string.settings_category_general)
    val matchGeneral = shouldShow(searchText, generalTitle)
    
    val languageTitle = stringResource(id = R.string.settings_app_language)
    val languageValue = AppCompatDelegate.getApplicationLocales()[0]?.displayLanguage?.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    } ?: stringResource(id = R.string.system_default)
    val showLanguage = matchGeneral || shouldShow(searchText, languageTitle, languageValue)

    val updateTitle = stringResource(id = R.string.settings_check_update)
    val showUpdate = matchGeneral || shouldShow(searchText, updateTitle)

    val autoUpdateTitle = stringResource(id = R.string.settings_auto_update_check)
    val autoUpdateSummary = stringResource(id = R.string.settings_auto_update_check_summary)
    val showAutoUpdate = matchGeneral || shouldShow(searchText, autoUpdateTitle, autoUpdateSummary)

    val globalNamespaceTitle = stringResource(id = R.string.settings_global_namespace_mode)
    val globalNamespaceSummary = stringResource(id = R.string.settings_global_namespace_mode_summary)
    val showGlobalNamespace = (kPatchReady && aPatchReady) && (matchGeneral || shouldShow(searchText, globalNamespaceTitle, globalNamespaceSummary))

    val magicMountTitle = stringResource(id = R.string.settings_magic_mount)
    val magicMountSummary = stringResource(id = R.string.settings_magic_mount_summary)
    val showMagicMount = (kPatchReady && aPatchReady) && (matchGeneral || shouldShow(searchText, magicMountTitle, magicMountSummary))

    val selinuxModeTitle = stringResource(id = R.string.settings_selinux_mode)
    val selinuxModeSummary = stringResource(id = R.string.settings_selinux_mode_summary)
    val selinuxModeValue = when (currentSELinuxMode) {
        "Enforcing" -> stringResource(R.string.settings_selinux_mode_enforcing)
        "Permissive" -> stringResource(R.string.settings_selinux_mode_permissive)
        else -> stringResource(R.string.home_selinux_status_unknown)
    }
    val showSELinuxMode = (kPatchReady && aPatchReady) && (matchGeneral || shouldShow(searchText, selinuxModeTitle, selinuxModeValue))

    val resetSuPathTitle = stringResource(id = R.string.setting_reset_su_path)
    val showResetSuPath = kPatchReady && (matchGeneral || shouldShow(searchText, resetSuPathTitle))

    val appTitleTitle = stringResource(id = R.string.settings_app_title)
    val currentAppTitleRaw = prefs.getString("app_title", "folkpatch")
    val currentAppTitle = stringResource(appTitleNameToString(currentAppTitleRaw.toString()))
    val showAppTitle = matchGeneral || shouldShow(searchText, appTitleTitle, currentAppTitle)

    val launcherIconTitle = stringResource(id = R.string.settings_launcher_icon)
    val currentIconRaw = prefs.getString("launcher_icon_variant", "default")
    val currentIcon = stringResource(iconNameToString(currentIconRaw.toString()))
    val showLauncherIcon = matchGeneral || shouldShow(searchText, launcherIconTitle, currentIcon)

    val desktopAppNameTitle = stringResource(id = R.string.desktop_app_name)
    val currentDesktopAppName = prefs.getString("desktop_app_name", "FolkPatch")
    val showDesktopAppName = matchGeneral || shouldShow(searchText, desktopAppNameTitle, currentDesktopAppName.toString())

    val dpiTitle = stringResource(id = R.string.settings_app_dpi)
    val currentDpiVal = me.bmax.apatch.util.DPIUtils.currentDpi
    val dpiValue = if (currentDpiVal == -1) stringResource(id = R.string.system_default) else "$currentDpiVal DPI"
    val showDpi = matchGeneral || shouldShow(searchText, dpiTitle, dpiValue)

    val logTitle = stringResource(id = R.string.send_log)
    val showLog = matchGeneral || shouldShow(searchText, logTitle)

    val folkXEngineTitle = stringResource(id = R.string.settings_folkx_engine_title)
    val folkXEngineSummary = stringResource(id = R.string.settings_folkx_engine_summary)
    val showFolkXEngine = matchGeneral || shouldShow(searchText, folkXEngineTitle, folkXEngineSummary)

    val appListLoadingSchemeTitle = stringResource(id = R.string.settings_app_list_loading_scheme)
    val currentScheme = prefs.getString("app_list_loading_scheme", "root_service")
    val currentSchemeLabel = if (currentScheme == "root_service") stringResource(R.string.app_list_loading_scheme_root_service) else stringResource(R.string.app_list_loading_scheme_package_manager)
    val showAppListLoadingScheme = kPatchReady && (matchGeneral || shouldShow(searchText, appListLoadingSchemeTitle, currentSchemeLabel))

    val showGeneralCategory = showLanguage || showUpdate || showAutoUpdate || showGlobalNamespace || showMagicMount || showResetSuPath || showAppTitle || showLauncherIcon || showDesktopAppName || showDpi || showLog || showFolkXEngine || showAppListLoadingScheme || showSELinuxMode

    // Dialog States
    val showLanguageDialog = remember { mutableStateOf(false) }
    val showUpdateDialog = remember { mutableStateOf(false) }
    val showResetSuPathDialog = remember { mutableStateOf(false) }
    val showAppTitleDialog = remember { mutableStateOf(false) }
    val showIconChooseDialog = remember { mutableStateOf(false) }
    val showDesktopAppNameDialog = remember { mutableStateOf(false) }
    val showDpiDialog = remember { mutableStateOf(false) }
    var showLogBottomSheet by remember { mutableStateOf(false) }
    val showFolkXAnimationTypeDialog = remember { mutableStateOf(false) }
    val showFolkXAnimationSpeedDialog = remember { mutableStateOf(false) }
    val showAppListLoadingSchemeDialog = remember { mutableStateOf(false) }
    val showSELinuxModeDialog = remember { mutableStateOf(false) }

    var autoUpdateCheck by remember { mutableStateOf(prefs.getBoolean("auto_update_check", true)) }

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
                ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                    Text(text = updateTitle)
                }, modifier = Modifier.clickable {
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
                }, leadingContent = { Icon(Icons.Filled.Update, null) })
            }

            // Auto Check Update
            if (showAutoUpdate) {
                SwitchItem(
                    icon = Icons.Filled.Autorenew,
                    title = autoUpdateTitle,
                    summary = autoUpdateSummary,
                    checked = autoUpdateCheck,
                    onCheckedChange = {
                        autoUpdateCheck = it
                        prefs.edit { putBoolean("auto_update_check", it) }
                    }
                )
            }
            
            // FolkX Engine
            if (showFolkXEngine) {
                val folkXEngineEnabled by remember { mutableStateOf(prefs.getBoolean("folkx_engine_enabled", true)) }
                SwitchItem(
                    icon = Icons.Filled.Animation,
                    title = folkXEngineTitle,
                    summary = folkXEngineSummary,
                    checked = folkXEngineEnabled,
                    onCheckedChange = {
                        prefs.edit().putBoolean("folkx_engine_enabled", it).apply()
                        // Force recomposition or update state
                    }
                )
                
                if (folkXEngineEnabled) {
                    val currentType = prefs.getString("folkx_animation_type", "linear")
                    val currentSpeed = prefs.getFloat("folkx_animation_speed", 1.0f)
                    
                    val animationTypeLabel = when(currentType) {
                        "linear" -> R.string.settings_folkx_animation_linear
                        "spatial" -> R.string.settings_folkx_animation_spatial
                        "fade" -> R.string.settings_folkx_animation_fade
                        "vertical" -> R.string.settings_folkx_animation_vertical
                        "diagonal" -> R.string.settings_folkx_animation_diagonal
                        else -> R.string.settings_folkx_animation_linear
                    }
                    
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(stringResource(R.string.settings_folkx_animation_type)) },
                        supportingContent = {
                            Text(
                                text = stringResource(animationTypeLabel),
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
                        onGlobalNamespaceChange(it)
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
                        onMagicMountChange(it)
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
                ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                    Text(text = dpiTitle)
                }, modifier = Modifier.clickable {
                    showDpiDialog.value = true
                }, supportingContent = {
                    Text(text = dpiValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline)
                }, leadingContent = { Icon(Icons.Filled.AspectRatio, null) })
            }

            // Log
            if (showLog) {
                ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = {
                    Text(text = logTitle)
                }, modifier = Modifier.clickable {
                    showLogBottomSheet = true
                }, leadingContent = { Icon(Icons.Filled.BugReport, null) })
            }
        }
    }

    // Dialogs
    LanguageDialog(showLanguageDialog)

    if (showUpdateDialog.value) {
        UpdateDialog(
            onDismiss = { showUpdateDialog.value = false },
            onUpdate = {
                showUpdateDialog.value = false
                UpdateChecker.openUpdateUrl(context)
            }
        )
    }

    if (showResetSuPathDialog.value) {
        ResetSUPathDialog(showResetSuPathDialog)
    }
    
    if (showSELinuxModeDialog.value) {
        SELinuxModeDialog(
            showDialog = showSELinuxModeDialog,
            currentMode = currentSELinuxMode,
            onModeChanged = onSELinuxModeChange
        )
    }
    
    if (showAppTitleDialog.value) {
        AppTitleChooseDialog(showAppTitleDialog)
    }
    
    if (showIconChooseDialog.value) {
        IconChooseDialog(showIconChooseDialog)
    }
    
    if (showDesktopAppNameDialog.value) {
        DesktopAppNameChooseDialog(showDesktopAppNameDialog)
    }

    if (showDpiDialog.value) {
        DpiChooseDialog(showDpiDialog)
    }

    if (showFolkXAnimationTypeDialog.value) {
        FolkXAnimationTypeDialog(showFolkXAnimationTypeDialog)
    }

    if (showFolkXAnimationSpeedDialog.value) {
        FolkXAnimationSpeedDialog(showFolkXAnimationSpeedDialog)
    }

    if (showAppListLoadingSchemeDialog.value) {
        AppListLoadingSchemeDialog(showAppListLoadingSchemeDialog)
    }

    if (showLogBottomSheet) {
        LogBottomSheet(
            onDismissRequest = { showLogBottomSheet = false },
            snackBarHost = snackBarHost
        )
    }
}

// Dialog Implementations will be added here or imported. 
// Since we are splitting, we should copy the dialogs here or to a shared components file.
// For now, I will assume they are here. I will paste the implementations I found.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogBottomSheet(onDismissRequest: () -> Unit, snackBarHost: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()
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

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
                                    onDismissRequest()
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
                                    onDismissRequest()
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

fun appTitleList(): List<AppTitle> {
    return listOf(
        AppTitle("folkpatch", R.string.app_title_folkpatch),
        AppTitle("apatch", R.string.app_title_apatch),
        AppTitle("kpatch", R.string.app_title_kernelpatch),
        AppTitle("magisk", R.string.launcher_icon_magisk),
        AppTitle("supersu", R.string.app_title_supersu),
        AppTitle("kernelsu", R.string.launcher_icon_kernelsu)
    )
}

data class AppTitle(val name: String, val nameId: Int)

fun appTitleNameToString(titleName: String): Int {
    return appTitleList().find { it.name == titleName }?.nameId ?: R.string.app_title_folkpatch
}

@Composable
fun iconNameToString(iconName: String): Int {
    return when (iconName) {
        "default" -> R.string.launcher_icon_default
        "classic" -> R.string.launcher_icon_classic
        "apatch" -> R.string.launcher_icon_apatch
        "kernelsu" -> R.string.launcher_icon_kernelsu
        "kernelsunext" -> R.string.launcher_icon_kernelsu_next
        "kitsune" -> R.string.launcher_icon_kitsune
        "magisk" -> R.string.launcher_icon_magisk
        "superroot" -> R.string.launcher_icon_superroot
        "mask" -> R.string.launcher_icon_kitsune
        "camou" -> R.string.launcher_icon_default
        else -> R.string.launcher_icon_default
    }
}

// ... Implementations of LanguageDialog, DpiChooseDialog, etc. (Pasted from Read output)
// I will need to complete the file with the dialogs I read earlier.

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
                                    style = MaterialTheme.typography.bodyMedium,
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
                                    style = MaterialTheme.typography.bodyMedium,
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
                        listOf("linear", "spatial", "fade", "vertical", "diagonal").forEach { type ->
                            val labelId = when(type) {
                                "linear" -> R.string.settings_folkx_animation_linear
                                "spatial" -> R.string.settings_folkx_animation_spatial
                                "fade" -> R.string.settings_folkx_animation_fade
                                "vertical" -> R.string.settings_folkx_animation_vertical
                                "diagonal" -> R.string.settings_folkx_animation_diagonal
                                else -> R.string.settings_folkx_animation_linear
                            }
                             ListItem(
                                headlineContent = { Text(stringResource(labelId)) },
                                leadingContent = {
                                    RadioButton(
                                        selected = currentType == type,
                                        onClick = null
                                    )
                                },
                                modifier = Modifier.clickable {
                                    prefs.edit().putString("folkx_animation_type", type).apply()
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
fun ResetSUPathDialog(showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    var suPath by remember { mutableStateOf(me.bmax.apatch.Natives.suPath()) }
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
                        visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {

                        Text(stringResource(id = android.R.string.cancel))
                    }

                    Button(enabled = suPath.startsWith("/") && suPath.trim().length > 1, onClick = {
                        showDialog.value = false
                        val success = me.bmax.apatch.Natives.resetSuPath(suPath)
                        Toast.makeText(
                            context,
                            if (success) R.string.success else R.string.failure,
                            Toast.LENGTH_SHORT
                        ).show()
                        me.bmax.apatch.util.rootShellForResult("echo $suPath > ${APApplication.SU_PATH_FILE}")
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
    
    val appTitles = listOf(
        AppTitle("folkpatch", R.string.app_title_folkpatch),
        AppTitle("apatch", R.string.app_title_apatch),
        AppTitle("kpatch", R.string.app_title_kernelpatch),
        AppTitle("magisk", R.string.launcher_icon_magisk),
        AppTitle("supersu", R.string.app_title_supersu),
        AppTitle("kernelsu", R.string.launcher_icon_kernelsu)
    )

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
                items(appTitles) { title ->
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
    val name: String, @androidx.annotation.StringRes val nameId: Int
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

