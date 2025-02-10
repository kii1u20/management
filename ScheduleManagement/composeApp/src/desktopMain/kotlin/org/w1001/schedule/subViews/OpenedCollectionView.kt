package org.w1001.schedule.subViews

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text("Назад")
            }
            Text(
                text = collection,
                style = MaterialTheme.typography.headlineMedium,
            )
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.alpha(0f)
            ) {
                Text("Назад")
            }
        }

        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )
            documents.isEmpty() -> Text("No collections found")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
            ) {
                items(documents) { document ->
                    DocumentCard(document = document.name) {
                        scope.launch {
                            isLoading = true //show the loading dialog when the document is being loaded
                            val fullDoc = repository.loadDocument(document.id)
                            if (fullDoc != null) {
                                viewModel.loadDocument(fullDoc)
                            }
                            isLoading = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: String,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.6f,  // Less damping = more bouncy
                stiffness = 300f      // Higher stiffness = faster animation
            )
        )
    }

    Card(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .aspectRatio(2.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = document,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}