package org.w1001.schedule.components.mainMenu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

@Composable
fun DeleteObjectDialog(
    objectName: String?,
    dialogTitle: String,
    dialogText: String,
    onDismiss: () -> Unit,
    onConfirmDelete: suspend () -> Boolean,
    onDeleteFinished: suspend (Boolean, String) -> Unit
) {
    if (objectName == null) return

    val scale = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    fun dismissDialog() {
        coroutineScope.launch {
            scale.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
            )
            onDismiss()
        }
    }

    LaunchedEffect(objectName) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
        )
    }

    AlertDialog(
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            alpha = scale.value
        },
        onDismissRequest = { dismissDialog() },
        title = { Text(dialogTitle) },
        text = { Text(dialogText) },
        confirmButton = {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        val success = onConfirmDelete()
                        val message = if (success) "Deleted successfully" else "Failed to delete"
                        onDeleteFinished(success, message)
                        dismissDialog()
                    }
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { dismissDialog() }) {
                Text("No")
            }
        }
    )
}
