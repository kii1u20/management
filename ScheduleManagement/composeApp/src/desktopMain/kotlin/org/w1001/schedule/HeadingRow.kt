package org.w1001.schedule

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HeadingRow(
    workTime: Int,
    columns: Int,
    horizontalScrollState: ScrollState,
    calcCellBindings: HashMap<Int, MutableList<MutableState<Int>>>
) {
    if (workTime == 1) {
        Row(horizontalArrangement = Arrangement.Start) {
            DayBox()
            ColumnBox(
                horizontalScrollState,
                columns,
                cellSize.value.width * 3,
                cellSize.value.height,
                cellSize.value.width,
                3,
                calcCellBindings
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.Start) {
            DayBox()
            ColumnBox(
                horizontalScrollState,
                columns,
                cellSize.value.width * 5 + 5.dp,
                cellSize.value.height,
                cellSize.value.width,
                5,
                calcCellBindings
            )
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
private fun ColumnBox(
    horizontalScrollState: ScrollState,
    columns: Int,
    width: Dp,
    height: Dp,
    cellWidth: Dp,
//    cellHeight: Dp,
    fontSizeMultiplier: Int,
    calcCellBindings: HashMap<Int, MutableList<MutableState<Int>>>
) {
    Row(Modifier.horizontalScroll(horizontalScrollState)) {
        Spacer(modifier = Modifier.width(10.dp))
        for (j in 0 until columns) {
            BoxWithConstraints(
                Modifier.size(width, height)
//                                        .background(Color.Red)
            ) {
                val fontSize = with(LocalDensity.current) { (maxWidth / (3 * fontSizeMultiplier)).toSp() }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(cellWidth * 2, height)) {
                        Text(
                            columnNames[j].value + ":",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.body1.copy(
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                fontSize = fontSize,
                                lineHeight = fontSize // Add this to ensure proper vertical centering
                            )
                        )
                    }

                    val sum = calcCellBindings[j]?.sumOf { it.value }
                    Box(modifier = Modifier.size(cellWidth, height)) {
                        Text(
                            sum.toString(),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.body1.copy(
                                color = Color.Black,
                                textAlign = TextAlign.Start,
                                fontSize = fontSize,
                                lineHeight = fontSize // Add this to ensure proper vertical centering
                            )
                        )
                    }
//                    Box(modifier = Modifier.background(Color.Red).width(cellWidth / 2).height(height))
//                    Spacer(modifier = Modifier.width(cellWidth))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}