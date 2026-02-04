package me.bmax.apatch.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ramcosta.composedestinations.generated.destinations.AppProfileScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ScriptLibraryScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.bmax.apatch.R
import me.bmax.apatch.apApp
import me.bmax.apatch.ui.component.SearchAppBar
import me.bmax.apatch.ui.component.WallpaperAwareDropdownMenu
import me.bmax.apatch.ui.component.WallpaperAwareDropdownMenuItem
import me.bmax.apatch.ui.viewmodel.SuperUserViewModel
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils.Companion.setupWindowBlurListener


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SuperUserScreen(navigator: DestinationsNavigator) {
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
                        navigator.navigate(ScriptLibraryScreenDestination)
                    }) {
                        Icon(Icons.Filled.Terminal, contentDescription = stringResource(R.string.script_library))
                    }
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
                    AppItem(app) {
                        navigator.navigate(AppProfileScreenDestination(app.packageName, app.uid))
                        viewModel.search = ""
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppItem(
    app: SuperUserViewModel.AppInfo,
    onClick: () -> Unit,
) {
    val config = app.config
    val rootGranted = config.allow != 0
    val excludeApp = config.exclude == 1

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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
                    if (excludeApp) {
                        LabelText(label = stringResource(id = R.string.su_pkg_excluded_label))
                    }
                    if (rootGranted) {
                        // LabelText(label = config.profile.uid.toString())
                        LabelText(label = "ROOT")
                        // LabelText(label = config.profile.toUid.toString())
                        // LabelText(
                        //     label = when {
                        //         // todo: valid scontext ?
                        //         config.profile.scontext.isNotEmpty() -> config.profile.scontext
                        //         else -> stringResource(id = R.string.su_selinux_via_hook)
                        //     }
                        // )
                    }
                }
            }
        }
    )
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
