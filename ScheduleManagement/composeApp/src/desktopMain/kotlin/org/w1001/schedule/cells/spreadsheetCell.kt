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
fun spreadsheetCell(
    cellData: CellData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    textColor: Color = Color.Black,
    enabled: Boolean = true,
    cornerRadius: Dp = 0.dp // Default corner radius
) {
    var textFieldValue by remember(cellData.content.value) {
        mutableStateOf(
            TextFieldValue(
                text = cellData.content.value,
                selection = TextRange(cellData.content.value.length) // Initialize cursor at end
            )
        )
    }
    val source = remember { MutableInteractionSource() }
    val textFieldRef = remember { FocusRequester() }

    Cell(
        modifier = modifier,
        isSelected = isSelected,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        cornerRadius = cornerRadius
    ) {
        val fontSize = with(LocalDensity.current) { (maxWidth / 3).toSp() }

        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                val newText = it.text.replace("-", "").trim()
                textFieldValue = it.copy(text = newText)
                cellData.content.value = newText
            },
            enabled = enabled,
            interactionSource = source,
            modifier = Modifier.fillMaxSize().focusRequester(textFieldRef),
            textStyle = MaterialTheme.typography.body1.copy(
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = fontSize,
                lineHeight = fontSize // Add this to ensure proper vertical centering
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(  // Changed from Row to Box
                    contentAlignment = Alignment.Center,  // Center both horizontally and vertically
                    modifier = Modifier.fillMaxSize()
                ) {
                    innerTextField()
                }
            }
        )

        if (source.collectIsPressedAsState().value) {
            onClick()
            textFieldRef.requestFocus()
        }
    }
}