package org.w1001.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking

val viewModel = AppViewModel()
fun main() = application {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    Window(
        onCloseRequest = {
            runBlocking {
                try {
                    viewModel.saveDocument()
                    exitApplication()
                } catch (e: Exception) {
                    errorMessage = "Error saving document: ${e.message}"
                    println("Error saving document: ${e.message}")
                }
            }
//            exitApplication()
        },
        title = "ScheduleManagement",
        state = WindowState(size = DpSize(800.dp, 900.dp), placement = WindowPlacement.Floating),
    ) {
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

