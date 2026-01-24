package me.bmax.apatch.ui.screen

import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.R
import me.bmax.apatch.data.ScriptInfo
import me.bmax.apatch.util.ScriptLibraryManager
import me.bmax.apatch.util.ui.LocalSnackbarHost
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ScriptExecutionLogScreen(
    navigator: DestinationsNavigator,
    scriptInfo: ScriptInfo
) {
    var text by remember { mutableStateOf("") }
    val displayBuffer = remember { StringBuffer() }
    val fullLogBuffer = remember { StringBuffer() }

    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val updaterJob = launch {
            while (true) {
                delay(100)
                val newText = displayBuffer.toString()
                if (text.length != newText.length) {
                    text = newText
                }
            }
        }

        withContext(Dispatchers.IO) {
            ScriptLibraryManager.executeScriptWithCallbacks(
                scriptInfo,
                onStdout = {
                    val tempText = "$it\n"
                    if (tempText.startsWith("[H[J")) {
                        displayBuffer.setLength(0)
                        displayBuffer.append(tempText.substring(6))
                    } else {
                        displayBuffer.append(tempText)
                    }
                    fullLogBuffer.append(it).append("\n")
                },
                onStderr = {
                    val tempText = "$it\n"
                    if (tempText.startsWith("[H[J")) {
                        displayBuffer.setLength(0)
                        displayBuffer.append(tempText.substring(6))
                    } else {
                        displayBuffer.append(tempText)
                    }
                    fullLogBuffer.append(it).append("\n")
                }
            )
        }

        updaterJob.cancel()
        val finalText = displayBuffer.toString()
        if (text.length != finalText.length) {
            text = finalText
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.script_library_output)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.popBackStack() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
                                    val date = format.format(Date())
                                    val file = File(
                                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                        "FolkPatch/${scriptInfo.alias}_${date}.log"
                                    )
                                    file.writeText(fullLogBuffer.toString())
                                    snackBarHost.showSnackbar("Log saved to ${file.absolutePath}")
                                } catch (e: Exception) {
                                    snackBarHost.showSnackbar("Failed to save log: ${e.message}")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.script_library_save_log)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHost) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize(1f)
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            LaunchedEffect(text) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Text(
                modifier = Modifier.padding(8.dp),
                text = text,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                fontFamily = FontFamily.Monospace,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
            )
        }
    }
}
