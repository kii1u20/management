package org.w1001.schedule.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.w1001.schedule.AppViewModel
import org.w1001.schedule.DocumentState
import org.w1001.schedule.viewModel

@Composable
fun InfoPane(
    modifier: Modifier = Modifier, cellSize: MutableState<DpSize>, onSave: () -> Unit,
    viewModel: AppViewModel
) {
    val showExitDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    if (errorMessage.value != null) {
        WarningDialog(
            message = errorMessage.value!!,
            onDismiss = { errorMessage.value = null }
        )
    }

    if (showExitDialog.value) {
        exitApplicationDialog(
            exitApplication = { viewModel.inMainMenu.value = true },
            showExitDialog = showExitDialog,
            viewModel = viewModel,
            errorMessage = errorMessage
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        titleBox()

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
            showExitDialog.value = true
        }) {
            Text("Main Menu")
        }

        Button(onClick = onSave) {
            Text("Save")
        }

//        fontScaler() //mergeCell, calcCell and headingRow are not set up for auto scaling yet
    }
}

@Composable
fun fontScaler() {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Авто размер на шрифт",
                color = MaterialTheme.colorScheme.onSurface,
                style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
            )

            Checkbox(
                checked = viewModel.enableAutoFontSize.value,
                onCheckedChange = { viewModel.enableAutoFontSize.value = it }
            )
        }
    }

    if(!viewModel.enableAutoFontSize.value) {
        Column() {
            Text(
                "Размер шрифта",
                color = MaterialTheme.colorScheme.onSurface,
                style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
            )

            Slider(
                value = viewModel.fontSize.value,
                onValueChange = { viewModel.fontSize.value = it },
                valueRange = 10f..50f
            )
        }
    }
}

@Composable
fun titleBox(

) {
    val documentName = when (val state = viewModel.documentState.value) {
        is DocumentState.ScheduleState -> state.documentName.value
        DocumentState.Empty -> "No Document Loaded"
    }
    BoxWithConstraints {
        val headingFontSize = with(LocalDensity.current) { (maxWidth / 13).toSp() }
        BasicTextField(
            value = documentName,
            onValueChange = { newName ->
                if (viewModel.documentState.value is DocumentState.ScheduleState) {
                    (viewModel.documentState.value as DocumentState.ScheduleState).documentName.value = newName
                }
            },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = headingFontSize,
                fontWeight = FontWeight.Bold
            ),
            singleLine = false,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth().animateContentSize()
        )
    }
}