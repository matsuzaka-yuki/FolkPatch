package me.bmax.apatch.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.extra.SuperSwitch

@Destination<RootGraph>
@Composable
fun NavigationLayoutScreen(navigator: DestinationsNavigator) {
    val prefs = APApplication.sharedPreferences

    var showNavApm by rememberSaveable {
        mutableStateOf(prefs.getBoolean("show_nav_apm", true))
    }
    var showNavKpm by rememberSaveable {
        mutableStateOf(prefs.getBoolean("show_nav_kpm", true))
    }
    var showNavSuperUser by rememberSaveable {
        mutableStateOf(prefs.getBoolean("show_nav_superuser", true))
    }

    Scaffold(
        topBar = {
            TopBar(onBack = dropUnlessResumed { navigator.popBackStack() })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Show APM
                SuperSwitch(
                    title = stringResource(id = R.string.settings_show_apm),
                    checked = showNavApm,
                    onCheckedChange = {
                        prefs.edit { putBoolean("show_nav_apm", it) }
                        showNavApm = it
                    }
                )

                // Show KPM
                SuperSwitch(
                    title = stringResource(id = R.string.settings_show_kpm),
                    checked = showNavKpm,
                    onCheckedChange = {
                        prefs.edit { putBoolean("show_nav_kpm", it) }
                        showNavKpm = it
                    }
                )

                // Show SuperUser
                SuperSwitch(
                    title = stringResource(id = R.string.settings_show_superuser),
                    checked = showNavSuperUser,
                    onCheckedChange = {
                        prefs.edit { putBoolean("show_nav_superuser", it) }
                        showNavSuperUser = it
                    }
                )
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit = {}) {
    SmallTopAppBar(
        title = stringResource(R.string.settings_nav_layout_title),
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        },
    )
}
