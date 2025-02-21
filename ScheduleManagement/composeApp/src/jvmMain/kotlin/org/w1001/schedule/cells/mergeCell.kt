package org.w1001.schedule.cells
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.w1001.schedule.CellData

@Composable
fun mergedCell(
    cellDataList: List<CellData>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String,

    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    textColor: Color = Color.Black,
    enabled: Boolean = true,
    cornerRadius: Dp = 0.dp
) {

    val textFieldRef = remember { FocusRequester() }

    val textInteractionSource = remember { MutableInteractionSource() }
    // Detect press on Text and call onClick immediately
    LaunchedEffect(textInteractionSource) {
        textInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press) {
                onClick() // Trigger selection on press down
            }
        }
    }

    LaunchedEffect(cellDataList) {
        cellDataList.forEach { it.content.value = "" }
        cellDataList.last().content.value = value
    }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            textFieldRef.requestFocus()
        }
    }

    // For now, use the first cell's value as the merged text.
    var textFieldValue by remember(cellDataList.joinToString { it.content.value }) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    Cell(
        modifier = modifier,
        isSelected = isSelected,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        cornerRadius = cornerRadius
    ) {
        val fontSize = with(LocalDensity.current) { (maxWidth / (3 * cellDataList.size)).toSp() }

        if (isSelected) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val newText = newValue.text.replace("-", "").trim()
                    textFieldValue = newValue.copy(text = newText)
                    // Update all underlying cellData objects.
                    cellDataList.last().content.value = newText
                },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(textFieldRef),
                textStyle = MaterialTheme.typography.body1.copy(
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize,
                    lineHeight = fontSize
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        innerTextField()
                    }
                }
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            textInteractionSource.tryEmit(press)
                            tryAwaitRelease()
                            textInteractionSource.tryEmit(PressInteraction.Release(press))
                        }
                    )
                }
            ) {
                Text(
                    text = cellDataList.last().content.value,
                    style = MaterialTheme.typography.body1.copy(
                        color = textColor,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        lineHeight = fontSize
                    )
                )
            }
        }
    }
}