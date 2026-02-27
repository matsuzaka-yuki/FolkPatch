package me.bmax.apatch.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SearchAppBar
import me.bmax.apatch.ui.screen.settings.*
import me.bmax.apatch.util.APatchKeyHelper
import me.bmax.apatch.util.getSELinuxMode
import me.bmax.apatch.util.isGlobalNamespaceEnabled as checkGlobalNamespaceEnabled
import me.bmax.apatch.util.isMagicMountEnabled as checkMagicMountEnabled
import me.bmax.apatch.util.isHideServiceEnabled as checkHideServiceEnabled
import me.bmax.apatch.util.ui.LocalSnackbarHost

import com.ramcosta.composedestinations.generated.destinations.ApiMarketplaceScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ThemeStoreScreenDestination
import com.ramcosta.composedestinations.generated.destinations.UmountConfigScreenDestination
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape

@Destination<RootGraph>
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingScreen(navigator: DestinationsNavigator) {
    val state by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = state != APApplication.State.UNKNOWN_STATE
    val aPatchReady =
        (state == APApplication.State.ANDROIDPATCH_INSTALLING || state == APApplication.State.ANDROIDPATCH_INSTALLED || state == APApplication.State.ANDROIDPATCH_NEED_UPDATE)

    var showDevDialog by rememberSaveable { mutableStateOf(false) }
    var isGlobalNamespaceEnabled by rememberSaveable { mutableStateOf(false) }
    var isMagicMountEnabled by rememberSaveable { mutableStateOf(false) }
    var isHideServiceEnabled by rememberSaveable { mutableStateOf(false) }
    var currentSELinuxMode by rememberSaveable { mutableStateOf("Unknown") }
    var searchText by rememberSaveable { mutableStateOf("") }

    // Auto Backup Module State (Lifted)
    val prefs = APApplication.sharedPreferences
    var autoBackupModule by rememberSaveable { mutableStateOf(prefs.getBoolean("auto_backup_module", false)) }

    LaunchedEffect(kPatchReady, aPatchReady) {
        if (kPatchReady && aPatchReady) {
            withContext(Dispatchers.IO) {
                val globalNamespace = checkGlobalNamespaceEnabled()
                val magicMount = checkMagicMountEnabled()
                val hideService = checkHideServiceEnabled()
                val seLinux = getSELinuxMode()
                isGlobalNamespaceEnabled = globalNamespace
                isMagicMountEnabled = magicMount
                isHideServiceEnabled = hideService
                currentSELinuxMode = seLinux
            }
        }
    }

    val snackBarHost = LocalSnackbarHost.current

    DeveloperInfo(
        showDialog = showDevDialog
    ) {
        showDevDialog = false
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.settings)) },
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onClearClick = { searchText = "" },
                dropdownContent = {},
                trailingActions = {
                    IconButton(
                        modifier = Modifier.padding(end = 5.dp),
                        onClick = {
                            showDevDialog = true
                        }
                    ) {
                        Icon(Icons.Outlined.Info, null)
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackBarHost) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            GeneralSettings(
                searchText = searchText,
                kPatchReady = kPatchReady,
                aPatchReady = aPatchReady,
                currentSELinuxMode = currentSELinuxMode,
                onSELinuxModeChange = { currentSELinuxMode = it },
                isGlobalNamespaceEnabled = isGlobalNamespaceEnabled,
                onGlobalNamespaceChange = { isGlobalNamespaceEnabled = it },
                isMagicMountEnabled = isMagicMountEnabled,
                onMagicMountChange = { isMagicMountEnabled = it },
                isHideServiceEnabled = isHideServiceEnabled,
                onHideServiceChange = { isHideServiceEnabled = it },
                snackBarHost = snackBarHost,
                onNavigateToUmountConfig = {
                    navigator.navigate(UmountConfigScreenDestination)
                }
            )

            AppearanceSettings(
                searchText = searchText,
                snackBarHost = snackBarHost,
                kPatchReady = kPatchReady,
                onNavigateToThemeStore = {
                    navigator.navigate(ThemeStoreScreenDestination)
                },
                onNavigateToApiMarketplace = {
                    navigator.navigate(ApiMarketplaceScreenDestination)
                }
            )

            BehaviorSettings(
                searchText = searchText,
                kPatchReady = kPatchReady,
                aPatchReady = aPatchReady
            )

            SecuritySettings(
                searchText = searchText,
                snackBarHost = snackBarHost,
                kPatchReady = kPatchReady
            )

            if (aPatchReady) {
                BackupSettings(
                    searchText = searchText,
                    autoBackupModule = autoBackupModule,
                    onAutoBackupModuleChange = { autoBackupModule = it }
                )
            }


            if (aPatchReady) {
                ModuleSettings(
                    searchText = searchText,
                    aPatchReady = aPatchReady
                )
            }

            MultimediaSettings(
                searchText = searchText,
                snackBarHost = snackBarHost
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperInfo(
    showDialog: Boolean,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/matsuzaka-yuki/FolkPatch"
    val telegramUrl = "https://t.me/FolkPatch"
    val sociabuzzUrl = "https://afdian.com/a/matsuzaka_yuki"

    if (showDialog) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF303030)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("http://q.qlogo.cn/headimg_dl?dst_uin=3231515355&spec=640&img_type=jpg")
                            .crossfade(true)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Developer Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF303030))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Matsuzaka Yuki",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.developer_and_maintainer),
                    fontSize = 15.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "\"美しい世界を見てきましょう\"",
                    textAlign = TextAlign.Center,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilledTonalButton(
                        onClick = { uriHandler.openUri(githubUrl) },
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.github),
                            contentDescription = stringResource(R.string.github),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.github))
                    }

                    FilledTonalButton(
                        onClick = { uriHandler.openUri(telegramUrl) },
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.telegram),
                            contentDescription = stringResource(R.string.telegram),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.telegram))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = { uriHandler.openUri(sociabuzzUrl) },
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Coffee,
                        contentDescription = stringResource(R.string.support_or_donate),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.support_or_donate))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
