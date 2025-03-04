package org.w1001.schedule.subViews

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
import org.w1001.schedule.database.SpreadsheetRepository
import org.w1001.schedule.viewModel

private val logger = KotlinLogging.logger("CollectionView.kt")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionView(
    place: String,
    repository: SpreadsheetRepository,
    onBack: () -> Unit,
    onCollectionSelected: (String) -> Unit
) {
    var collections by remember { mutableStateOf<List<String>>(emptyList()) }
    var collectionNames by remember { mutableStateOf(HashSet<String>()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccess by remember { mutableStateOf(true) }

    var collectionToDelete by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(place) {
        try {
            collections = repository.getCollectionNames(place)
            collectionNames = collections.mapTo(HashSet()) { it }
            viewModel.currentDatabase = place
            viewModel.currentCollection = ""
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
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                coroutineScope.launch {
                    val success = repository.createCollection(place, name)
                    showCreateDialog = false
                    isSuccess = success

                    val message = if (success) {
                        "Колекцията е създадена успешно"
                    } else {
                        "Колекция с това име вече съществува"
                    }
                    logger.info { message }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }


                    // If creation was successful, refresh the collections list
                    if (success) {
                        collections = repository.getCollectionNames(place)
                        collectionNames = collections.mapTo(HashSet()) { it }
                    }
                }
            },
            isUniqueName = { name -> !collectionNames.contains(name) }
        )
    }

    // **New** AlertDialog when collectionToDelete is not null
    if (collectionToDelete != null) {
        DeleteObjectDialog(
            objectName = collectionToDelete,
            dialogTitle = "Delete collection",
            dialogText = "Are you sure you want to delete \"${collectionToDelete}\"?",
            onDismiss = { collectionToDelete = null },
            onConfirmDelete = {
                repository.deleteCollection(place, collectionToDelete!!)
            },
            onDeleteFinished = { success, message ->
                isSuccess = success
                if (success) {
                    collections = repository.getCollectionNames(place)
                    collectionNames = collections.mapTo(HashSet()) { it }
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
            MainMenuTopBar(onBack, { showCreateDialog = true }, place)

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

                collections.isEmpty() -> {
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
                                        collections = repository.getCollectionNames(place)
                                        collectionNames = collections.mapTo(HashSet()) { it }
                                        viewModel.currentDatabase = place
                                        viewModel.currentCollection = ""
                                        viewModel.clearLoadedDocument()
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

                else -> MainMenuGrid(collections, onCollectionSelected, onDeleteObject = { collectionName ->
                    collectionToDelete = collectionName
                }, showDeleteButton = true)
            }
        }
    }
}
