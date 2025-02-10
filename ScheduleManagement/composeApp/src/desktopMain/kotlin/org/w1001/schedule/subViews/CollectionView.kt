package org.w1001.schedule.subViews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.w1001.schedule.components.mainMenu.MainMenuGrid
import org.w1001.schedule.components.mainMenu.MainMenuTopBar
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
        MainMenuTopBar(onBack, {} , place)

        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )

            collections.isEmpty() -> Text("No collections found")
            else -> MainMenuGrid(collections, onCollectionSelected)
        }
    }
}
