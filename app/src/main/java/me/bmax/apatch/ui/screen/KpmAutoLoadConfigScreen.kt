package me.bmax.apatch.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.KpmAutoLoadConfig
import me.bmax.apatch.ui.component.KpmAutoLoadManager
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add

@Destination<RootGraph>
@Composable
fun KpmAutoLoadConfigScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(KpmAutoLoadManager.isEnabled.value) }
    var jsonString by remember { mutableStateOf(KpmAutoLoadManager.getConfigJson()) }
    val showSaveDialog = remember { mutableStateOf(false) }
    var isValidJson by remember { mutableStateOf(true) }
    var isVisualMode by remember { mutableStateOf(false) }
    var kpmPathsList by remember { mutableStateOf(KpmAutoLoadManager.kpmPaths.value.toList()) }

    // 根据路径列表更新JSON字符串
    fun updateJsonString(paths: List<String>, enabled: Boolean, onUpdate: (String) -> Unit) {
        val config = KpmAutoLoadConfig(enabled, paths)
        onUpdate(KpmAutoLoadManager.getConfigJson(config))
    }

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 导入KPM文件到内部存储
            val importedPath = KpmAutoLoadManager.importKpm(context, it)

            if (importedPath != null && importedPath.endsWith(".kpm", ignoreCase = true) && importedPath !in kpmPathsList) {
                kpmPathsList = kpmPathsList + importedPath
                // 更新JSON字符串
                updateJsonString(kpmPathsList, isEnabled) { newJson ->
                    jsonString = newJson
                }
                Toast.makeText(context, context.getString(R.string.kpm_autoload_save_success), Toast.LENGTH_SHORT).show()
            } else if (importedPath == null) {
                Toast.makeText(context, context.getString(R.string.kpm_autoload_file_not_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        val config = KpmAutoLoadManager.loadConfig(context)
        isEnabled = config.enabled
        jsonString = KpmAutoLoadManager.getConfigJson()
        kpmPathsList = config.kpmPaths
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.kpm_autoload_title),
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(android.R.string.cancel)
                        )
                    }
                }
            )
        },
        popupHost = {
            SuperDialog(
                title = stringResource(R.string.kpm_autoload_save_confirm),
                show = showSaveDialog,
                onDismissRequest = { showSaveDialog.value = false }
            ) {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        text = stringResource(android.R.string.cancel),
                        onClick = { showSaveDialog.value = false },
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(Modifier.width(20.dp))

                    TextButton(
                        text = stringResource(android.R.string.ok),
                        onClick = {
                            val config = if (isVisualMode) {
                                // 使用可视化模式的数据
                                KpmAutoLoadConfig(enabled = isEnabled, kpmPaths = kpmPathsList)
                            } else {
                                // 使用JSON模式的数据
                                KpmAutoLoadConfig(
                                    enabled = isEnabled, kpmPaths =
                                    KpmAutoLoadManager.parseConfigFromJson(jsonString)?.kpmPaths ?: emptyList()
                                )
                            }

                            val success = KpmAutoLoadManager.saveConfig(context, config)
                            if (success) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.kpm_autoload_save_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigator.navigateUp()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.kpm_autoload_save_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            showSaveDialog.value = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical(),
            contentPadding = PaddingValues(
                start = 10.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                end = 10.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                Card {
                    // 功能启用开关
                    SuperSwitch(
                        title = stringResource(R.string.kpm_autoload_enabled),
                        summary = stringResource(R.string.kpm_autoload_enabled_summary),
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            updateJsonString(kpmPathsList, it) { newJson ->
                                jsonString = newJson
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // 可视化模式或JSON模式
                if (isVisualMode) {
                    // 可视化模式
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.kpm_autoload_kpm_list_title),
                                    style = MiuixTheme.textStyles.title1
                                )
                                Button(
                                    onClick = {
                                        filePickerLauncher.launch("application/octet-stream")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(stringResource(R.string.kpm_autoload_add_kpm))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (kpmPathsList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.kpm_autoload_no_kpm_added),
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.secondary
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    kpmPathsList.forEach { path ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = path.substringAfterLast("/"),
                                                        style = MiuixTheme.textStyles.body1
                                                    )
                                                    Text(
                                                        text = path,
                                                        style = MiuixTheme.textStyles.body2,
                                                        color = MiuixTheme.colorScheme.secondary
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        kpmPathsList = kpmPathsList - path
                                                        updateJsonString(kpmPathsList, isEnabled) { newJson ->
                                                            jsonString = newJson
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = stringResource(R.string.kpm_autoload_remove_kpm),
                                                        tint = MiuixTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // JSON配置编辑框
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.kpm_autoload_json_config),
                                style = MiuixTheme.textStyles.title1,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            TextField(
                                value = jsonString,
                                onValueChange = { input ->
                                    jsonString = input
                                    isValidJson = KpmAutoLoadManager.parseConfigFromJson(input) != null
                                    // 如果JSON有效，更新路径列表
                                    if (isValidJson) {
                                        KpmAutoLoadManager.parseConfigFromJson(input)?.let { config ->
                                            kpmPathsList = config.kpmPaths
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(R.string.kpm_autoload_json_label)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (!isValidJson) {
                                Text(
                                    text = stringResource(R.string.kpm_autoload_json_error),
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.kpm_autoload_json_helper),
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 可视化模式/JSON模式切换按钮
                    Button(
                        onClick = {
                            isVisualMode = !isVisualMode
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isVisualMode) stringResource(R.string.kpm_autoload_json_mode) else stringResource(R.string.kpm_autoload_visual_mode))
                    }

                    // 保存按钮
                    Button(
                        onClick = {
                            showSaveDialog.value = true
                        },
                        modifier = Modifier.weight(1f),
                        enabled = if (isVisualMode) kpmPathsList.isNotEmpty() else isValidJson
                    ) {
                        Text(stringResource(R.string.kpm_autoload_save))
                    }
                }
            }
        }
    }


}
