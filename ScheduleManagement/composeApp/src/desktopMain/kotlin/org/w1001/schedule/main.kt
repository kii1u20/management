package org.w1001.schedule

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

val viewModel = AppViewModel()
fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "ScheduleManagement",
        state = WindowState(size = DpSize(800.dp, 900.dp), placement = WindowPlacement.Floating),
    ) {
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

