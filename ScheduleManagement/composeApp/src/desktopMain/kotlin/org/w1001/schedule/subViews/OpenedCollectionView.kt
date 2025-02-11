package org.w1001.schedule.subViews

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.w1001.schedule.components.mainMenu.MainMenuGrid
import org.w1001.schedule.components.mainMenu.MainMenuTopBar
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(place) {
        try {
            documents = repository.loadDocumentMetadata(place, collection, collection)
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainMenuTopBar(
            onBack = onBack,
            onCreate = {},
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
                    scope.launch {
                        isLoading = true // show the loading dialog when the document is being loaded
                        val fullDoc = repository.loadDocument(document.id, place, collection)
                        if (fullDoc != null) {
                            viewModel.loadDocument(fullDoc)
                        }
                        isLoading = false
                    }
                }
            })
        }
    }
}
