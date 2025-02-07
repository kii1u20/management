package org.w1001.schedule

//import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenu(
    viewModel: AppViewModel
) {
    var showLoadDialog by remember { mutableStateOf(false) }
    val repository = remember { SpreadsheetRepository() }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Button(onClick = { viewModel.inMainMenu.value = false }) {
                    Text("Exit Main Menu")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showLoadDialog = true }) {
                    Text("Load")
                }
            }

            ScheduleSetupUI(viewModel)

            if (showLoadDialog) {
                LoadDocumentsDialog(
                    repository = repository,
                    onDismiss = { showLoadDialog = false },
                    onDocumentSelected = { document ->
                        viewModel.loadDocument(document)
                    }
                )
            }
        }
    }
}

@Composable
fun LoadDocumentsDialog(
    repository: SpreadsheetRepository,
    onDismiss: () -> Unit,
    onDocumentSelected: (SpreadsheetDocument) -> Unit
) {
    var documents by remember { mutableStateOf<List<SpreadsheetDocument>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            documents = repository.loadDocuments("Pavlikeni", "schedule")
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Document") },
        text = {
            when {
                isLoading -> {
                    Box(
//                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> Text("Error: $error")
                documents.isEmpty() -> Text("No documents found")
                else -> LazyColumn {
                    items(documents) { doc ->
                        TextButton(
                            onClick = {
                                onDocumentSelected(doc)
                                onDismiss()
                            }
                        ) {
                            Text(doc.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ScheduleSetupUI(
    viewModel: AppViewModel
) {
    var isColumnValid by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter the number of columns (must be a positive integer):")
            OutlinedTextField(
                value = viewModel.numberOfColumns.value,
                onValueChange = { input ->
                    // Validate input and ensure it's a positive integer
                    isColumnValid = input.toIntOrNull()?.let { it > 0 } ?: false
                    viewModel.numberOfColumns.value = input
                    showError = !isColumnValid

                    if (isColumnValid) {
                        viewModel.columnNames.clear()
                        repeat(input.toInt()) {
                            viewModel.columnNames.add(mutableStateOf(""))
                        }
                    }
                },
                isError = showError,
                modifier = Modifier.padding(16.dp)
            )

            if (showError) {
                Text(
                    text = "Please enter a valid positive integer.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                ColumnNamesInput(viewModel.numberOfColumns.value.toInt(), viewModel.columnNames)
            }



            Text("Select work time type:")
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = viewModel.workTime.value == 1,
                    onClick = {
                        viewModel.workTime.value = 1
                        recomputeCellsListSize(viewModel)
                    }
                )
                Text("Single Work Time")
                RadioButton(
                    selected = viewModel.workTime.value == 2,
                    onClick = {
                        viewModel.workTime.value = 2
                        recomputeCellsListSize(viewModel)
                    }
                )
                Text("Double Work Time")
            }

            Button(onClick = {
                if (isColumnValid) {
                    viewModel.inMainMenu.value = false
                    recomputeCellsListSize(viewModel)
                    viewModel.clearLoadedDocument()
                } else {
                    showError = true
                }
            }) {
                Text("Submit")
            }
        }
    }
}

fun recomputeCellsListSize(viewModel: AppViewModel) {
    viewModel.cells.clear()
    viewModel.cells.addAll(List(31) { row ->
        List(
            if (viewModel.workTime.value == 1) viewModel.numberOfColumns.value.toInt() * 2
            else viewModel.numberOfColumns.value.toInt() * 4
        ) { col ->
            CellData(mutableStateOf(""))
        }.toMutableStateList()
    }.toMutableStateList())
}

@Composable
fun ColumnNamesInput(numberOfColumns: Int, columnNames: List<MutableState<String>>) {
    Column {
        for (i in 0 until numberOfColumns) {
            OutlinedTextField(
                value = columnNames[i].value,
                onValueChange = { columnNames[i].value = it },
                label = { Text("Column ${i + 1} Name") },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}