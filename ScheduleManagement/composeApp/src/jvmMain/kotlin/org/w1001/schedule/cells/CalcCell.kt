package org.w1001.schedule.cells

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.w1001.schedule.CalcStep
import org.w1001.schedule.CellData
import org.w1001.schedule.MathEngine.Companion.evaluateStep

val specialCharMap = mapOf(
    "A" to 8,
    // Add other special characters and their values here
)

@Composable
fun calcCell(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    cornerRadius: Dp = 0.dp,
    textColor: Color = Color.Black,
    calculation: CalcStep,
    cells: List<List<CellData>>,
    resultBinding: MutableState<Int>? = null // new binding parameter
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

        resultBinding?.value = result

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