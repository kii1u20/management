package org.w1001.schedule.subViews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.w1001.schedule.components.mainMenu.MainMenuGrid
import org.w1001.schedule.components.mainMenu.MainMenuTopBar
import org.w1001.schedule.database.SpreadsheetRepository

@Composable
fun PlacesView(
    repository: SpreadsheetRepository,
    onPlaceSelected: (String) -> Unit
) {
    var places by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            places = repository.getDatabases()
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),  // increased horizontal padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainMenuTopBar(
            onBack = {},
            onCreate = {},
            heading = "Моля, изберете обект",
            createButtonVisible = false,
            backButtonVisible = false
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
            error != null -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            places.isEmpty() -> {
                Text(
                    "No locations available",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                MainMenuGrid(places, onPlaceSelected)
            }
        }
    }
}
