package org.w1001.schedule.subViews

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mongodb.MongoSocketException
import com.mongodb.MongoTimeoutException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.w1001.schedule.components.WarningDialog
import org.w1001.schedule.components.mainMenu.MainMenuGrid
import org.w1001.schedule.components.mainMenu.MainMenuTopBar
import org.w1001.schedule.database.SpreadsheetRepository
import org.w1001.schedule.viewModel

private val logger = KotlinLogging.logger("PlacesView.kt")
@Composable
fun PlacesView(
    repository: SpreadsheetRepository,
    onPlaceSelected: (String) -> Unit
) {
    var places by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        try {
            places = repository.getDatabases()
            viewModel.currentDatabase = ""
            viewModel.currentCollection = ""
            viewModel.clearDocumentState()
            isLoading = false
        } catch (e: Exception) {
            logger.error { e.stackTraceToString()}
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

            places.isEmpty() -> {
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
                                    places = repository.getDatabases()
                                    viewModel.currentDatabase = ""
                                    viewModel.currentCollection = ""
                                } catch (e: Exception) {
                                    logger.error { e.stackTraceToString()}
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

            else -> {
                MainMenuGrid(places, onPlaceSelected, onDeleteObject = {}, showDeleteButton = false)
            }
        }
    }
}
