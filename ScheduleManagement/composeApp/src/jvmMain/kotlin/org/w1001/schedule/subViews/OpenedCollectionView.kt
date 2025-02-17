package org.w1001.schedule.subViews

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.w1001.schedule.components.mainMenu.*
import org.w1001.schedule.database.DocumentMetadata
import org.w1001.schedule.database.SpreadsheetRepository
import org.w1001.schedule.viewModel

@Composable
fun OpenedCollectionView(
    place: String,
    collection: String,
    repository: SpreadsheetRepository,
    onBack: () -> Unit
) {
    var documents by remember { mutableStateOf<List<DocumentMetadata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccess by remember { mutableStateOf(true) }

    var documentToDelete by remember { mutableStateOf<String?>(null) }

    val scale = remember { Animatable(0f) }

    LaunchedEffect(place) {
        try {
            documents = repository.loadDocumentMetadata(place, collection)
            viewModel.currentCollection = collection
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    if (showCreateDialog) {
        CreateDocumentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, type, columns, columnNames ->
                coroutineScope.launch {
//                    val success = repository.createCollection(place, name)
                    showCreateDialog = false
                    isSuccess = true

                    val message = if (isSuccess) {
                        viewModel.createNewSchedule(
                            columns = columns,
                            columnNames = columnNames.map { mutableStateOf(it) }.toMutableStateList(),
                            name = name,
                            isSchedule1 = type == "schedule1"
                        )
                        viewModel.currentDatabase = place
                        viewModel.currentCollection = collection
                        "Document created successfully"
                    } else {
                        "A document with this name already exists"
                    }

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }

//                    if (isSuccess) {
//                        documents = repository.loadDocumentMetadata(place, collection, collection)
//                    }
                }
            },
            documentTypes = viewModel.documentTypes
        )
    }


    if (documentToDelete != null) {
        DeleteObjectDialog(
            objectName = documentToDelete,
            dialogTitle = "Delete document",
            dialogText = "Are you sure you want to delete \"${documentToDelete}\"?",
            onDismiss = { documentToDelete = null },
            onConfirmDelete = {
                repository.deleteDocumentByName(place, collection, documentToDelete!!)
            },
            onDeleteFinished = { success, message ->
                isSuccess = success
                if (success) {
                    documents = repository.loadDocumentMetadata(place, collection)
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState, snackbar = { snackbarData ->
                CustomSnackbar(snackbarData, isSuccess = isSuccess)
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainMenuTopBar(
                onBack = onBack,
                onCreate = { showCreateDialog = true },
                heading = "$place - $collection"
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                error != null -> Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error
                )

                documents.isEmpty() -> Text("No collections found")
                else -> MainMenuGrid(documents.map { it.name }, onObjectSelected = { name ->
                    val document = documents.find { it.name == name }
                    if (document != null) {
                        coroutineScope.launch {
                            isLoading = true // show the loading dialog when the document is being loaded
                            val fullDoc = repository.loadDocument(document.id, place, collection)
                            if (fullDoc != null) {
                                viewModel.loadDocument(fullDoc) // HANDLE EXCEPTION!
                                viewModel.currentDatabase = place
                                viewModel.currentCollection = collection
                            }
                            isLoading = false
                        }
                    }
                }, onDeleteObject = { document ->
                    documentToDelete = document
                }, showDeleteButton = true)
            }
        }
    }
}
