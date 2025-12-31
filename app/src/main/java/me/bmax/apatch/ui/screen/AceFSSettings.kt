package me.bmax.apatch.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.component.WallpaperAwareDropdownMenu
import me.bmax.apatch.ui.component.WallpaperAwareDropdownMenuItem
import me.bmax.apatch.util.APatchCli
import me.bmax.apatch.util.AceFSConfig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.DialogProperties

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AceFSSettingsScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(AceFSConfig.Config()) }
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                withContext(Dispatchers.IO) {
                    Natives.su()
                    try {
                        val source = java.io.File(AceFSConfig.CONFIG_PATH)
                        if (source.exists()) {
                            context.contentResolver.openOutputStream(it)?.use { output ->
                                source.inputStream().use { input ->
                                    input.copyTo(output)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, R.string.acefs_export_success, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, R.string.acefs_export_failed, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, R.string.acefs_export_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                withContext(Dispatchers.IO) {
                    Natives.su()
                    try {
                        context.contentResolver.openInputStream(it)?.use { input ->
                            val content = input.readBytes()
                            val dest = java.io.File(AceFSConfig.CONFIG_PATH)
                            dest.parentFile?.let { parent ->
                                if (!parent.exists()) {
                                    parent.mkdirs()
                                }
                            }
                            dest.writeBytes(content)
                        }
                        val newConfig = AceFSConfig.readConfig()
                        withContext(Dispatchers.Main) {
                            config = newConfig
                            Toast.makeText(context, R.string.acefs_import_success, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, R.string.acefs_import_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // Load initial state
    LaunchedEffect(Unit) {
        config = AceFSConfig.readConfig()
    }

    // Explicitly save config to disk (for manual save buttons)
    fun saveConfigToDisk(currentConfig: AceFSConfig.Config, showToast: Boolean = true) {
        config = currentConfig // Update local state immediately
        scope.launch {
            try {
                AceFSConfig.saveConfig(currentConfig)
                if (showToast) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, R.string.acefs_save_success, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.acefs_save_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Only update local state, do not save to disk
    fun updateLocalConfig(newConfig: AceFSConfig.Config) {
        config = newConfig
    }

    // Update state and save immediately (for non-list settings if needed, but user asked for manual save for lists)
    // Actually, user said "cancel these three options... dialog confirm save function, change to temporary save... use one button to agree to save settings"
    // This implies that for the lists, we use updateLocalConfig.
    // For other settings (like Switch/TextField), we can still use saveConfigToDisk immediately OR also make them manual.
    // Given the context of "Hide menu", I will assume other settings in other menus (like Spoof) can remain as is or be manual.
    // However, keeping mixed behavior is confusing.
    // Let's make the "Save" button save the whole config, and list dialogs only update local state.
    // For the Switch/TextFields, if I change them to updateLocalConfig, then the user MUST click save.
    // The user specifically mentioned "Hide menu bottom add a save long button".
    // I will implement manual save for the lists in Hide menu.
    // For consistency in this file, I'll make the lists use `updateLocalConfig`.
    // The switch/text fields are currently using `updateConfig` which I will rename/refactor.
    
    // To be safe and consistent within the Hide menu:
    // The lists will update `config`. The save button will save `config`.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.acefs_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        WallpaperAwareDropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            WallpaperAwareDropdownMenuItem(
                                text = { Text(stringResource(R.string.acefs_import_config)) },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("*/*"))
                                }
                            )
                            WallpaperAwareDropdownMenuItem(
                                text = { Text(stringResource(R.string.acefs_export_config)) },
                                onClick = {
                                    showMenu = false
                                    exportLauncher.launch("AceFS_Config.json")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. FolkPatch Hide
            ExpandableSettingsCard(
                title = stringResource(R.string.acefs_hide_service_title),
                icon = Icons.Filled.VisibilityOff
            ) {
                SwitchItem(
                    icon = null,
                    title = stringResource(R.string.acefs_hide_service_title),
                    summary = stringResource(R.string.acefs_hide_service_summary),
                    checked = config.enableHide,
                    onCheckedChange = { saveConfigToDisk(config.copy(enableHide = it), showToast = false) }
                )
            }

            // 2. Custom Umount Paths
            ExpandableSettingsCard(
                title = stringResource(R.string.acefs_category_hide),
                icon = Icons.Filled.FolderOpen
            ) {
                ConfigListRow(
                    title = stringResource(R.string.acefs_umount_paths),
                    items = config.umountPaths,
                    onItemsChange = { updateLocalConfig(config.copy(umountPaths = it)) }
                )
                
                ConfigListRow(
                    title = stringResource(R.string.acefs_hidden_paths),
                    items = config.hiddenPaths,
                    onItemsChange = { updateLocalConfig(config.copy(hiddenPaths = it)) }
                )



                Button(
                    onClick = { saveConfigToDisk(config) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.acefs_save_config))
                }
            }

            // 3. Spoof Configuration
            ExpandableSettingsCard(
                title = stringResource(R.string.acefs_category_spoof),
                icon = Icons.Filled.Face
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = config.fakeKernelVersion,
                        onValueChange = { updateLocalConfig(config.copy(fakeKernelVersion = it)) },
                        label = { Text(stringResource(R.string.acefs_fake_kernel_version)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = config.fakeBuildTime,
                        onValueChange = { updateLocalConfig(config.copy(fakeBuildTime = it)) },
                        label = { Text(stringResource(R.string.acefs_fake_build_time)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = { saveConfigToDisk(config) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.acefs_save_config))
                    }
                }
            }

            // 4. Permission Management
            ExpandableSettingsCard(
                title = stringResource(R.string.acefs_category_permission),
                icon = Icons.Filled.Security
            ) {
                SwitchItem(
                    icon = null,
                    title = stringResource(R.string.acefs_manage_superuser),
                    summary = null,
                    checked = config.enableSuManage,
                    onCheckedChange = { saveConfigToDisk(config.copy(enableSuManage = it), showToast = false) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ExpandableSettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.rotate(rotationState)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ConfigListRow(
    title: String,
    items: List<String>,
    onItemsChange: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Settings, contentDescription = "Configure")
            }
        },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        ListManageDialog(
            title = title,
            initialItems = items,
            onDismiss = { showDialog = false },
            onApply = { 
                onItemsChange(it)
                showDialog = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListManageDialog(
    title: String,
    initialItems: List<String>,
    onDismiss: () -> Unit,
    onApply: (List<String>) -> Unit
) {
    var items by remember { mutableStateOf(initialItems) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh, // Use opaque surface container
            tonalElevation = 0.dp // Remove tonal elevation to ensure solid color
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newValue ->
                                    val newItems = items.toMutableList()
                                    newItems[index] = newValue
                                    items = newItems
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                val newItems = items.toMutableList()
                                newItems.removeAt(index)
                                items = newItems
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove")
                            }
                        }
                    }
                    
                    item {
                         Button(
                            onClick = {
                                val newItems = items.toMutableList()
                                newItems.add("")
                                items = newItems
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.acefs_add_item))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onApply(items.filter { it.isNotEmpty() }) }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}
