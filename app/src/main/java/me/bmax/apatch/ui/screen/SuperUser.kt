package me.bmax.apatch.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.launch
import me.bmax.apatch.APApplication
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.apApp
import me.bmax.apatch.ui.component.ProvideMenuShape
import me.bmax.apatch.ui.component.SearchAppBar
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.component.WallpaperAwareDropdownMenu
import me.bmax.apatch.ui.component.WallpaperAwareDropdownMenuItem
import me.bmax.apatch.ui.viewmodel.SuperUserViewModel
import me.bmax.apatch.util.PkgConfig
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils.Companion.setupWindowBlurListener


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SuperUserScreen() {
    val viewModel = viewModel<SuperUserViewModel>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.backupAppList(context, it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreAppList(context, it) }
    }

    var showBatchExcludeDialog by remember { mutableStateOf(false) }

    if (showBatchExcludeDialog) {
        BatchExcludeDialog(
            onDismiss = { showBatchExcludeDialog = false },
            onExclude = {
                viewModel.excludeAll()
                showBatchExcludeDialog = false
            },
            onReverseExclude = {
                viewModel.reverseExcludeAll()
                showBatchExcludeDialog = false
            }
        )
    }

    LaunchedEffect(Unit) {
        if (viewModel.appList.isEmpty()) {
            viewModel.fetchAppList()
        }
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.su_title)) },
                searchText = viewModel.search,
                onSearchTextChange = { viewModel.search = it },
                onClearClick = { viewModel.search = "" },
                leadingActions = {
                    IconButton(onClick = {
                        showBatchExcludeDialog = true
                    }) {
                        Icon(Icons.Filled.PlaylistAddCheck, contentDescription = stringResource(R.string.su_batch_exclude_title))
                    }
                },
                dropdownContent = {
                    var showDropdown by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showDropdown = true },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(id = R.string.settings)
                        )

                        WallpaperAwareDropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            WallpaperAwareDropdownMenuItem(
                                text = { Text(stringResource(R.string.su_refresh)) },
                                onClick = {
                                    scope.launch {
                                        viewModel.fetchAppList()
                                    }
                                    showDropdown = false
                                }
                            )

                            WallpaperAwareDropdownMenuItem(
                                text = {
                                    Text(
                                        if (viewModel.showSystemApps) {
                                            stringResource(R.string.su_hide_system_apps)
                                        } else {
                                            stringResource(R.string.su_show_system_apps)
                                        }
                                    )
                                },
                                onClick = {
                                    viewModel.showSystemApps = !viewModel.showSystemApps
                                    showDropdown = false
                                }
                            )

                            WallpaperAwareDropdownMenuItem(
                                text = { Text(stringResource(R.string.su_backup_list)) },
                                onClick = {
                                    backupLauncher.launch("FolkPatch_list_backup.json")
                                    showDropdown = false
                                }
                            )

                            WallpaperAwareDropdownMenuItem(
                                text = { Text(stringResource(R.string.su_restore_list)) },
                                onClick = {
                                    restoreLauncher.launch(arrayOf("application/json", "*/*"))
                                    showDropdown = false
                                }
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->

        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            onRefresh = { scope.launch { viewModel.fetchAppList() } },
            isRefreshing = viewModel.isRefreshing
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(viewModel.appList.filter { it.packageName != apApp.packageName }, key = { it.packageName + it.uid }) { app ->
                    AppItem(app)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppItem(
    app: SuperUserViewModel.AppInfo,
) {
    val config = app.config
    var showEditProfile by remember { mutableStateOf(false) }
    var rootGranted by remember(app.config) { mutableStateOf(config.allow != 0) }
    var excludeApp by remember(app.config) { mutableIntStateOf(config.exclude) }

    ListItem(
        modifier = Modifier.clickable(onClick = {
            if (!rootGranted) {
                showEditProfile = !showEditProfile
            } else {
                rootGranted = false
                config.allow = 0
                Natives.revokeSu(app.uid)
                PkgConfig.changeConfig(config)
            }
        }),
        headlineContent = { Text(app.label) },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(app.packageInfo)
                    .crossfade(true).build(),
                contentDescription = app.label,
                modifier = Modifier
                    .padding(4.dp)
                    .width(48.dp)
                    .height(48.dp)
            )
        },
        supportingContent = {

            Column {
                Text(app.packageName)
                FlowRow {

                    if (excludeApp == 1) {
                        LabelText(label = stringResource(id = R.string.su_pkg_excluded_label))
                    }
                    if (rootGranted) {
                        LabelText(label = config.profile.uid.toString())
                        LabelText(label = config.profile.toUid.toString())
                        LabelText(
                            label = when {
                                // todo: valid scontext ?
                                config.profile.scontext.isNotEmpty() -> config.profile.scontext
                                else -> stringResource(id = R.string.su_selinux_via_hook)
                            }
                        )
                    }
                }
            }
        },
        trailingContent = {
            Switch(checked = rootGranted, onCheckedChange = {
                rootGranted = !rootGranted
                if (rootGranted) {
                    excludeApp = 0
                    config.allow = 1
                    config.exclude = 0
                    config.profile.scontext = APApplication.MAGISK_SCONTEXT
                } else {
                    config.allow = 0
                }
                config.profile.uid = app.uid
                PkgConfig.changeConfig(config)
                if (config.allow == 1) {
                    Natives.grantSu(app.uid, 0, config.profile.scontext)
                    Natives.setUidExclude(app.uid, 0)
                } else {
                    Natives.revokeSu(app.uid)
                }
            })
        },
    )

    AnimatedVisibility(
        visible = showEditProfile && !rootGranted,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 0.dp)
    ) {
        SwitchItem(
            icon = Icons.Filled.Security,
            title = stringResource(id = R.string.su_pkg_excluded_setting_title),
            summary = stringResource(id = R.string.su_pkg_excluded_setting_summary),
            checked = excludeApp == 1,
            onCheckedChange = {
                if (it) {
                    excludeApp = 1
                    config.allow = 0
                    config.profile.scontext = APApplication.DEFAULT_SCONTEXT
                    Natives.revokeSu(app.uid)
                } else {
                    excludeApp = 0
                }
                config.exclude = excludeApp
                config.profile.uid = app.uid
                PkgConfig.changeConfig(config)
                Natives.setUidExclude(app.uid, excludeApp)
            },
        )
    }
}

