package me.bmax.apatch.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.bmax.apatch.R
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.layout.only

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    val showDialog = remember { mutableStateOf(true) }

    SuperDialog(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
        show = showDialog,
        title = stringResource(R.string.update_available_title),
        onDismissRequest = {
            // Cannot dismiss by default means - only through buttons
        },
        content = {
            Text(
                text = stringResource(R.string.update_available_message),
                style = MiuixTheme.textStyles.body2,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                TextButton(
                    text = stringResource(R.string.update_close),
                    onClick = {
                        onDismiss()
                        showDialog.value = false
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = stringResource(R.string.update_action),
                    onClick = {
                        onUpdate()
                        showDialog.value = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    )
}
