package me.bmax.apatch.ui.screen.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import me.bmax.apatch.BuildConfig
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.launch
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SettingsCategory
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.theme.BackupConfig
import me.bmax.apatch.util.BackupLogManager
import me.bmax.apatch.util.WebDavUtils
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils
import androidx.compose.ui.graphics.Color

@Composable
fun BackupSettings(
    searchText: String,
    autoBackupModule: Boolean,
    onAutoBackupModuleChange: (Boolean) -> Unit
) {
    val prefs = APApplication.sharedPreferences
    val context = LocalContext.current
    
    // Backup Category
    val backupTitle = stringResource(id = R.string.settings_category_backup)
    val matchBackup = shouldShow(searchText, backupTitle)

    val enableCloudBackupTitle = stringResource(id = R.string.settings_enable_cloud_backup)
    val enableCloudBackupSummary = stringResource(id = R.string.settings_enable_cloud_backup_summary)
    val showEnableCloudBackup = matchBackup || shouldShow(searchText, enableCloudBackupTitle, enableCloudBackupSummary)
    
    val configureWebDavTitle = stringResource(id = R.string.settings_configure_webdav)
    val showConfigureWebDav = BackupConfig.isBackupEnabled && (matchBackup || shouldShow(searchText, configureWebDavTitle))

    val enableLocalBackupTitle = stringResource(id = R.string.settings_enable_local_backup)
    val enableLocalBackupSummary = stringResource(id = R.string.settings_enable_local_backup_summary)
    val showEnableLocalBackup = matchBackup || shouldShow(searchText, enableLocalBackupTitle, enableLocalBackupSummary)

    val enableBootBackupTitle = stringResource(id = R.string.settings_auto_backup_boot)
    val enableBootBackupSummary = stringResource(id = R.string.settings_auto_backup_boot_summary)
    val showEnableBootBackup = matchBackup || shouldShow(searchText, enableBootBackupTitle, enableBootBackupSummary)

    val openBackupDirTitle = stringResource(id = R.string.settings_open_backup_dir)
    val showOpenBackupDir = autoBackupModule && (matchBackup || shouldShow(searchText, openBackupDirTitle))
    
    val showBackupCategory = showEnableCloudBackup || showConfigureWebDav || showEnableLocalBackup || showEnableBootBackup || showOpenBackupDir

    val showWebDavDialog = remember { mutableStateOf(false) }

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
                    checked = autoBackupModule,
                    onCheckedChange = {
                        onAutoBackupModuleChange(it)
                        prefs.edit().putBoolean("auto_backup_module", it).apply()
                    }
                )
             }

             if (showEnableBootBackup) {
                 var autoBackupBoot by remember { mutableStateOf(prefs.getBoolean("auto_backup_boot", false)) }
                 SwitchItem(
                    icon = Icons.Filled.RestartAlt,
                    title = enableBootBackupTitle,
                    summary = enableBootBackupSummary,
                    checked = autoBackupBoot,
                    onCheckedChange = {
                        autoBackupBoot = it
                        prefs.edit().putBoolean("auto_backup_boot", it).apply()
                    }
                )
             }

             if (showOpenBackupDir) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(text = openBackupDirTitle) },
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
                    visualTransformation = PasswordVisualTransformation()
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
    LaunchedEffect(Unit) {
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
