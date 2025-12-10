package me.bmax.apatch.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.bmax.apatch.R
import me.bmax.apatch.ui.viewmodel.ThemeStoreViewModel

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStoreScreen(
    navigator: DestinationsNavigator
) {
    val viewModel = viewModel<ThemeStoreViewModel>()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTheme by remember { mutableStateOf<ThemeStoreViewModel.RemoteTheme?>(null) }

    LaunchedEffect(Unit) {
        if (viewModel.themes.isEmpty()) {
            viewModel.fetchThemes()
        }
    }

    if (selectedTheme != null) {
        val theme = selectedTheme!!
        val typeString = if (theme.type == "tablet") stringResource(R.string.theme_type_tablet) else stringResource(R.string.theme_type_phone)
        val sourceString = if (theme.source == "official") stringResource(R.string.theme_source_official) else stringResource(R.string.theme_source_third_party)

        AlertDialog(
            onDismissRequest = { selectedTheme = null },
            title = { Text(text = theme.name) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.theme_store_author, theme.author),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.theme_store_version, theme.version),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${stringResource(R.string.theme_type)}: $typeString",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${stringResource(R.string.theme_source)}: $sourceString",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(theme.downloadUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.theme_store_download_failed))
                            }
                        }
                        selectedTheme = null
                    }
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.theme_store_download))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.theme_store_title)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (viewModel.isRefreshing) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (viewModel.errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { viewModel.fetchThemes() }) {
                    Text("Retry")
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                // Use 128.dp to ensure at least 2 columns on small phones (320dp+)
                columns = StaggeredGridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.themes) { theme ->
                    ThemeGridItem(
                        theme = theme,
                        onClick = { selectedTheme = theme }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeGridItem(
    theme: ThemeStoreViewModel.RemoteTheme,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Only display cover as requested
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(theme.previewUrl)
                .crossfade(true)
                .build(),
            contentDescription = theme.name,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
    }
}
