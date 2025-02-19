package org.w1001.schedule.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mongodb.MongoSocketException
import com.mongodb.MongoTimeoutException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.w1001.schedule.AppViewModel

private val logger = KotlinLogging.logger("exitApplicationDialog.kt")
@Composable
fun exitApplicationDialog(
    exitApplication: () -> Unit,
    showExitDialog: MutableState<Boolean>,
    viewModel: AppViewModel,
    errorMessage: MutableState<String?>
) {
    val scale = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            )
        )
    }
    fun dismiss(onComplete: () -> Unit) {
        scope.launch {
            scale.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 300f
                )
            )
            onComplete()
        }
    }
    AlertDialog(
        onDismissRequest = { dismiss { showExitDialog.value = false } },
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            alpha = scale.value
        },
        title = { Text("Save and Exit") },
        text = { Text("Do you want to save before exiting?") },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            viewModel.saveDocument()
                            dismiss { exitApplication() }
                        } catch (e: Exception) {
                            logger.error { e.stackTraceToString() }
                            errorMessage.value = when (e) {
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
                Button(onClick = { dismiss { exitApplication() } }) {
                    Text("Exit without saving")
                }
                Button(onClick = { dismiss { showExitDialog.value = false } }) {
                    Text("Cancel")
                }
            }
        }
    )
}
