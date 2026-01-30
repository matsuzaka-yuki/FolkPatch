package me.bmax.apatch.ui.screen.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SettingsCategory
import me.bmax.apatch.ui.component.SwitchItem

@Composable
fun ModuleSettings(
    searchText: String,
    aPatchReady: Boolean,
) {
    val prefs = APApplication.sharedPreferences
    
    // Module Category
    val moduleTitle = stringResource(R.string.settings_category_module)
    val matchModule = shouldShow(searchText, moduleTitle)

    val moreInfoTitle = stringResource(id = R.string.settings_show_more_module_info)
    val moreInfoSummary = stringResource(id = R.string.settings_show_more_module_info_summary)
    val showMoreInfo = matchModule || shouldShow(searchText, moreInfoTitle, moreInfoSummary)

    val moduleSortOptimizationTitle = stringResource(id = R.string.settings_module_sort_optimization)
    val moduleSortOptimizationSummary = stringResource(id = R.string.settings_module_sort_optimization_summary)
    val showModuleSortOptimization = matchModule || shouldShow(searchText, moduleSortOptimizationTitle, moduleSortOptimizationSummary)

    val disableModuleUpdateCheckTitle = stringResource(id = R.string.settings_disable_module_update_check)
    val disableModuleUpdateCheckSummary = stringResource(id = R.string.settings_disable_module_update_check_summary)
    val showDisableModuleUpdateCheck = matchModule || shouldShow(searchText, disableModuleUpdateCheckTitle, disableModuleUpdateCheckSummary)

    val foldSystemModuleTitle = stringResource(id = R.string.settings_fold_system_module)
    val foldSystemModuleSummary = stringResource(id = R.string.settings_fold_system_module_summary)
    val showFoldSystemModule = matchModule || shouldShow(searchText, foldSystemModuleTitle, foldSystemModuleSummary)

    val apmBatchInstallFullProcessTitle = stringResource(id = R.string.apm_batch_install_full_process)
    val apmBatchInstallFullProcessSummary = stringResource(id = R.string.apm_batch_install_full_process_summary)
    val showApmBatchInstallFullProcess = matchModule || shouldShow(searchText, apmBatchInstallFullProcessTitle, apmBatchInstallFullProcessSummary)

    val simpleListBottomBarTitle = stringResource(id = R.string.settings_simple_list_bottom_bar)
    val simpleListBottomBarSummary = stringResource(id = R.string.settings_simple_list_bottom_bar_summary)
    val showSimpleListBottomBar = matchModule || shouldShow(searchText, simpleListBottomBarTitle, simpleListBottomBarSummary)

    val showModuleCategory = showMoreInfo || showModuleSortOptimization || showDisableModuleUpdateCheck || showFoldSystemModule || showApmBatchInstallFullProcess || showSimpleListBottomBar

    if (showModuleCategory) {
        SettingsCategory(icon = Icons.Filled.Extension, title = moduleTitle, isSearching = searchText.isNotEmpty()) {
            
            if (showDisableModuleUpdateCheck) {
                var disableModuleUpdateCheck by remember { mutableStateOf(prefs.getBoolean("disable_module_update_check", false)) }
                SwitchItem(
                    icon = Icons.Filled.CloudOff,
                    title = disableModuleUpdateCheckTitle,
                    summary = disableModuleUpdateCheckSummary,
                    checked = disableModuleUpdateCheck,
                    onCheckedChange = {
                        disableModuleUpdateCheck = it
                        prefs.edit().putBoolean("disable_module_update_check", it).apply()
                    }
                )
            }

            if (showMoreInfo) {
                var showMoreModuleInfo by remember { mutableStateOf(prefs.getBoolean("show_more_module_info", true)) }
                SwitchItem(
                    icon = Icons.Filled.Info,
                    title = moreInfoTitle,
                    summary = moreInfoSummary,
                    checked = showMoreModuleInfo,
                    onCheckedChange = {
                        showMoreModuleInfo = it
                        prefs.edit().putBoolean("show_more_module_info", it).apply()
                    }
                )
            }

            if (showModuleSortOptimization) {
                var moduleSortOptimization by remember { mutableStateOf(prefs.getBoolean("module_sort_optimization", true)) }
                SwitchItem(
                    icon = Icons.Filled.FormatColorFill, // Using a generic icon as Sort isn't standard
                    title = moduleSortOptimizationTitle,
                    summary = moduleSortOptimizationSummary,
                    checked = moduleSortOptimization,
                    onCheckedChange = {
                        moduleSortOptimization = it
                        prefs.edit().putBoolean("module_sort_optimization", it).apply()
                    }
                )
            }

            if (showFoldSystemModule) {
                var foldSystemModule by remember { mutableStateOf(prefs.getBoolean("fold_system_module", false)) }
                SwitchItem(
                    icon = Icons.Filled.Folder,
                    title = foldSystemModuleTitle,
                    summary = foldSystemModuleSummary,
                    checked = foldSystemModule,
                    onCheckedChange = {
                        foldSystemModule = it
                        prefs.edit().putBoolean("fold_system_module", it).apply()
                    }
                )
            }

            if (showApmBatchInstallFullProcess) {
                var apmBatchInstallFullProcess by remember { mutableStateOf(prefs.getBoolean("apm_batch_install_full_process", false)) }
                SwitchItem(
                    icon = Icons.Filled.Terminal,
                    title = apmBatchInstallFullProcessTitle,
                    summary = apmBatchInstallFullProcessSummary,
                    checked = apmBatchInstallFullProcess,
                    onCheckedChange = {
                        apmBatchInstallFullProcess = it
                        prefs.edit().putBoolean("apm_batch_install_full_process", it).apply()
                    }
                )
            }

            if (showSimpleListBottomBar) {
                var simpleListBottomBar by remember { mutableStateOf(prefs.getBoolean("simple_list_bottom_bar", false)) }
                SwitchItem(
                    icon = Icons.Filled.ViewCompact,
                    title = simpleListBottomBarTitle,
                    summary = simpleListBottomBarSummary,
                    checked = simpleListBottomBar,
                    onCheckedChange = {
                        simpleListBottomBar = it
                        prefs.edit().putBoolean("simple_list_bottom_bar", it).apply()
                    }
                )
            }
        }
    }
}
