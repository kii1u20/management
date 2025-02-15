package org.w1001.schedule.subViews

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.w1001.schedule.components.mainMenu.CreateCollectionDialog
import org.w1001.schedule.components.mainMenu.CustomSnackbar
import org.w1001.schedule.components.mainMenu.MainMenuGrid
import org.w1001.schedule.components.mainMenu.MainMenuTopBar
import org.w1001.schedule.database.SpreadsheetRepository
import org.w1001.schedule.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionView(
    place: String,
    repository: SpreadsheetRepository,
    onBack: () -> Unit,
    onCollectionSelected: (String) -> Unit
) {
    var collections by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccess by remember { mutableStateOf(true) }

    LaunchedEffect(place) {
        try {
            collections = repository.getCollectionNames(place)
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, type ->
                coroutineScope.launch {
                    val success = repository.createCollection(place, name)
                    showCreateDialog = false
                    isSuccess = success

                    val message = if (success) {
                        "Колекцията е създадена успешно"
                    } else {
                        "Колекция с това име вече съществува"
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }


                    // If creation was successful, refresh the collections list
                    if (success) {
                        collections = repository.getCollectionNames(place)
                    }
                }
            },
            documentTypes = viewModel.documentTypes
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, snackbar = { snackbarData ->
            CustomSnackbar(snackbarData, isSuccess = isSuccess)
        }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainMenuTopBar(onBack, {showCreateDialog = true} , place)

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

                collections.isEmpty() -> Text("No collections found")
                else -> MainMenuGrid(collections, onCollectionSelected)
            }
        }
    }
}
