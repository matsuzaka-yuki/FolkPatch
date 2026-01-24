package me.bmax.apatch.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ScriptExecutionLogScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.bmax.apatch.R
import me.bmax.apatch.data.ScriptInfo
import me.bmax.apatch.ui.component.FilePickerDialog
import me.bmax.apatch.ui.component.rememberConfirmDialog
import me.bmax.apatch.ui.component.rememberLoadingDialog
import me.bmax.apatch.ui.theme.BackgroundConfig
import me.bmax.apatch.ui.viewmodel.ScriptLibraryViewModel
import me.bmax.apatch.util.ui.LocalSnackbarHost
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ScriptLibraryScreen(navigator: DestinationsNavigator) {
    val viewModel = viewModel<ScriptLibraryViewModel>()
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val scripts by viewModel.scripts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var scriptAlias by remember { mutableStateOf("") }
    var selectedScript by remember { mutableStateOf<ScriptInfo?>(null) }

    val confirmDialog = rememberConfirmDialog()

    val confirmDeleteTitle = stringResource(R.string.script_library_confirm_delete)
    val confirmDeleteLabel = stringResource(R.string.script_library_delete)
    val dismissLabel = stringResource(android.R.string.cancel)
    val deleteSuccessMsg = context.getString(R.string.script_library_delete_success)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.script_library_title)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.script_library_add))
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            onRefresh = { viewModel.loadScripts() },
            isRefreshing = isLoading
        ) {
            if (scripts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.script_library_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(scripts) { script ->
                        ScriptItem(
                            script = script,
                            onRun = {
                                navigator.navigate(ScriptExecutionLogScreenDestination(script))
                            },
                            onDelete = {
                                selectedScript = script
                                val confirmContent = "${script.alias}\n${script.path}"

                                scope.launch {
                                    val confirmResult = confirmDialog.awaitConfirm(
                                        title = confirmDeleteTitle,
                                        content = confirmContent,
                                        confirm = confirmDeleteLabel,
                                        dismiss = dismissLabel
                                    )
                                    if (confirmResult == me.bmax.apatch.ui.component.ConfirmResult.Confirmed) {
                                        viewModel.removeScript(
                                            script,
                                            onSuccess = {
                                                scope.launch {
                                                    snackBarHost.showSnackbar(deleteSuccessMsg)
                                                }
                                            },
                                            onError = { error ->
                                                scope.launch {
                                                    snackBarHost.showSnackbar(context.getString(R.string.script_library_delete_failed, error))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddScriptDialog(
            onDismiss = {
                showAddDialog = false
                selectedFile = null
                scriptAlias = ""
            },
            onConfirm = { file, alias ->
                showAddDialog = false
                viewModel.addScript(
                    file,
                    alias,
                    onSuccess = {
                        scope.launch {
                            snackBarHost.showSnackbar(context.getString(R.string.script_library_add_success))
                        }
                    },
                    onError = { error ->
                        scope.launch {
                            snackBarHost.showSnackbar(context.getString(R.string.script_library_add_failed, error))
                        }
                    }
                )
                selectedFile = null
                scriptAlias = ""
            },
            selectedFile = selectedFile,
            onFileSelected = { selectedFile = it },
            scriptAlias = scriptAlias,
            onAliasChange = { scriptAlias = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScriptLabel(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScriptItem(
    script: ScriptInfo,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    val isWallpaperMode = BackgroundConfig.isCustomBackgroundEnabled
    val opacity = if (isWallpaperMode) {
        BackgroundConfig.customBackgroundOpacity.coerceAtLeast(0.2f)
    } else {
        1f
    }

    val cardColor = if (isWallpaperMode) {
        MaterialTheme.colorScheme.surface.copy(alpha = opacity)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
    }

    val labelOpacity = (opacity + 0.1f).coerceAtMost(1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        tonalElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 第一行：标签 + 脚本名称
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = script.alias,
                        style = MaterialTheme.typography.titleMedium
                    )

                    ScriptLabel(
                        text = "Shell",
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = labelOpacity),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // 第二行：脚本路径
                Text(
                    text = script.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 按钮区域
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onRun,
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = (opacity + 0.3f).coerceAtMost(1f))
                        )
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.script_library_run))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    FilledTonalButton(
                        onClick = onDelete,
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = (opacity + 0.3f).coerceAtMost(1f)),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.script_library_delete))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScriptDialog(
    onDismiss: () -> Unit,
    onConfirm: (File, String) -> Unit,
    selectedFile: File?,
    onFileSelected: (File) -> Unit,
    scriptAlias: String,
    onAliasChange: (String) -> Unit
) {
    var showFilePicker by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = showFilePicker) {
        FilePickerDialog(
            initialPath = null,
            allowedExtensions = listOf("sh"),
            onDismissRequest = { showFilePicker = false },
            onFileSelected = { file ->
                onFileSelected(file)
                showFilePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.script_library_add_title)) },
        text = {
            Column {
                OutlinedButton(
                    onClick = { showFilePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.script_library_select_file))
                }

                if (selectedFile != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedFile?.absolutePath ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = scriptAlias,
                    onValueChange = onAliasChange,
                    label = { Text(stringResource(R.string.script_library_alias)) },
                    placeholder = { Text(stringResource(R.string.script_library_alias_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedFile?.let { onConfirm(it, scriptAlias) }
                },
                enabled = selectedFile != null
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
