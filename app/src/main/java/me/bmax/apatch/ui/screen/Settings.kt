package me.bmax.apatch.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.NavigationLayoutScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.APApplication
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.LoadingDialogHandle
import me.bmax.apatch.ui.component.rememberLoadingDialog
import me.bmax.apatch.util.APatchKeyHelper
import me.bmax.apatch.util.DPIUtils
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Destination<RootGraph>
@Composable
fun SettingScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()

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

    val showResetSuPathDialog = remember { mutableStateOf(false) }
    val showLogBottomSheet = remember { mutableStateOf(false) }
    val showClearKeyDialog = rememberSaveable { mutableStateOf(false) }

    val loadingDialog = rememberLoadingDialog()

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
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, logSavedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior
            )
        },
        popupHost = {
            ResetSUPathDialog(showResetSuPathDialog)
            LogBottomSheet(
                showLogBottomSheet,
                scope,
                exportBugreportLauncher,
                loadingDialog,
                context
            )
        }
    )
    { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = 10.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                end = 10.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                val prefs = APApplication.sharedPreferences
                var currentDpi by rememberSaveable {
                    mutableIntStateOf(prefs.getInt("app_dpi", -1))
                }
                Card {
                    // clear key
                    if (kPatchReady) {
                        val clearKeyDialogTitle = stringResource(id = R.string.clear_super_key)
                        val clearKeyDialogContent =
                            stringResource(id = R.string.settings_clear_super_key_dialog)
                        SuperArrow(
                            title = stringResource(R.string.clear_super_key),
                            onClick = { showClearKeyDialog.value = true }
                        )
                        if (showClearKeyDialog.value) {
                            SuperDialog(
                                show = showClearKeyDialog,
                                title = clearKeyDialogTitle,
                                summary = clearKeyDialogContent
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {

                                    TextButton(
                                        stringResource(id = android.R.string.cancel),
                                        onClick = { showClearKeyDialog.value = false },
                                        modifier = Modifier.weight(1f),
                                    )

                                    Spacer(Modifier.width(20.dp))

                                    TextButton(
                                        stringResource(id = android.R.string.ok),
                                        onClick = {
                                            APatchKeyHelper.clearConfigKey()
                                            APApplication.superKey = ""
                                            showClearKeyDialog.value = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.textButtonColorsPrimary(),
                                    )
                                }
                            }
                        }
                    }

                    // store key local?
                    SuperSwitch(
                        title = stringResource(id = R.string.settings_donot_store_superkey),
                        summary = stringResource(id = R.string.settings_donot_store_superkey_summary),
                        checked = bSkipStoreSuperKey,
                        onCheckedChange = {
                            bSkipStoreSuperKey = it
                            APatchKeyHelper.setShouldSkipStoreSuperKey(bSkipStoreSuperKey)
                        })

                    // Global mount
                    if (kPatchReady && aPatchReady) {
                        SuperSwitch(
                            title = stringResource(id = R.string.settings_global_namespace_mode),
                            summary = stringResource(id = R.string.settings_global_namespace_mode_summary),
                            checked = isGlobalNamespaceEnabled,
                            onCheckedChange = {
                                setGlobalNamespaceEnabled(
                                    if (isGlobalNamespaceEnabled) {
                                        "0"
                                    } else {
                                        "1"
                                    }
                                )
                                isGlobalNamespaceEnabled = it
                            })
                    }

                    // Lite Mode
                    if (kPatchReady && aPatchReady) {
                        SuperSwitch(
                            title = stringResource(id = R.string.settings_lite_mode),
                            summary = stringResource(id = R.string.settings_lite_mode_mode_summary),
                            checked = isLiteModeEnabled,
                            onCheckedChange = {
                                setLiteMode(it)
                                isLiteModeEnabled = it
                            })
                    }

                    // Force OverlayFS
                    if (kPatchReady && aPatchReady && isOverlayFSAvailable) {
                        SuperSwitch(
                            title = stringResource(id = R.string.settings_force_overlayfs_mode),
                            summary = stringResource(id = R.string.settings_force_overlayfs_mode_summary),
                            checked = forceUsingOverlayFS,
                            onCheckedChange = {
                                setForceUsingOverlayFS(it)
                                forceUsingOverlayFS = it
                            })
                    }

                    // Stay on Page
                    if (aPatchReady) {
                        var stayOnPage by rememberSaveable {
                            mutableStateOf(prefs.getBoolean("apm_action_stay_on_page", true))
                        }
                        SuperSwitch(
                            title = stringResource(id = R.string.settings_apm_stay_on_page),
                            summary = stringResource(id = R.string.settings_apm_stay_on_page_summary),
                            checked = stayOnPage,
                            onCheckedChange = {
                                prefs.edit { putBoolean("apm_action_stay_on_page", it) }
                                stayOnPage = it
                            })

                        // APM Sorting
                        var apmSortEnabled by rememberSaveable {
                            mutableStateOf(prefs.getBoolean("apm_sort_enabled", true))
                        }
                        SuperSwitch(
                            title = stringResource(id = R.string.settings_apm_sorting),
                            summary = stringResource(id = R.string.settings_apm_sorting_summary),
                            checked = apmSortEnabled,
                            onCheckedChange = {
                                prefs.edit { putBoolean("apm_sort_enabled", it) }
                                apmSortEnabled = it
                            })
                    }

                    // WebView Debug
                    if (aPatchReady) {
                        var enableWebDebugging by rememberSaveable {
                            mutableStateOf(prefs.getBoolean("enable_web_debugging", false))
                        }

                        SuperSwitch(
                            title = stringResource(id = R.string.enable_web_debugging),
                            summary = stringResource(id = R.string.enable_web_debugging_summary),
                            checked = enableWebDebugging,
                            onCheckedChange = { isChecked ->
                                enableWebDebugging = isChecked
                                APApplication.sharedPreferences.edit {
                                    putBoolean("enable_web_debugging", isChecked)
                                }
                            }
                        )
                    }

                    // Check Update
                    var checkUpdate by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("check_update", true)
                        )
                    }

                    SuperSwitch(
                        title = stringResource(id = R.string.settings_check_update),
                        summary = stringResource(id = R.string.settings_check_update_summary),
                        checked = checkUpdate,
                        onCheckedChange = { isChecked ->
                            checkUpdate = isChecked
                            prefs.edit { putBoolean("check_update", isChecked) }
                        })

                    // Hide About Card
                    var hideAboutCard by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("hide_about_card", true)
                        )
                    }
                    SuperSwitch(
                        title = stringResource(id = R.string.hide_about_card),
                        summary = stringResource(id = R.string.hide_about_card_summary),
                        checked = hideAboutCard,
                        onCheckedChange = { isChecked ->
                            hideAboutCard = isChecked
                            prefs.edit { putBoolean("hide_about_card", isChecked) }
                        })

                    // Theme System
                    var themeMode by rememberSaveable {
                        mutableIntStateOf(prefs.getInt("color_mode", 0))
                    }

                    val themeItems = listOf(
                        stringResource(id = R.string.settings_theme_mode_system),
                        stringResource(id = R.string.settings_theme_mode_light),
                        stringResource(id = R.string.settings_theme_mode_dark),
                        stringResource(id = R.string.settings_theme_mode_monet_system),
                        stringResource(id = R.string.settings_theme_mode_monet_light),
                        stringResource(id = R.string.settings_theme_mode_monet_dark),
                    )

                    SuperDropdown(
                        title = stringResource(id = R.string.settings_theme),
                        items = themeItems,
                        selectedIndex = themeMode,
                        onSelectedIndexChange = { index ->
                            prefs.edit { putInt("color_mode", index) }
                            themeMode = index
                        }
                    )

                    // Home Layout
                    val homeLayoutItems = listOf(
                        stringResource(id = R.string.settings_home_layout_default),
                        stringResource(id = R.string.settings_home_layout_list)
                    )
                    val homeLayoutValues = listOf("default", "list")
                    var currentHomeLayout by rememberSaveable { mutableStateOf(prefs.getString("home_layout_style", "default") ?: "default") }
                    var homeLayoutIndex = homeLayoutValues.indexOf(currentHomeLayout)
                    if (homeLayoutIndex == -1) homeLayoutIndex = 0

                    SuperDropdown(
                        title = stringResource(id = R.string.settings_home_layout_style),
                        items = homeLayoutItems,
                        selectedIndex = homeLayoutIndex,
                        onSelectedIndexChange = { index ->
                            prefs.edit { putString("home_layout_style", homeLayoutValues[index]) }
                            currentHomeLayout = homeLayoutValues[index]
                        }
                    )

                    // su path
                    if (kPatchReady) {
                        SuperArrow(
                            title = stringResource(R.string.setting_reset_su_path),
                            onClick = { showResetSuPathDialog.value = true }
                        )
                    }

                    // language
                    val languages = stringArrayResource(id = R.array.languages)
                    val languagesValues = stringArrayResource(id = R.array.languages_values)

                    val currentLocales = AppCompatDelegate.getApplicationLocales()
                    val currentLanguageTag = if (currentLocales.isEmpty) {
                        null
                    } else {
                        currentLocales.get(0)?.toLanguageTag()
                    }

                    val initialIndex = if (currentLanguageTag == null) {
                        0
                    } else {
                        val index = languagesValues.indexOf(currentLanguageTag)
                        if (index >= 0) index else 0
                    }

                    var selectedIndex by remember { mutableStateOf(initialIndex) }

                    SuperDropdown(
                        title = stringResource(R.string.settings_app_language),
                        items = languages.toList(),
                        selectedIndex = selectedIndex,
                        onSelectedIndexChange = { newIndex ->
                            selectedIndex = newIndex
                            if (newIndex == 0) {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.getEmptyLocaleList()
                                )
                            } else {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(
                                        languagesValues[newIndex]
                                    )
                                )
                            }
                        }
                    )

                    // Navigation Layout Settings
                    SuperArrow(
                        title = stringResource(id = R.string.settings_nav_layout_title),
                        summary = stringResource(id = R.string.settings_nav_layout_summary),
                        onClick = {
                            navigator.navigate(NavigationLayoutScreenDestination)
                        }
                    )

                    // App DPI
                    val dpiItems = listOf(
                        stringResource(id = R.string.dpi_preset_system_default),
                        stringResource(id = R.string.dpi_preset_small),
                        stringResource(id = R.string.dpi_preset_medium),
                        stringResource(id = R.string.dpi_preset_large),
                        stringResource(id = R.string.dpi_preset_xlarge)
                    )
                    val dpiValues = listOf(-1, 320, 400, 480, 560)
                    var dpiIndex by rememberSaveable {
                        mutableStateOf(dpiValues.indexOf(currentDpi).coerceAtLeast(0))
                    }

                    SuperDropdown(
                        title = stringResource(id = R.string.settings_app_dpi),
                        items = dpiItems,
                        selectedIndex = dpiIndex,
                        onSelectedIndexChange = { index ->
                            DPIUtils.setDpi(context, dpiValues[index])
                            prefs.edit { putInt("app_dpi", dpiValues[index]) }
                            currentDpi = dpiValues[index]
                            dpiIndex = index
                            // Recreate activity to apply DPI change
                            (context as? Activity)?.recreate()
                        }
                    )

                    // log
                    SuperArrow(
                        title = stringResource(R.string.send_log),
                        onClick = {
                            showLogBottomSheet.value = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LogBottomSheet(
    showLogBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
    exportBugreportLauncher: ActivityResultLauncher<String>,
    loadingDialog: LoadingDialogHandle,
    context: Context
) {
    SuperBottomSheet(
        show = showLogBottomSheet,
        title = "Save Log", // tmp hard code strings
        onDismissRequest = { showLogBottomSheet.value = false }
    ) {
        SuperArrow(
            title = stringResource(R.string.save_log),
            onClick = {
                scope.launch {
                    val formatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm")
                    val current = LocalDateTime.now().format(formatter)
                    exportBugreportLauncher.launch("APatch_bugreport_${current}.tar.gz")
                    showLogBottomSheet.value = false
                }
            }
        )
        SuperArrow(
            title = stringResource(R.string.send_log),
            onClick = {
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
                    showLogBottomSheet.value = false
                }
            }
        )
    }
}

@Composable
fun ResetSUPathDialog(showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    var suPath by remember { mutableStateOf(Natives.suPath()) }

    val suPathChecked: (path: String) -> Boolean = {
        it.startsWith("/") && it.trim().length > 1
    }

    SuperDialog(
        show = showDialog,
        title = stringResource(R.string.setting_reset_su_path),
        onDismissRequest = { showDialog.value = false }
    ) {
        TextField(
            value = suPath,
            onValueChange = { suPath = it },
            label = stringResource(R.string.setting_reset_su_new_path),
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            TextButton(
                stringResource(id = android.R.string.cancel),
                onClick = { showDialog.value = false },
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.width(20.dp))

            TextButton(
                stringResource(id = android.R.string.ok),
                onClick = {
                    showDialog.value = false
                    val success = Natives.resetSuPath(suPath)
                    Toast.makeText(
                        context,
                        if (success) R.string.success else R.string.failure,
                        Toast.LENGTH_SHORT
                    ).show()
                    rootShellForResult("echo $suPath > ${APApplication.SU_PATH_FILE}")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                enabled = suPathChecked(suPath)
            )
        }
    }
}
