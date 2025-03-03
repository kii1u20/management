package org.w1001.schedule.subViews

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mongodb.MongoSocketException
import com.mongodb.MongoTimeoutException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.w1001.schedule.components.WarningDialog
import org.w1001.schedule.components.mainMenu.*
import org.w1001.schedule.database.DocumentMetadata
import org.w1001.schedule.database.SpreadsheetRepository
import org.w1001.schedule.viewModel

private val logger = KotlinLogging.logger("OpenedCollectionView.kt")
@Composable
fun OpenedCollectionView(
    place: String,
    collection: String,
    repository: SpreadsheetRepository,
    onBack: () -> Unit
) {
    var documents by remember { mutableStateOf<List<DocumentMetadata>>(emptyList()) }
    var documentNames by remember { mutableStateOf(HashSet<String>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccess by remember { mutableStateOf(true) }

    var documentToDelete by remember { mutableStateOf<String?>(null) }

    val scale = remember { Animatable(0f) }


    LaunchedEffect(place) {
        try {
            documents = repository.loadDocumentMetadata(place, collection)
            documentNames = documents.mapTo(HashSet()) { it.name }
            viewModel.currentCollection = collection
            viewModel.clearLoadedDocument()
            isLoading = false
        } catch (e: Exception) {
            logger.error { e.stackTraceToString() }
            errorMessage = when (e) {
                is MongoSocketException -> "No internet connection"
                is MongoTimeoutException -> "No internet connection"
                else -> e.message ?: "An unknown error occurred"
            }
            isLoading = false
        }
    }

    if (errorMessage != null) {
        WarningDialog(
            message = errorMessage!!,
            onDismiss = { errorMessage = null }
        )
    }

    if (showCreateDialog) {
        CreateDocumentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { viewModel.createNewDocument(it) },
            isUniqueName = { name -> !documentNames.contains(name) }
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
                    documentNames = documents.mapTo(HashSet()) { it.name }
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

                documents.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "No collections available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    try {
                                        documents = repository.loadDocumentMetadata(place, collection)
                                        documentNames = documents.mapTo(HashSet()) { it.name }
                                        viewModel.currentCollection = collection
                                    } catch (e: Exception) {
                                        logger.error { e.stackTraceToString() }
                                        errorMessage = when (e) {
                                            is MongoSocketException -> "No internet connection"
                                            is MongoTimeoutException -> "No internet connection"
                                            else -> e.message ?: "An unknown error occurred"
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Опитай пак")
                        }
                    }
                }
                else -> MainMenuGrid(documents.map { it.name }, onObjectSelected = { name ->
                    val document = documents.find { it.name == name }
                    if (document != null) {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                val fullDoc = repository.loadDocument(document.id, place, collection)
                                if (fullDoc != null) {
                                    viewModel.loadDocument(fullDoc)
                                    viewModel.currentDatabase = place
                                    viewModel.currentCollection = collection
                                } else {
                                    errorMessage = "Document not found"
                                    logger.error { errorMessage }
                                }
                            } catch (e: Exception) {
                                logger.error { e.stackTraceToString() }
                                isSuccess = false
                                errorMessage = when (e) {
                                    is MongoSocketException,
                                    is MongoTimeoutException -> "No internet connection"
                                    is IllegalArgumentException -> "Invalid document type"
                                    else -> e.message ?: "An unknown error occurred"
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }, onDeleteObject = { document ->
                    documentToDelete = document
                }, showDeleteButton = true)
            }
        }
    }
}
