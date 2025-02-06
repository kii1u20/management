package org.w1001.schedule.cells
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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

    LaunchedEffect(isSelected) {
        if (isSelected) {
            textFieldRef.requestFocus()
        }
    }

    LaunchedEffect(cellDataList) {
        cellDataList.forEach { it.content.value = "" }
        cellDataList.last().content.value = value
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
    val source = remember { MutableInteractionSource() }

    Cell(
        modifier = modifier,
        isSelected = isSelected,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        cornerRadius = cornerRadius
    ) {
        val fontSize = with(LocalDensity.current) { (maxWidth / (3 * cellDataList.size)).toSp() }

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val newText = newValue.text.replace("-", "").trim()
                textFieldValue = newValue.copy(text = newText)
                // Update all underlying cellData objects.
                cellDataList.last().content.value = newText
            },
            enabled = enabled,
            interactionSource = source,
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

        if (source.collectIsPressedAsState().value) {
            onClick()
//            textFieldRef.requestFocus()
        }
    }
}