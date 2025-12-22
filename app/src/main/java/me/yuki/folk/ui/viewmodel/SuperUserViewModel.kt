package me.yuki.folk.ui.viewmodel

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.parcelize.Parcelize
import me.yuki.folk.APApplication
import me.yuki.folk.IAPRootService
import me.yuki.folk.Natives
import me.yuki.folk.apApp
import me.yuki.folk.services.RootServices
import me.yuki.folk.util.APatchCli
import me.yuki.folk.util.HanziToPinyin
import me.yuki.folk.util.PkgConfig
import java.text.Collator
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class SuperUserViewModel : ViewModel() {
    companion object {
        private const val TAG = "SuperUserViewModel"
        private var apps by mutableStateOf<List<AppInfo>>(emptyList())
    }

    @Parcelize
    data class AppInfo(
        val label: String, val packageInfo: PackageInfo, val config: PkgConfig.Config
    ) : Parcelable {
        val packageName: String
            get() = packageInfo.packageName
        val uid: Int
            get() = packageInfo.applicationInfo!!.uid
    }

    var search by mutableStateOf("")
    var showSystemApps by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)
        private set

    private val sortedList by derivedStateOf {
        val comparator = compareBy<AppInfo> {
            when {
                it.config.allow != 0 -> 0
                it.config.exclude == 1 -> 1
                else -> 2
            }
        }.then(compareBy(Collator.getInstance(Locale.getDefault()), AppInfo::label))
        apps.sortedWith(comparator).also {
            isRefreshing = false
        }
    }

    val appList by derivedStateOf {
        sortedList.filter {
            it.label.lowercase().contains(search.lowercase()) || it.packageName.lowercase()
                .contains(search.lowercase()) || HanziToPinyin.getInstance()
                .toPinyinString(it.label).contains(search.lowercase())
        }.filter {
            it.uid == 2000 // Always show shell
                    || showSystemApps || it.packageInfo.applicationInfo!!.flags.and(ApplicationInfo.FLAG_SYSTEM) == 0
        }
    }

    private suspend inline fun connectRootService(
        crossinline onDisconnect: () -> Unit = {}
    ): Pair<IBinder, ServiceConnection> = suspendCoroutine { continuation ->
        val connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.w(TAG, "onServiceDisconnected: $name")
                onDisconnect()
            }

            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                Log.i(TAG, "onServiceConnected: $name")
                if (binder != null) {
                    try {
                        continuation.resume(binder to this)
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Service connected but continuation already resumed", e)
                    }
                } else {
                    Log.e(TAG, "Service connected but binder is null")
                    // If binder is null, we can't really resume successfully, but we should unblock.
                    // However, normally binder is not null.
                }
            }
        }
        val intent = Intent(apApp, RootServices::class.java)
        
        Log.d(TAG, "Attempting to bind RootService. Shell isRoot: ${APatchCli.SHELL.isRoot}")
        Log.d(TAG, "Shell info: ${APatchCli.SHELL}")
        
        // Ensure binding happens on the main thread as required by Android's bindService
        val task = RootServices.bindOrTask(
            intent,
            Shell.EXECUTOR,
            connection,
        )
        
        if (task == null) {
            Log.e(TAG, "RootServices.bindOrTask returned null")
            continuation.resumeWithException(IllegalStateException("bindOrTask returned null"))
        } else {
            val shell = APatchCli.SHELL
            Log.d(TAG, "Executing bind task...")
            
            // Execute the binding task
            shell.execTask(task)
        }
    }

    fun excludeAll() {
        val modifiedConfigs = mutableListOf<PkgConfig.Config>()
        val currentApps = apps

        currentApps.forEach { app ->
            if ((app.packageInfo.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0) return@forEach
            if (app.config.allow == 0 && app.config.exclude == 0) {
                app.config.exclude = 1
                app.config.profile.scontext = APApplication.DEFAULT_SCONTEXT
                Natives.setUidExclude(app.uid, 1)
                modifiedConfigs.add(app.config)
            }
        }

        if (modifiedConfigs.isNotEmpty()) {
            PkgConfig.batchChangeConfigs(modifiedConfigs)
            // Force UI update
            apps = ArrayList(currentApps)
        }
    }

    fun reverseExcludeAll() {
        val modifiedConfigs = mutableListOf<PkgConfig.Config>()
        val currentApps = apps

        currentApps.forEach { app ->
            if ((app.packageInfo.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0) return@forEach
            if (app.config.allow == 0) {
                val newExclude = if (app.config.exclude == 1) 0 else 1
                app.config.exclude = newExclude
                if (newExclude == 1) {
                    app.config.profile.scontext = APApplication.DEFAULT_SCONTEXT
                }
                Natives.setUidExclude(app.uid, newExclude)
                modifiedConfigs.add(app.config)
            }
        }

        if (modifiedConfigs.isNotEmpty()) {
            PkgConfig.batchChangeConfigs(modifiedConfigs)
            // Force UI update
            apps = ArrayList(currentApps)
        }
    }

    private fun stopRootService() {
        val intent = Intent(apApp, RootServices::class.java)
        RootServices.stop(intent)
    }

    /**
     * Fallback method to get packages using PackageManager when RootService fails.
     * This is needed for devices where LibSU's RootServerMain can't initialize
     * (e.g., ONYX e-readers with modified frameworks).
     *
     * Note: This only gets packages for the current user, not all users.
     */
    private fun getPackagesViaPackageManager(): List<PackageInfo> {
        return try {
            val pm = apApp.packageManager
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            Log.e(TAG, "getPackagesViaPackageManager failed", e)
            emptyList()
        }
    }

    suspend fun fetchAppList() {
        isRefreshing = true

        val prefs = APApplication.sharedPreferences
        val loadingScheme = prefs.getString("app_list_loading_scheme", "root_service")

        // Try RootService with timeout, fallback to PackageManager if it fails
        val allPackages: List<PackageInfo> = withContext(Dispatchers.IO) {
            if (loadingScheme == "package_manager") {
                Log.i(TAG, "Using PackageManager to load app list (user preference)")
                getPackagesViaPackageManager()
            } else {
                Log.i(TAG, "Using RootService to load app list (user preference)")
                try {
                    // Use withTimeoutOrNull to avoid hanging forever if RootService fails to connect
                    val result = withTimeoutOrNull(10000L) {
                        withContext(Dispatchers.Main) {
                            connectRootService {
                                Log.w(TAG, "RootService disconnected")
                            }
                        }
                    }

                    if (result != null) {
                        val binder = result.first
                        val packages = IAPRootService.Stub.asInterface(binder).getPackages(0)
                        Log.i(TAG, "RootService connected and retrieved ${packages.list.size} packages")
                        withContext(Dispatchers.Main) {
                            stopRootService()
                        }
                        packages.list
                    } else {
                        Log.w(TAG, "RootService connection timed out, using PackageManager fallback")
                        getPackagesViaPackageManager()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "RootService failed: ${e.message}", e)
                    getPackagesViaPackageManager()
                }
            }
        }

        if (allPackages.isEmpty()) {
            Log.e(TAG, "Failed to get package list")
            isRefreshing = false
            return
        }

        withContext(Dispatchers.IO) {
            val uids = Natives.suUids().toList()
            Log.d(TAG, "all allows: $uids")

            var configs: HashMap<Int, PkgConfig.Config> = HashMap()
            thread {
                Natives.su()
                configs = PkgConfig.readConfigs()
            }.join()

            Log.d(TAG, "all configs: $configs")

            apps = allPackages.map {
                val appInfo = it.applicationInfo
                val uid = appInfo!!.uid
                val actProfile = if (uids.contains(uid)) Natives.suProfile(uid) else null
                val config = configs.getOrDefault(
                    uid, PkgConfig.Config(appInfo.packageName, Natives.isUidExcluded(uid), 0, Natives.Profile(uid = uid))
                )
                config.allow = 0

                // from kernel
                if (actProfile != null) {
                    config.allow = 1
                    config.profile = actProfile
                }
                AppInfo(
                    label = appInfo.loadLabel(apApp.packageManager).toString(),
                    packageInfo = it,
                    config = config
                )
            }.filter { it.packageName != apApp.packageName }
        }
    }
}
