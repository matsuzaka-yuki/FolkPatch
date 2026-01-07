package me.bmax.apatch.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.bmax.apatch.R
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.layout.only
import kotlin.system.exitProcess

@Composable
fun SignatureVerifyDialog() {
    val showDialog = remember { mutableStateOf(true) }
    val uriHandler = LocalUriHandler.current

    // 3秒后强制退出
    LaunchedEffect(Unit) {
        delay(3000)
        exitProcess(0)
    }

    SuperDialog(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
        show = showDialog,
        title = stringResource(R.string.unofficial_version_title),
        onDismissRequest = {
            // Cannot dismiss
        },
        content = {
            Text(
                text = stringResource(R.string.unofficial_version_message),
                style = MiuixTheme.textStyles.body2,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            TextButton(
                text = stringResource(R.string.go_to_github),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    uriHandler.openUri("https://github.com/matsuzaka-yuki/FolkPatch/releases/tag/lite")
                }
            )
        }
    )
}