@Composable
fun LabelText(label: String) {
    Box(
        modifier = Modifier
            .padding(top = 4.dp, end = 4.dp)
            .background(
                Color.Black, shape = RoundedCornerShape(4.dp)
            )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 5.dp),
            style = TextStyle(
                fontSize = 8.sp,
                color = Color.White,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchExcludeDialog(
    onDismiss: () -> Unit,
    onExclude: () -> Unit,
    onReverseExclude: () -> Unit
) {
    val title = stringResource(R.string.su_batch_exclude_title)
    val content = stringResource(R.string.su_batch_exclude_content)
    val excludeText = stringResource(R.string.su_exclude_btn)
    val reverseText = stringResource(R.string.su_exclude_reverse_btn)
    val cancelText = stringResource(android.R.string.cancel)

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
            securePolicy = SecureFlagPolicy.SecureOff,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(modifier = Modifier.padding(PaddingValues(all = 24.dp))) {
                Box(
                    Modifier
                        .padding(PaddingValues(bottom = 16.dp))
                        .align(Alignment.Start)
                ) {
                    Text(text = title, style = MaterialTheme.typography.headlineSmall)
                }
                Box(
                    Modifier
                        .weight(weight = 1f, fill = false)
                        .padding(PaddingValues(bottom = 24.dp))
                        .align(Alignment.Start)
                ) {
                    Text(text = content, style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = cancelText)
                    }
                    TextButton(onClick = onExclude) {
                        Text(text = excludeText)
                    }
                    TextButton(onClick = onReverseExclude) {
                        Text(text = reverseText)
                    }
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}