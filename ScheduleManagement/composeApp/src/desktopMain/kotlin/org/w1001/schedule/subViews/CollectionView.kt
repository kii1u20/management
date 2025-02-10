package org.w1001.schedule.subViews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.w1001.schedule.components.MainMenuCard
import org.w1001.schedule.database.SpreadsheetRepository

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

    LaunchedEffect(place) {
        try {
            collections = repository.getCollectionNames(place)
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
            Button(onClick = onBack, modifier = Modifier.weight(0.1f)) {
                Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.weight(0.4f)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                    )
                }
                Text("Назад", modifier = Modifier.weight(0.6f), maxLines = 1)
            }
            Text(
                text = place,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(0.6f),
            )
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(0.1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.weight(0.4f)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = "",
                        )
                    }

                    Text("Създай", modifier = Modifier.weight(0.6f), maxLines = 1)
                }
            }
        }

        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )

            collections.isEmpty() -> Text("No collections found")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
            ) {
                items(collections) { collection ->
                    MainMenuCard(text = collection) {
                        onCollectionSelected(collection)
                    }
                }
            }
        }
    }
}
