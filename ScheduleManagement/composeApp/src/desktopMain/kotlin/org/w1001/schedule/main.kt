package org.w1001.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import org.w1001.schedule.components.WarningDialog
import org.w1001.schedule.mainViews.App
import org.w1001.schedule.mainViews.MainMenu

val viewModel = AppViewModel()
fun main() = application {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Window(
        onCloseRequest = { showExitDialog = true },
        title = "ScheduleManagement",
        state = WindowState(size = DpSize(800.dp, 900.dp), placement = WindowPlacement.Floating),
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
                                    errorMessage = "Error saving document: ${e.message}"
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

        if (viewModel.inMainMenu.value) {
            MainMenu(viewModel)
        } else {
            App(viewModel)
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

