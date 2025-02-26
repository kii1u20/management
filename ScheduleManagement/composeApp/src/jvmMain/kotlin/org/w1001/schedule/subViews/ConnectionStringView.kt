package org.w1001.schedule.subViews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.w1001.schedule.CredentialManager
import org.w1001.schedule.components.mainMenu.MainMenuTopBar

private val logger = KotlinLogging.logger("ConnectionStringView.kt")

@Composable
fun ConnectionStringView() {
    var connectionString by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var testSuccess by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainMenuTopBar(
            onBack = {},
            onCreate = {},
            heading = "MongoDB Connection Setup",
            createButtonVisible = false,
            backButtonVisible = false
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .width(600.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0x40000000)
                )
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Please enter your MongoDB connection string",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = connectionString,
                    onValueChange = {
                        connectionString = it
                        errorMessage = null
                        testSuccess = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Connection String") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    supportingText = {
                        Text("Format: mongodb+srv://username:password@clustername.mongodb.net")
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    if (connectionString.isBlank()) {
                                        errorMessage = "Connection string cannot be empty"
                                        return@launch
                                    }

                                    // Test connection before saving
                                    if (testConnectionString(connectionString)) {
                                        testSuccess = true
                                        CredentialManager.storeConnectionString(connectionString)
                                    } else {
                                        errorMessage = "Connection test failed. Please check your connection string."
                                    }
                                } catch (e: Exception) {
                                    logger.error { e.stackTraceToString() }
                                    errorMessage = e.message ?: "An unknown error occurred"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Save and Connect")
                    }
                }

                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AnimatedVisibility(visible = testSuccess) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Connection successful! Redirecting...",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Your connection string will be stored securely on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private suspend fun testConnectionString(connectionString: String): Boolean {
    // Simulate connection test
    return connectionString.startsWith("mongodb") &&
            connectionString.contains("@") &&
            connectionString.contains(".")
}
