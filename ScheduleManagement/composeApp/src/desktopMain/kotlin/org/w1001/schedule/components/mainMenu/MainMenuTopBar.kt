package org.w1001.schedule.components.mainMenu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuTopBar(onBack: () -> Unit, onCreate: () -> Unit, heading: String, backButtonVisible: Boolean = true, createButtonVisible: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(modifier = Modifier.weight(0.1f)) {
            val buttonFontSize = with(LocalDensity.current) { (maxWidth / 10).toSp() }

            Button(onClick = onBack, enabled = backButtonVisible, modifier = Modifier.alpha(if (backButtonVisible) 1f else 0f)) {
                Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.weight(0.4f)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                    )
                }
                Text(
                    "Назад",
                    modifier = Modifier.weight(0.6f),
                    maxLines = 1,
                    fontSize = buttonFontSize
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.weight(0.8f).padding(horizontal = 16.dp)) {
            val headingFontSize = with(LocalDensity.current) { (maxWidth / 20).toSp() }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = headingFontSize
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.weight(0.1f)) {
            val buttonFontSize = with(LocalDensity.current) { (maxWidth / 10).toSp() }

            Button(onClick = onCreate, enabled = createButtonVisible, modifier = Modifier.alpha(if (createButtonVisible) 1f else 0f)) {
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
                    Text(
                        "Създай",
                        modifier = Modifier.weight(0.6f),
                        maxLines = 1,
                        fontSize = buttonFontSize
                    )
                }
            }
        }
    }
}