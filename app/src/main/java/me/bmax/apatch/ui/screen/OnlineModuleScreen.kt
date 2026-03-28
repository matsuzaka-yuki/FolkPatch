package me.bmax.apatch.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.generated.destinations.InstallScreenDestination
import me.bmax.apatch.R
import me.bmax.apatch.ui.viewmodel.OnlineModuleViewModel
import me.bmax.apatch.util.download
import me.bmax.apatch.util.DownloadListener
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Scaffold
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Destination<RootGraph>
@Composable
fun OnlineAPMModuleScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    OnlineModuleScreen(
        navigator = navigator,
        moduleType = "apm",
        title = context.getString(R.string.online_apm_module_title)
    )
}

@Destination<RootGraph>
@Composable
fun OnlineKPMModuleScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    OnlineModuleScreen(
        navigator = navigator,
        moduleType = "kpm",
        title = context.getString(R.string.online_kpm_module_title)
    )
}

@Composable
fun OnlineModuleScreen(
    navigator: DestinationsNavigator,
    moduleType: String,
    title: String
) {
    val viewModel = viewModel<OnlineModuleViewModel> {
        OnlineModuleViewModel(moduleType)
    }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (viewModel.modules.isEmpty()) {
            viewModel.fetchModules()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = { navigator.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            InputField(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                    if (!it) viewModel.onSearchQueryChange("")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (viewModel.isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (viewModel.modules.isEmpty()) {
                    Text(
                        text = stringResource(R.string.online_module_empty),
                        modifier = Modifier.align(Alignment.Center),
                        style = MiuixTheme.textStyles.body2
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.modules) { module ->
                            OnlineModuleItem(
                                module = module,
                                context = context,
                                showArgs = moduleType == "kpm"
                            )
                        }
                    }
                }
            }
        }

        // Only APM modules need to navigate to install screen after download
        if (moduleType == "apm") {
            DownloadListener(context) { uri ->
                navigator.navigate(InstallScreenDestination(uri, MODULE_TYPE.APM))
            }
        }
    }
}

@Composable
fun OnlineModuleItem(
    module: OnlineModuleViewModel.OnlineModule,
    context: Context,
    showArgs: Boolean = false
) {
    val downloadStartText = stringResource(R.string.online_module_download_start, module.name)
    val downloadNotificationText = stringResource(R.string.online_module_download_notification, module.name)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = module.name,
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version: ${module.version}",
                    style = MiuixTheme.textStyles.body2
                )
                if (showArgs && module.parameter.isNotEmpty()) {
                    val parameterText = when (module.parameter) {
                        "1" -> "Control"
                        "0" -> "NoControl"
                        else -> module.parameter
                    }
                    Text(
                        text = "Args: $parameterText",
                        style = MiuixTheme.textStyles.body2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = module.description,
                    style = MiuixTheme.textStyles.body2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    Toast.makeText(context, downloadNotificationText, Toast.LENGTH_LONG).show()

                    download(
                        context = context,
                        url = module.url,
                        fileName = "${module.name}-${module.version}.zip",
                        description = downloadStartText,
                        onDownloading = {},
                        onDownloaded = {}
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = "Download"
                )
            }
        }
    }
}
