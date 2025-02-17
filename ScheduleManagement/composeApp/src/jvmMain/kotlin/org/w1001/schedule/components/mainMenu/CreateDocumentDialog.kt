@file:OptIn(ExperimentalMaterial3Api::class)

package org.w1001.schedule.components.mainMenu

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CreateDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, columns: String, columnNames: List<String>) -> Unit,
    documentTypes: List<String>
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

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
                }
                .width(500.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Create New Document",
                    style = MaterialTheme.typography.headlineSmall
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType ?: "Select document type",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        documentTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = selectedType,
                    transitionSpec = {
                        fadeIn() + expandVertically() with fadeOut() + shrinkVertically()
                    }
                ) { type ->
                    when (type) {
                        "schedule1", "schedule2" -> ScheduleCreateFlow(
                            onDismiss = { dismiss(onDismiss) },
                            onConfirm = { name, columns, columnNames ->
                                dismiss { onConfirm(name, type, columns, columnNames) }
                            }
                        )
                        null -> Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { dismiss(onDismiss) }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ScheduleCreateFlow(
    onDismiss: () -> Unit,
    onConfirm: (name: String, columns: String, columnNames: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var columns by remember { mutableStateOf("1") }
    val columnNames = remember { mutableStateListOf("Column 1") }
    var showError by remember { mutableStateOf(false) }
    val verticalScrollState = rememberScrollState()

    val columnCount by derivedStateOf {
        columns.toIntOrNull() ?: 1
    }

    Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Document Name") },
            isError = showError && name.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = columns,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    // Parse the input and clamp it to a minimum of 1
                    var numColumns = it.toIntOrNull() ?: 1
                    if (numColumns < 1) numColumns = 1

                    columns = numColumns.toString()

                    // Update the list of column names
                    when {
                        numColumns > columnNames.size -> {
                            columnNames.addAll(
                                List(numColumns - columnNames.size) { idx ->
                                    "Column ${columnNames.size + idx + 1}"
                                }
                            )
                        }
                        numColumns < columnNames.size -> {
                            while (columnNames.size > numColumns) {
                                columnNames.removeLast()
                            }
                        }
                    }
                }
            },
            label = { Text("Number of Columns") },
            isError = showError && columns.isBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = columnCount,
            transitionSpec = {
                if (targetState > initialState) {
                    (fadeIn() + expandVertically(expandFrom = Alignment.Top)) togetherWith
                            fadeOut()
                } else {
                    fadeIn() togetherWith
                            (fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top))
                }
            }
        ) { count ->
            // Render text fields for each column
            Column() {
                Text("Column Names", style = MaterialTheme.typography.titleMedium)
                repeat(count) { index ->
                    OutlinedTextField(
                        value = columnNames.getOrNull(index) ?: "",
                        onValueChange = { newName ->
                            if (index < columnNames.size) {
                                columnNames[index] = newName
                            }
                        },
                        label = { Text("Column ${index + 1}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (name.isBlank() || columns.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(name, columns, columnNames)
                    }
                }
            ) {
                Text("Create")
            }
        }

        if (showError) {
            Text(
                "Please fill all fields",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
