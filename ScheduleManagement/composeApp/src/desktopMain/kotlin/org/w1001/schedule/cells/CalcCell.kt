package org.w1001.schedule.cells

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.w1001.schedule.CellData
import org.w1001.schedule.Operation

// Represents a cell reference (row and column)
data class CellRef(val row: Int, val col: Int)

// Represents a calculation step
sealed class CalcStep {
    data class CellValue(val cellRef: CellRef) : CalcStep()
    data class Calculation(val left: CalcStep, val right: CalcStep, val operation: Operation) : CalcStep()
}

val specialCharMap = mapOf(
    "A" to 8,
    // Add other special characters and their values here
)

fun evaluateStep(step: CalcStep, cells: List<List<CellData>>): Int {
    return when (step) {
        is CalcStep.CellValue -> {
            val content = cells[step.cellRef.row][step.cellRef.col].content.value
            content.toIntOrNull() ?: specialCharMap[content] ?: 0
        }
        is CalcStep.Calculation -> {
            val leftValue = evaluateStep(step.left, cells)
            val rightValue = evaluateStep(step.right, cells)
            step.operation.execute(leftValue, rightValue)
        }
    }
}

@Composable
fun calcCell(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    cornerRadius: Dp = 0.dp,
    textColor: Color = Color.Black,
    calculation: CalcStep,
    cells: List<List<CellData>>,
) {
    Cell(
        modifier = modifier,
        isSelected = false,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        cornerRadius = cornerRadius
    ) {
        val fontSize = with(LocalDensity.current) { (maxWidth / 3).toSp() }

        val result = try {
            evaluateStep(calculation, cells)
        } catch (e: Exception) {
            0
        }

        Text(
            result.toString(),
            textAlign = TextAlign.Center,
            fontSize = fontSize,
            style = MaterialTheme.typography.body1.copy(
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = fontSize,
                lineHeight = fontSize
            )
        )
    }
}