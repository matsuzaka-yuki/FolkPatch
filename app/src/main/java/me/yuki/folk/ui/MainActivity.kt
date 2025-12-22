package me.yuki.folk.ui

import android.annotation.SuppressLint
import android.os.Build
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
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
import coil.Coil
import coil.ImageLoader
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.yuki.folk.APApplication
import me.yuki.folk.ui.screen.BottomBarDestination
import me.yuki.folk.ui.theme.APatchTheme
import me.yuki.folk.ui.theme.APatchThemeWithBackground
import me.yuki.folk.ui.theme.BackgroundConfig
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.MaterialTheme
import me.yuki.folk.util.PermissionRequestHandler
import me.yuki.folk.util.PermissionUtils
import me.yuki.folk.util.ui.LocalSnackbarHost
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.window.DialogProperties
import me.yuki.folk.R
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess
import me.zhanghai.android.appiconloader.coil.AppIconKeyer
import me.yuki.folk.util.UpdateChecker
import me.yuki.folk.ui.component.UpdateDialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext

class MainActivity : AppCompatActivity() {

    private var isLoading = true
    private lateinit var permissionHandler: PermissionRequestHandler

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(me.yuki.folk.util.DPIUtils.updateContext(newBase))
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().setKeepOnScreenCondition { isLoading }

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

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
                        setupUI()
                    }
                })
            val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.action_biometric))
                .setSubtitle(getString(R.string.msg_biometric))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()
            biometricPrompt.authenticate(promptInfo)
        } else {
            setupUI()
        }
    }

    private fun setupUI() {
        
        // Load DPI settings
        me.yuki.folk.util.DPIUtils.load(this)
        me.yuki.folk.util.DPIUtils.applyDpi(this)
        
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
            val snackBarHostState = remember { SnackbarHostState() }
            val bottomBarRoutes = remember {
                BottomBarDestination.entries.map { it.direction.route }.toSet()
            }

            APatchThemeWithBackground(navController = navController) {
                
                val showUpdateDialog = remember { mutableStateOf(false) }
                val context = LocalContext.current
                
                LaunchedEffect(Unit) {
                    if (prefs.getBoolean("auto_update_check", true)) {
                        withContext(Dispatchers.IO) {
                             // Delay a bit to wait for network connection
                             kotlinx.coroutines.delay(2000)
                             val hasUpdate = me.yuki.folk.util.UpdateChecker.checkUpdate()
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
                val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)

                val hideDestination = (destination.kPatchRequired && !kPatchReady) || (destination.aPatchRequired && !aPatchReady)
                if (hideDestination) return@forEach

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
