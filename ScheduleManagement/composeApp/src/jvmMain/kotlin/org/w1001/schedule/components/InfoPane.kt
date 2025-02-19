package org.w1001.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.w1001.schedule.AppViewModel

@Composable
fun InfoPane(
    modifier: Modifier = Modifier, cellSize: MutableState<DpSize>, onSave: () -> Unit,
    viewModel: AppViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.background(Color.Red)) {
        Text("Info Pane", textAlign = TextAlign.Center)
        Button(onClick = {
            cellSize.value = DpSize(
                (cellSize.value.width + 10.dp).coerceAtLeast(10.dp), (cellSize.value.height + 5.dp).coerceAtLeast(5.dp)
            )
        }) {
            Text("Increase Cell Size")
        }

        Button(onClick = {
            cellSize.value = DpSize(
                (cellSize.value.width - 10.dp).coerceAtLeast(10.dp), (cellSize.value.height - 5.dp).coerceAtLeast(5.dp)
            )
        }) {
            Text("Decrease Cell Size")
        }

        Button(onClick = {
            viewModel.inMainMenu.value = true
        }) {
            Text("Main Menu")
        }

        Button(onClick = onSave) {
            Text("Save")
        }
    }
}