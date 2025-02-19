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
    documentTypes: List<String>,
    isUniqueName: (String) -> Boolean
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
                .width(600.dp),
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
                            },
                            isUniqueName = isUniqueName
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
    onConfirm: (name: String, columns: String, columnNames: List<String>) -> Unit,
    isUniqueName: (String) -> Boolean
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
            isError = showError && (name.isBlank() || !isUniqueName(name)),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = columns,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    // Parse the input and clamp it to a minimum of 1
                    columns = it

                    val numColumns = it.toIntOrNull() ?: 1

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
            isError = showError && (columns.isBlank() || columns.toIntOrNull() == 0),
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
                        isError = showError && columnNames.getOrNull(index)?.isBlank() == true,
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
            if (showError) {
                val text = if (!isUniqueName(name)) {
                    "Документ с това име вече съществува"
                } else if (columns.toIntOrNull() == 0) {
                    "Моля, въведете валиден брой колони (поне 1)"
                } else {
                    "Моля, попълнете всички полета"
                }
                Text(
                    text,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (!showError) {
                        onConfirm(name, columns, columnNames)
                    }
                },
                enabled = !showError
            ) {
                Text("Create")
            }
        }

        showError =
            name.isBlank() || columns.isBlank() || columnNames.any { it.isBlank() } || columns.toIntOrNull() == 0 || !isUniqueName(
                name
            )


    }
}
