package org.w1001.schedule

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

val inMainMenu: MutableState<Boolean> = mutableStateOf(true)
val numberOfColumns: MutableState<String> = mutableStateOf("3")
val workTimeType: MutableState<Int> = mutableStateOf(1)
val columnNames = mutableStateListOf<MutableState<String>>().apply {
    for (i in 0 until numberOfColumns.value.toInt()) {
        add(mutableStateOf("Column ${i + 1}"))

    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ScheduleManagement",
        state = WindowState(size = DpSize(800.dp, 900.dp)),
    ) {
        if (inMainMenu.value) {
            MainMenu(inMainMenu, numberOfColumns, workTimeType, columnNames)
        } else {
            App(numberOfColumns.value.toInt(), workTimeType.value, columnNames)
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
}
