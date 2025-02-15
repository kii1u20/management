@file:OptIn(ExperimentalMaterial3Api::class)

package org.w1001.schedule.components.mainMenu

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, columns: String, columnNames: List<String>) -> Unit,
    documentTypes: List<String>
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Document") },
        text = {
            Column {
                // Document Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedType ?: "Select document type",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
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

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic create flow based on selected type
                AnimatedContent(
                    targetState = selectedType,
                    transitionSpec = {
                        fadeIn() + expandVertically() with
                                fadeOut() + shrinkVertically()
                    }
                ) { type ->
                    when (type) {
                        "schedule1", "schedule2" -> ScheduleCreateFlow(
                            onConfirm = { name, columns, columnNames ->
                                onConfirm(name, type, columns, columnNames)
                            }
                        )
                        // Add more document type flows here
                        null -> Box {} // Empty box when no type is selected
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ScheduleCreateFlow(
    onConfirm: (name: String, columns: String, columnNames: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var columns by remember { mutableStateOf("") }
    var columnNames by remember { mutableStateOf(listOf<String>()) }
    var showError by remember { mutableStateOf(false) }

    Column {
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
                    columns = it
                    // Initialize or update column names list
                    val numColumns = it.toIntOrNull() ?: 0
                    columnNames = when {
                        numColumns > columnNames.size ->
                            columnNames + List(numColumns - columnNames.size) { idx ->
                                "Column ${columnNames.size + idx + 1}"
                            }
                        numColumns < columnNames.size -> columnNames.take(numColumns)
                        else -> columnNames
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
            targetState = columnNames,
            transitionSpec = {
                if (targetState.size > initialState.size) {
                    (fadeIn() + expandVertically(expandFrom = Alignment.Top)) with
                            fadeOut()
                } else {
                    fadeIn() with
                            (fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top))
                }
            }
        ) { currentColumnNames ->
            if (currentColumnNames.isNotEmpty()) {
                Column {
                    Text(
                        "Column Names",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    currentColumnNames.forEachIndexed { index, columnName ->
                        key(index) {
                            OutlinedTextField(
                                value = columnName,
                                onValueChange = { newName ->
                                    columnNames = columnNames.toMutableList().apply {
                                        this[index] = newName
                                    }
                                },
                                label = { Text("Column ${index + 1}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .animateEnterExit(
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank() || columns.isBlank()) {
                    showError = true
                } else {
                    onConfirm(name, columns, columnNames)
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Create")
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
