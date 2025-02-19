package org.w1001.schedule.components.mainMenu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
    isUniqueName: (String) -> Boolean
) {
    var collectionName by remember { mutableStateOf("") }

    val scale = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    var showError by remember { mutableStateOf(false) }

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
        coroutineScope.launch {
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

    Dialog(onDismissRequest = { dismiss(onDismiss) }) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = scale.value
                }.width(600.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Създаване на колекция",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Име на колекция") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showError) {
                        val text = if (!isUniqueName(collectionName)) {
                            "Колеция с това име вече съществува"
                        }else {
                            "Моля, попълнете всички полета"
                        }
                        Text(
                            text,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    TextButton(onClick = { dismiss(onDismiss) }) {
                        Text("Отказ")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (!showError) onConfirm(collectionName) },
//                        onClick = { dismiss { onConfirm(collectionName, selectedType) } },
                        enabled = !showError
                    ) {
                        Text("Създай")
                    }
                }
            }
        }

        if (collectionName.isBlank() || !isUniqueName(collectionName)) showError = true else showError = false
    }
}
