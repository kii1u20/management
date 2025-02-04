package org.w1001.schedule

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HeadingRow(workTime: Int, columns: Int, horizontalScrollState: ScrollState) {
    if (workTime == 1) {
        Row(horizontalArrangement = Arrangement.Start) {
            DayBox()
            ColumnBox(horizontalScrollState, columns, cellSize.value.width * 3, cellSize.value.height, 3)
        }
    } else {
        Row(horizontalArrangement = Arrangement.Start) {
            DayBox()
            ColumnBox(horizontalScrollState, columns, cellSize.value.width * 5 + 5.dp, cellSize.value.height, 5)
        }
    }
}

@Composable
private fun DayBox() {
    BoxWithConstraints(
        Modifier.size(cellSize.value)
//                            .background(Color.Red)
    ) {
        val fontSize = with(LocalDensity.current) { (maxWidth / 3).toSp() }
        Text(
            "Day",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.body1.copy(
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = fontSize,
                lineHeight = fontSize // Add this to ensure proper vertical centering
            )
        )
    }
}

@Composable
private fun ColumnBox(horizontalScrollState: ScrollState, columns: Int, width: Dp, height: Dp, fontSizeMultiplier: Int) {
    Row(Modifier.horizontalScroll(horizontalScrollState)) {
        Spacer(modifier = Modifier.width(10.dp))
        for (j in 0 until columns) {
            BoxWithConstraints(
                Modifier.size(width, height)
//                                        .background(Color.Red)
            ) {
                val fontSize = with(LocalDensity.current) { (maxWidth / (3 * fontSizeMultiplier)).toSp() }
                Text(
                    columnNames[j].value,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.body1.copy(
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        lineHeight = fontSize // Add this to ensure proper vertical centering
                    )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}