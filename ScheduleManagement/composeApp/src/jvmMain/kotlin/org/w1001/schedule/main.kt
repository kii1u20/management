package org.w1001.schedule

import MainMenuV2
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import org.w1001.schedule.components.WarningDialog
import org.w1001.schedule.components.exitApplicationDialog

private val logger = KotlinLogging.logger("main.kt")
val viewModel = AppViewModel()
fun main() = application {
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val showExitDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Window(
        onCloseRequest = { if (!viewModel.inMainMenu.value) showExitDialog.value = true else exitApplication() },
        title = "ScheduleManagement v${BuildConfig.VERSION}",
        state = WindowState(size = DpSize(1400.dp, 900.dp), placement = WindowPlacement.Floating),
    ) {
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
            if (showExitDialog.value) {
                exitApplicationDialog(
                    exitApplication = { exitApplication()},
                    showExitDialog = showExitDialog,
                    viewModel = viewModel,
                    errorMessage = errorMessage
                )
            }

            if (errorMessage.value != null) {
                WarningDialog(
                    message = errorMessage.value!!,
                    onDismiss = { errorMessage.value = null }
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (viewModel.inMainMenu.value) {
                    logger.info { "loading MainMenuV2" }
                    MainMenuV2(viewModel)
                } else {
                    logger.info { "loading DocumentUI" }
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
