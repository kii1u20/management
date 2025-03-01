package org.w1001.schedule.components

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
fun PrintDialog(
    onDismiss: () -> Unit,
    onConfirm: (companyName: String, storeName: String) -> Unit,
    initialCompanyName: String = "",
    initialStoreName: String = ""
) {
    var companyName by remember { mutableStateOf(initialCompanyName) }
    var storeName by remember { mutableStateOf(initialStoreName) }

    val scale = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            )
        )
    }

    fun validateInputs(): Boolean {
        if (companyName.isBlank()) {
            errorMessage = "Please enter company name"
            return false
        }
        if (storeName.isBlank()) {
            errorMessage = "Please enter store name"
            return false
        }
        return true
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
                    "Print Document",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { 
                        companyName = it
                        showError = false
                    },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && companyName.isBlank()
                )

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { 
                        storeName = it
                        showError = false 
                    },
                    label = { Text("Store Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && storeName.isBlank()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showError) {
                        Text(
                            errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    TextButton(onClick = { dismiss(onDismiss) }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                dismiss { onConfirm(companyName, storeName) }
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text("Print")
                    }
                }
            }
        }
    }
}
