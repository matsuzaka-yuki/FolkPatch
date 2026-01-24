package me.bmax.apatch.ui.screen.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SettingsCategory
import me.bmax.apatch.ui.component.SwitchItem

@Composable
fun BehaviorSettings(
    searchText: String,
    kPatchReady: Boolean,
    aPatchReady: Boolean,
) {
    val prefs = APApplication.sharedPreferences
    val context = LocalContext.current
    
    // Behavior Category
    val behaviorTitle = stringResource(R.string.settings_category_behavior)
    val matchBehavior = shouldShow(searchText, behaviorTitle)

    val webDebuggingTitle = stringResource(id = R.string.enable_web_debugging)
    val webDebuggingSummary = stringResource(id = R.string.enable_web_debugging_summary)
    val showWebDebugging = matchBehavior || shouldShow(searchText, webDebuggingTitle, webDebuggingSummary)

    val installConfirmTitle = stringResource(id = R.string.settings_apm_install_confirm)
    val installConfirmSummary = stringResource(id = R.string.settings_apm_install_confirm_summary)
    val showInstallConfirm = aPatchReady && (matchBehavior || shouldShow(searchText, installConfirmTitle, installConfirmSummary))

    val disableModulesTitle = stringResource(id = R.string.settings_show_disable_all_modules)
    val disableModulesSummary = stringResource(id = R.string.settings_show_disable_all_modules_summary)
    val showDisableModules = aPatchReady && (matchBehavior || shouldShow(searchText, disableModulesTitle, disableModulesSummary))

    val stayOnPageTitle = stringResource(id = R.string.settings_apm_stay_on_page)
    val stayOnPageSummary = stringResource(id = R.string.settings_apm_stay_on_page_summary)
    val showStayOnPage = aPatchReady && (matchBehavior || shouldShow(searchText, stayOnPageTitle, stayOnPageSummary))

    var currentStyle by remember { mutableStateOf(prefs.getString("home_layout_style", "focus")) }
    
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "home_layout_style") {
                currentStyle = sharedPreferences.getString("home_layout_style", "focus")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val hideApatchTitle = stringResource(id = R.string.settings_hide_apatch_card)
    val hideApatchSummary = stringResource(id = R.string.settings_hide_apatch_card_summary)
    val showHideApatch = currentStyle != "focus" && (matchBehavior || shouldShow(searchText, hideApatchTitle, hideApatchSummary))

    val hideSuTitle = stringResource(id = R.string.home_hide_su_path)
    val hideSuSummary = stringResource(id = R.string.home_hide_su_path_summary)
    val showHideSu = kPatchReady && (matchBehavior || shouldShow(searchText, hideSuTitle, hideSuSummary))

    val hideKpatchTitle = stringResource(id = R.string.home_hide_kpatch_version)
    val hideKpatchSummary = stringResource(id = R.string.home_hide_kpatch_version_summary)
    val showHideKpatch = kPatchReady && (matchBehavior || shouldShow(searchText, hideKpatchTitle, hideKpatchSummary))

    val hideFingerprintTitle = stringResource(id = R.string.home_hide_fingerprint)
    val hideFingerprintSummary = stringResource(id = R.string.home_hide_fingerprint_summary)
    val showHideFingerprint = kPatchReady && (matchBehavior || shouldShow(searchText, hideFingerprintTitle, hideFingerprintSummary))

    // Badge Count Settings
    val badgeCountTitle = stringResource(id = R.string.enable_badge_count)
    val badgeCountSummary = stringResource(id = R.string.enable_badge_count_summary)
    val showSuperUserBadgeTitle = stringResource(id = R.string.badge_superuser)
    val showApmBadgeTitle = stringResource(id = R.string.badge_apm)
    val showKernelBadgeTitle = stringResource(id = R.string.badge_kernel)
    
    val showBadgeSettings = kPatchReady && (matchBehavior || shouldShow(searchText, badgeCountTitle, badgeCountSummary, showSuperUserBadgeTitle, showApmBadgeTitle, showKernelBadgeTitle))

    val showBehaviorCategory = showWebDebugging || showInstallConfirm || showDisableModules || showStayOnPage || showHideApatch || showHideSu || showHideKpatch || showHideFingerprint || showBadgeSettings

    if (showBehaviorCategory) {
        SettingsCategory(icon = Icons.Filled.Visibility, title = behaviorTitle, isSearching = searchText.isNotEmpty()) {
            
            // Web Debugging
            if (showWebDebugging) {
                var enableWebDebugging by remember { mutableStateOf(prefs.getBoolean("enable_web_debugging", false)) }
                SwitchItem(
                    icon = Icons.Filled.BugReport,
                    title = webDebuggingTitle,
                    summary = webDebuggingSummary,
                    checked = enableWebDebugging,
                    onCheckedChange = {
                        enableWebDebugging = it
                        prefs.edit().putBoolean("enable_web_debugging", it).apply()
                    }
                )
            }

            // Install Confirm
            if (showInstallConfirm) {
                var installConfirm by remember { mutableStateOf(prefs.getBoolean("apm_install_confirm_enabled", true)) }
                SwitchItem(
                    icon = Icons.Filled.Check,
                    title = installConfirmTitle,
                    summary = installConfirmSummary,
                    checked = installConfirm,
                    onCheckedChange = {
                        installConfirm = it
                        prefs.edit().putBoolean("apm_install_confirm_enabled", it).apply()
                    }
                )
            }

            // Disable Modules
            if (showDisableModules) {
                var showDisableAllModules by remember { mutableStateOf(prefs.getBoolean("show_disable_all_modules", false)) }
                SwitchItem(
                    icon = Icons.Filled.CloudOff,
                    title = disableModulesTitle,
                    summary = disableModulesSummary,
                    checked = showDisableAllModules,
                    onCheckedChange = {
                        showDisableAllModules = it
                        prefs.edit().putBoolean("show_disable_all_modules", it).apply()
                    }
                )
            }

            // Stay On Page
            if (showStayOnPage) {
                var stayOnPage by remember { mutableStateOf(prefs.getBoolean("apm_action_stay_on_page", true)) }
                SwitchItem(
                    icon = Icons.Filled.RemoveFromQueue,
                    title = stayOnPageTitle,
                    summary = stayOnPageSummary,
                    checked = stayOnPage,
                    onCheckedChange = {
                        stayOnPage = it
                        prefs.edit().putBoolean("apm_action_stay_on_page", it).apply()
                    }
                )
            }

            // Hide APatch
            if (showHideApatch) {
                var hideApatchCard by remember { mutableStateOf(prefs.getBoolean("hide_apatch_card", false)) }
                SwitchItem(
                    icon = Icons.Filled.VisibilityOff,
                    title = hideApatchTitle,
                    summary = hideApatchSummary,
                    checked = hideApatchCard,
                    onCheckedChange = {
                        hideApatchCard = it
                        prefs.edit().putBoolean("hide_apatch_card", it).apply()
                    }
                )
            }

            // Hide SU Path
            if (showHideSu) {
                var hideSuPath by remember { mutableStateOf(prefs.getBoolean("hide_su_path", false)) }
                SwitchItem(
                    icon = Icons.Filled.VisibilityOff,
                    title = hideSuTitle,
                    summary = hideSuSummary,
                    checked = hideSuPath,
                    onCheckedChange = {
                        hideSuPath = it
                        prefs.edit().putBoolean("hide_su_path", it).apply()
                    }
                )
            }

            // Hide KPatch Version
            if (showHideKpatch) {
                var hideKpatchVersion by remember { mutableStateOf(prefs.getBoolean("hide_kpatch_version", false)) }
                SwitchItem(
                    icon = Icons.Filled.VisibilityOff,
                    title = hideKpatchTitle,
                    summary = hideKpatchSummary,
                    checked = hideKpatchVersion,
                    onCheckedChange = {
                        hideKpatchVersion = it
                        prefs.edit().putBoolean("hide_kpatch_version", it).apply()
                    }
                )
            }

            // Hide Fingerprint
            if (showHideFingerprint) {
                var hideFingerprint by remember { mutableStateOf(prefs.getBoolean("hide_fingerprint", false)) }
                SwitchItem(
                    icon = Icons.Filled.VisibilityOff,
                    title = hideFingerprintTitle,
                    summary = hideFingerprintSummary,
                    checked = hideFingerprint,
                    onCheckedChange = {
                        hideFingerprint = it
                        prefs.edit().putBoolean("hide_fingerprint", it).apply()
                    }
                )
            }

            // Badges
            if (showBadgeSettings) {
                var enableSuperUserBadge by remember { mutableStateOf(prefs.getBoolean("badge_superuser", true)) }
                var enableApmBadge by remember { mutableStateOf(prefs.getBoolean("badge_apm", true)) }
                var enableKernelBadge by remember { mutableStateOf(prefs.getBoolean("badge_kernel", true)) }
                var expanded by remember { mutableStateOf(false) }
                val rotationState by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "ArrowRotation"
                )

                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(text = badgeCountTitle) },
                    modifier = Modifier.clickable { expanded = !expanded },
                    supportingContent = {
                        Text(
                            text = badgeCountSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    leadingContent = { Icon(Icons.Filled.Notifications, null) },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                )

                androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        if (matchBehavior || shouldShow(searchText, showSuperUserBadgeTitle)) {
                            me.bmax.apatch.ui.component.CheckboxItem(
                                icon = null,
                                title = showSuperUserBadgeTitle,
                                summary = null,
                                checked = enableSuperUserBadge,
                                onCheckedChange = {
                                    enableSuperUserBadge = it
                                    prefs.edit().putBoolean("badge_superuser", it).apply()
                                }
                            )
                        }
                        
                        if (matchBehavior || shouldShow(searchText, showApmBadgeTitle)) {
                            me.bmax.apatch.ui.component.CheckboxItem(
                                icon = null,
                                title = showApmBadgeTitle,
                                summary = null,
                                checked = enableApmBadge,
                                onCheckedChange = {
                                    enableApmBadge = it
                                    prefs.edit().putBoolean("badge_apm", it).apply()
                                }
                            )
                        }
                        
                        if (matchBehavior || shouldShow(searchText, showKernelBadgeTitle)) {
                            me.bmax.apatch.ui.component.CheckboxItem(
                                icon = null,
                                title = showKernelBadgeTitle,
                                summary = null,
                                checked = enableKernelBadge,
                                onCheckedChange = {
                                    enableKernelBadge = it
                                    prefs.edit().putBoolean("badge_kernel", it).apply()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
