package me.bmax.apatch.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.content.SharedPreferences
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.generated.destinations.InstallScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ApmBulkInstallScreenDestination
import coil.Coil
import coil.ImageLoader
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.bmax.apatch.APApplication
import me.bmax.apatch.ui.screen.BottomBarDestination
import me.bmax.apatch.ui.screen.MODULE_TYPE
import me.bmax.apatch.ui.theme.APatchTheme
import me.bmax.apatch.ui.viewmodel.SuperUserViewModel
import me.bmax.apatch.ui.theme.APatchThemeWithBackground
import me.bmax.apatch.ui.theme.BackgroundConfig
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.MaterialTheme
import me.bmax.apatch.util.PermissionRequestHandler
import me.bmax.apatch.util.PermissionUtils
import me.bmax.apatch.util.ui.LocalSnackbarHost
import me.zhanghai.android.appiconloader.coil.AppIconFetcher
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.ui.window.DialogProperties
import me.bmax.apatch.R
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import me.zhanghai.android.appiconloader.coil.AppIconKeyer
import me.bmax.apatch.util.UpdateChecker
import me.bmax.apatch.ui.component.UpdateDialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import android.provider.OpenableColumns
import me.bmax.apatch.ui.theme.ThemeManager
import me.bmax.apatch.ui.component.rememberLoadingDialog
import android.widget.Toast

import me.bmax.apatch.ui.screen.ThemeImportDialog

class MainActivity : AppCompatActivity() {

    private var isLoading = true
    private var installUri: Uri? = null
    private var installUris: ArrayList<Uri>? = null
    private lateinit var permissionHandler: PermissionRequestHandler
    private val isLocked = mutableStateOf(false)

    private fun getFileName(context: android.content.Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown"
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(me.bmax.apatch.util.DPIUtils.updateContext(newBase))
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().setKeepOnScreenCondition { isLoading }

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)
        
