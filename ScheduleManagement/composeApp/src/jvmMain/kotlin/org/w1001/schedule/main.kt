package org.w1001.schedule

import MainMenuV2
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.mongodb.MongoSocketException
import com.mongodb.MongoTimeoutException
import kotlinx.coroutines.launch
import org.w1001.schedule.components.WarningDialog

val viewModel = AppViewModel()
fun main() = application {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Window(
        onCloseRequest = { if (!viewModel.inMainMenu.value) showExitDialog = true else exitApplication() },
        title = "ScheduleManagement v${BuildConfig.VERSION}",
        state = WindowState(size = DpSize(1400.dp, 900.dp), placement = WindowPlacement.Floating),
    ) {
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Save and Exit") },
                text = { Text("Do you want to save before exiting?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    viewModel.saveDocument()
                                    exitApplication()
                                } catch (e: Exception) {
                                    errorMessage = when (e) {
                                        is MongoSocketException -> "No internet connection"
                                        is MongoTimeoutException -> "No internet connection"
                                        else -> e.message ?: "An unknown error occurred"
                                    }
                                    viewModel.isSaving = false
                                }
                            }
                        }
                    ) {
                        Text("Save and Exit")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exitApplication() }) {
                            Text("Exit without saving")
                        }
                        Button(onClick = { showExitDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }

        if (errorMessage != null) {
            WarningDialog(
                message = errorMessage!!,
                onDismiss = { errorMessage = null }
            )
        }

        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF825500),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFDDB3),
                onPrimaryContainer = Color(0xFF291800),
                secondary = Color(0xFF715B00),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFFE08B),
                onSecondaryContainer = Color(0xFF231B00),
                tertiary = Color(0xFF984061),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFFFD9E2),
                onTertiaryContainer = Color(0xFF3E001D),
                background = Color(0xFFFFFBFF),
                onBackground = Color(0xFF1F1B16),
                surface = Color(0xFFFFF8F5),
                onSurface = Color(0xFF1F1B16),
                surfaceVariant = Color(0xFFEFE0CF),
                onSurfaceVariant = Color(0xFF4F4539),
                outline = Color(0xFF817567)
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (viewModel.inMainMenu.value) {
                    MainMenuV2(viewModel)
                } else {
                    DocumentUI(viewModel)
                }
            }
        }
    }

//        if (secondWindowOpened) {
//            Window(
//                onCloseRequest = { secondWindowOpened = false },
//                title = "Second Window",
////                state = WindowState(size = DpSize(400.dp, 300.dp))
//            ) {
//                App()
//            }
//        }
}