        installUri = if (intent.action == Intent.ACTION_SEND) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
        } else {
            intent.data ?: run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra("uris", Uri::class.java)?.firstOrNull()
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>("uris")?.firstOrNull()
                }
            }
        }

        if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            installUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            }
        }

        // 初始化权限处理器
        permissionHandler = PermissionRequestHandler(this)

        val prefs = APApplication.sharedPreferences
        val biometricLogin = prefs.getBoolean("biometric_login", false)
        val biometricManager = androidx.biometric.BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS

        if (biometricLogin && canAuthenticate) {
            isLocked.value = true
            val biometricPrompt = androidx.biometric.BiometricPrompt(
                this,
                androidx.core.content.ContextCompat.getMainExecutor(this),
                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        android.widget.Toast.makeText(this@MainActivity, errString, android.widget.Toast.LENGTH_SHORT).show()
                        finishAndRemoveTask()
                    }

                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        isLocked.value = false
                    }
                })
            val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.action_biometric))
                .setSubtitle(getString(R.string.msg_biometric))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()
            biometricPrompt.authenticate(promptInfo)
        }
        setupUI()
    }

    private fun setupUI() {
        
        // Load DPI settings
        me.bmax.apatch.util.DPIUtils.load(this)
        me.bmax.apatch.util.DPIUtils.applyDpi(this)
        
        // 检查并请求权限
        if (!PermissionUtils.hasExternalStoragePermission(this) || 
            !PermissionUtils.hasWriteExternalStoragePermission(this)) {
            permissionHandler.requestPermissions(
                onGranted = {
                    // 权限已授予
                },
                onDenied = {
                    // 权限被拒绝，可以显示一个提示
                }
            )
        }

        setContent {
            val locked by remember { isLocked }
            if (locked) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            } else {
            val prefs = APApplication.sharedPreferences
            var folkXEngineEnabled by remember {
                mutableStateOf(prefs.getBoolean("folkx_engine_enabled", true))
            }
            var folkXAnimationType by remember {
                mutableStateOf(prefs.getString("folkx_animation_type", "linear"))
            }
            var folkXAnimationSpeed by remember {
                mutableStateOf(prefs.getFloat("folkx_animation_speed", 1.0f))
            }

            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key == "folkx_engine_enabled") {
                        folkXEngineEnabled = sharedPreferences.getBoolean("folkx_engine_enabled", true)
                    }
                    if (key == "folkx_animation_type") {
                        folkXAnimationType = sharedPreferences.getString("folkx_animation_type", "linear")
                    }
                    if (key == "folkx_animation_speed") {
                        folkXAnimationSpeed = sharedPreferences.getFloat("folkx_animation_speed", 1.0f)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            val navController = rememberNavController()
            val navigator = navController.rememberDestinationsNavigator()
            val snackBarHostState = remember { SnackbarHostState() }
            val bottomBarRoutes = remember {
                BottomBarDestination.entries.map { it.direction.route }.toSet()
            }

            LaunchedEffect(Unit) {
                if (SuperUserViewModel.apps.isEmpty()) {
                    SuperUserViewModel().fetchAppList()
                }
            }

            APatchThemeWithBackground(navController = navController) {
                
                val showUpdateDialog = remember { mutableStateOf(false) }
                val context = LocalContext.current

                val loadingDialog = rememberLoadingDialog()
                val showThemeImportDialog = remember { mutableStateOf(false) }
                val themeImportUri = remember { mutableStateOf<Uri?>(null) }
                val themeImportMetadata = remember { mutableStateOf<ThemeManager.ThemeMetadata?>(null) }
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                
                val uri = installUri
                val uris = installUris
                LaunchedEffect(Unit) {
                    if (uris != null && uris.isNotEmpty()) {
                        navigator.navigate(ApmBulkInstallScreenDestination(initialUris = uris))
                    } else if (uri != null) {
                         val fileName = withContext(Dispatchers.IO) {
                            getFileName(context, uri)
                        }
                        if (fileName.endsWith(".fpt", ignoreCase = true)) {
                            themeImportUri.value = uri
                            scope.launch {
                                loadingDialog.show()
                                val metadata = ThemeManager.readThemeMetadata(context, uri)
                                loadingDialog.hide()
                                if (metadata != null) {
                                    themeImportMetadata.value = metadata
                                    showThemeImportDialog.value = true
                                } else {
                                    Toast.makeText(context, context.getString(R.string.settings_theme_import_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            navigator.navigate(InstallScreenDestination(uri, MODULE_TYPE.APM))
                        }
                    }
                }

                if (showThemeImportDialog.value && themeImportMetadata.value != null) {
                    ThemeImportDialog(
                        showDialog = showThemeImportDialog,
                        metadata = themeImportMetadata.value!!,
                        onConfirm = {
                            scope.launch {
                                val success = loadingDialog.withLoading {
                                    ThemeManager.importTheme(context, themeImportUri.value!!)
                                }
                                if (success) {
                                    Toast.makeText(context, context.getString(R.string.settings_theme_imported), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.settings_theme_import_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
                
                LaunchedEffect(Unit) {
                    if (prefs.getBoolean("auto_update_check", true)) {
                        withContext(Dispatchers.IO) {
                             // Delay a bit to wait for network connection
                             kotlinx.coroutines.delay(2000)
                             val hasUpdate = me.bmax.apatch.util.UpdateChecker.checkUpdate()
                             if (hasUpdate) {
                                 showUpdateDialog.value = true
                             }
                        }
                    }
                }

                if (showUpdateDialog.value) {
                    UpdateDialog(
                        onDismiss = { showUpdateDialog.value = false },
                        onUpdate = {
                            showUpdateDialog.value = false
                            UpdateChecker.openUpdateUrl(context)
                        }
                    )
                }

                Scaffold(
                    bottomBar = { BottomBar(navController) }
                ) { _ ->
                    CompositionLocalProvider(
                        LocalSnackbarHost provides snackBarHostState,
                    ) {
                        DestinationsNavHost(
                            modifier = Modifier.padding(bottom = 80.dp),
                            navGraph = NavGraphs.root,
                            navController = navController,
                            engine = rememberNavHostEngine(navHostContentAlignment = Alignment.TopCenter),
                            defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                                override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                                    {
                                        // If the target is a detail page (not a bottom navigation page), slide in from the right
                                        if (targetState.destination.route !in bottomBarRoutes) {
                                            slideInHorizontally(initialOffsetX = { it })
                                        } else {
                                            // Otherwise (switching between bottom navigation pages)
                                            if (folkXEngineEnabled) {
                                                val initialRoute = initialState.destination.route
                                                val targetRoute = targetState.destination.route
                                                val initialIndex = BottomBarDestination.entries.indexOfFirst { it.direction.route == initialRoute }
                                                val targetIndex = BottomBarDestination.entries.indexOfFirst { it.direction.route == targetRoute }

                                                val stiffness = 300f * folkXAnimationSpeed * folkXAnimationSpeed
                                                val duration300 = (300 / folkXAnimationSpeed).toInt()
                                                val duration600 = (600 / folkXAnimationSpeed).toInt()

                                                if (initialIndex != -1 && targetIndex != -1) {
                                                    when (folkXAnimationType) {
                                                        "spatial" -> {
                                                            if (targetIndex > initialIndex) {
                                                                scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness)) + fadeIn(animationSpec = tween(duration300))
                                                            } else {
                                                                scaleIn(initialScale = 1.1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness)) + fadeIn(animationSpec = tween(duration300))
                                                            }
                                                        }
                                                        "fade" -> {
                                                            fadeIn(animationSpec = tween(duration300))
                                                        }
                                                        "vertical" -> {
                                                            if (targetIndex > initialIndex) {
                                                                slideInVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetY = { height: Int -> height }) + fadeIn()
                                                            } else {
                                                                slideInVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetY = { height: Int -> -height }) + fadeIn()
                                                            }
                                                        }
                                                        "diagonal" -> {
                                                            if (targetIndex > initialIndex) {
                                                                slideInHorizontally(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetX = { width: Int -> width }) +
                                                                slideInVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetY = { height: Int -> height }) + fadeIn()
                                                            } else {
                                                                slideInHorizontally(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetX = { width: Int -> -width }) +
                                                                slideInVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetY = { height: Int -> -height }) + fadeIn()
                                                            }
                                                        }
                                                        else -> {
                                                            if (targetIndex > initialIndex) {
                                                                slideInHorizontally(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetX = { width: Int -> width })
                                                            } else {
                                                                slideInHorizontally(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), initialOffsetX = { width: Int -> -width })
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    fadeIn(animationSpec = tween(340))
                                                }
                                            } else {
                                                fadeIn(animationSpec = tween(340))
                                            }
                                        }
                                    }

                                override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                                    {
                                        // If navigating from the home page (bottom navigation page) to a detail page, slide out to the left
                                        if (initialState.destination.route in bottomBarRoutes && targetState.destination.route !in bottomBarRoutes) {
                                            slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
                                        } else {
                                            // Otherwise (switching between bottom navigation pages)
                                            if (folkXEngineEnabled && initialState.destination.route in bottomBarRoutes && targetState.destination.route in bottomBarRoutes) {
                                                val initialRoute = initialState.destination.route
                                                val targetRoute = targetState.destination.route
                                                val initialIndex = BottomBarDestination.entries.indexOfFirst { it.direction.route == initialRoute }
                                                val targetIndex = BottomBarDestination.entries.indexOfFirst { it.direction.route == targetRoute }

                                                val stiffness = 300f * folkXAnimationSpeed * folkXAnimationSpeed
                                                val duration300 = (300 / folkXAnimationSpeed).toInt()
                                                val duration600 = (600 / folkXAnimationSpeed).toInt()

                                                if (initialIndex != -1 && targetIndex != -1) {
                                                    when (folkXAnimationType) {
                                                        "spatial" -> {
                                                            if (targetIndex > initialIndex) {
                                                                scaleOut(targetScale = 1.1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness)) + fadeOut(animationSpec = tween(duration300))
                                                            } else {
                                                                scaleOut(targetScale = 0.9f, animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness)) + fadeOut(animationSpec = tween(duration300))
                                                            }
                                                        }
                                                        "fade" -> {
                                                            fadeOut(animationSpec = tween(duration600))
                                                        }
                                                        "vertical" -> {
                                                            if (targetIndex > initialIndex) {
                                                                slideOutVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), targetOffsetY = { height -> -height }) + fadeOut()
                                                            } else {
                                                                slideOutVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), targetOffsetY = { height -> height }) + fadeOut()
                                                            }
                                                        }
                                                        "diagonal" -> {
                                                            if (targetIndex > initialIndex) {
                                                                slideOutHorizontally(animationSpec = tween(duration600), targetOffsetX = { width -> -width }) +
                                                                slideOutVertically(animationSpec = tween(duration600), targetOffsetY = { height -> -height }) + fadeOut(animationSpec = tween(duration600))
                                                            } else {
                                                                slideOutHorizontally(animationSpec = tween(duration600), targetOffsetX = { width -> width }) +
                                                                slideOutVertically(animationSpec = tween(duration600), targetOffsetY = { height -> height }) + fadeOut(animationSpec = tween(duration600))
                                                            }
                                                        }
                                                        else -> {
                                                            if (targetIndex > initialIndex) {
                                                                slideOutHorizontally(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), targetOffsetX = { width -> -width })
                                                            } else {
                                                                slideOutHorizontally(animationSpec = spring(dampingRatio = 0.8f, stiffness = stiffness), targetOffsetX = { width -> width })
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    fadeOut(animationSpec = tween(340))
                                                }
                                            } else {
                                                fadeOut(animationSpec = tween(340))
                                            }
                                        }
                                    }

                                override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                                    {
                                        // If returning to the home page (bottom navigation page), slide in from the left
                                        if (targetState.destination.route in bottomBarRoutes) {
                                            slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
                                        } else {
                                            // Otherwise (e.g., returning between multiple detail pages), use default fade in
                                            fadeIn(animationSpec = tween(340))
                                        }
                                    }

                                override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                                    {
                                        // If returning from a detail page (not a bottom navigation page), scale down and fade out
                                        if (initialState.destination.route !in bottomBarRoutes) {
                                            scaleOut(targetScale = 0.9f) + fadeOut()
                                        } else {
                                            // Otherwise, use default fade out
                                            fadeOut(animationSpec = tween(340))
                                        }
                                    }
                            }
                        )
                    }
                }
            }
        }
        }

        // Initialize Coil
        val iconSize = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components {
                    add(AppIconKeyer())
                    add(AppIconFetcher.Factory(iconSize, false, this@MainActivity))
                }
                .build()
        )

        isLoading = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnofficialVersionDialog() {
    val uriHandler = LocalUriHandler.current
    
    // 6秒后强制退出
    LaunchedEffect(Unit) {
        delay(3000)
        exitProcess(0)
    }

    BasicAlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.unofficial_version_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.unofficial_version_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(
                        onClick = {
                            uriHandler.openUri("https://github.com/matsuzaka-yuki/FolkPatch")
                        }
                    ) {
                        Text(stringResource(R.string.go_to_github))
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    if (!APApplication.isSignatureValid) {
        UnofficialVersionDialog()
    }
    val state by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val navigator = navController.rememberDestinationsNavigator()

    val prefs = APApplication.sharedPreferences
    var showNavApm by remember { mutableStateOf(prefs.getBoolean("show_nav_apm", true)) }
    var showNavKpm by remember { mutableStateOf(prefs.getBoolean("show_nav_kpm", true)) }
    var showNavSuperUser by remember { mutableStateOf(prefs.getBoolean("show_nav_superuser", true)) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "show_nav_apm" -> showNavApm = sharedPrefs.getBoolean(key, true)
                "show_nav_kpm" -> showNavKpm = sharedPrefs.getBoolean(key, true)
                "show_nav_superuser" -> showNavSuperUser = sharedPrefs.getBoolean(key, true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Crossfade(
        targetState = state,
        label = "BottomBarStateCrossfade"
    ) { state ->
        val kPatchReady = state != APApplication.State.UNKNOWN_STATE
        val aPatchReady = state == APApplication.State.ANDROIDPATCH_INSTALLED

        NavigationBar(
            tonalElevation = if (BackgroundConfig.isCustomBackgroundEnabled) 0.dp else 8.dp,
            containerColor = if (BackgroundConfig.isCustomBackgroundEnabled) {
                MaterialTheme.colorScheme.surface.copy(alpha = BackgroundConfig.customBackgroundOpacity)
            } else {
                NavigationBarDefaults.containerColor
            }
        ) {
            BottomBarDestination.entries.forEach { destination ->
                val show = when {
                    destination == BottomBarDestination.AModule && !showNavApm -> false
                    destination == BottomBarDestination.KModule && !showNavKpm -> false
                    destination == BottomBarDestination.SuperUser && !showNavSuperUser -> false
                    (destination.kPatchRequired && !kPatchReady) || (destination.aPatchRequired && !aPatchReady) -> false
                    else -> true
                }

                if (show) {
                    key(destination) {
                        val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)

                        NavigationBarItem(
                            selected = isCurrentDestOnBackStack,
                            onClick = {
                                if (isCurrentDestOnBackStack) {
                                    navigator.popBackStack(destination.direction, false)
                                }
                                navigator.navigate(destination.direction) {
                                    popUpTo(NavGraphs.root) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (isCurrentDestOnBackStack) {
                                    Icon(destination.iconSelected, stringResource(destination.label))
                                } else {
                                    Icon(destination.iconNotSelected, stringResource(destination.label))
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(destination.label),
                                    overflow = TextOverflow.Visible,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        }
    }
}
