package org.w1001.schedule

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.w1001.schedule.cells.*

data class CellData(var content: MutableState<String>)

abstract class Operation {
    abstract fun execute(a: Int, b: Int): Int
}

class PlusOperation : Operation() {
    override fun execute(a: Int, b: Int): Int {
        return a + b
    }
}

class MinusOperation : Operation() {
    override fun execute(a: Int, b: Int): Int {
        return b - a
    }
}

val cellSize: MutableState<DpSize> = mutableStateOf(DpSize(50.dp, 25.dp))

val specialMergeSet = hashSetOf("A", "B", "C") // add other special values as needed

@Composable
fun App(
    columns: Int, workTime: Int, columnNames: MutableList<MutableState<String>>
) {
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val focusManager = LocalFocusManager.current
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val cells = remember {
        List(31) { row ->
            // Changed: only allocate editable cells (2 per group for workTime==1, 4 per group for workTime==2)
            List(if (workTime == 1) columns * 2 else columns * 4) { col ->
                CellData(mutableStateOf(""))
            }
        }
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                selectedCell = null
            })
        }) {
            Column(Modifier.weight(0.7f)) {
                HeadingRow(workTime = workTime, columns = columns, horizontalScrollState = horizontalScrollState)

                Row() {
                    Column(
                        Modifier.verticalScroll(verticalScrollState), // Make the column scrollable
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (i in cells.indices) {
                            spreadsheetCell(
                                cellData = CellData(mutableStateOf("${i + 1}")),
                                isSelected = if ((selectedCell?.first ?: false) == i) true else false,
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.size(cellSize.value),
                            )
                        }
                    }
                    Column(
                        Modifier.verticalScroll(verticalScrollState)
                            .horizontalScroll(horizontalScrollState),
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (i in cells.indices) {
                            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Start) {
                                Spacer(modifier = Modifier.width(10.dp))
                                val groupSize = if (workTime == 1) 2 else 4
                                // There are "columns" groups in each row.
                                for (group in 0 until columns) {
                                    // Gather all cells within the group.
                                    val groupCells = (0 until groupSize).map { idx -> cells[i][group * groupSize + idx] }
                                    val specialValue = groupCells.firstOrNull { specialMergeSet.contains(it.content.value) }?.content?.value
                                    if (specialValue != null) {
                                        // Render a mergedCell for the entire group, passing the special value.
                                        val size = if (workTime == 1) {cellSize.value.width * groupSize} else {cellSize.value.width * groupSize + 5.dp}
                                        mergedCell(
                                            cellDataList = groupCells,
                                            isSelected = selectedCell == Pair(i, group * groupSize),
                                            onClick = { selectedCell = Pair(i, group * groupSize) },
                                            modifier = Modifier.size(size, cellSize.value.height),
                                            value = specialValue
                                        )
                                    } else {
                                        // Render each cell individually.
                                        groupCells.forEachIndexed { idx, cell ->
                                            RightClickMenu(cellData = cell, content = {
                                                spreadsheetCell(
                                                    cellData = cell,
                                                    isSelected = selectedCell == Pair(i, group * groupSize + idx),
                                                    onClick = { selectedCell = Pair(i, group * groupSize + idx) },
                                                    enabled = true,
                                                    modifier = Modifier.size(cellSize.value)
                                                )
                                            })
                                            if (workTime == 2 && idx == groupSize / 2 - 1) {
                                                Spacer(modifier = Modifier.width(5.dp))
                                            }
                                        }
                                    }
                                    // Insert the calculated cell (which is not editable).
                                    if (workTime == 1) {
                                        val simpleCalc = CalcStep.Calculation(
                                            CalcStep.CellValue(CellRef(i, group * groupSize)),
                                            CalcStep.CellValue(CellRef(i, group * groupSize + 1)),
                                            MinusOperation()
                                        )
                                        calcCell(
                                            modifier = Modifier.size(cellSize.value),
                                            calculation = simpleCalc,
                                            cells = cells,
                                        )
                                    } else {
                                        val complexCalc = CalcStep.Calculation(
                                            CalcStep.Calculation(
                                                CalcStep.CellValue(CellRef(i, group * groupSize)),
                                                CalcStep.CellValue(CellRef(i, group * groupSize + 1)),
                                                MinusOperation()
                                            ),
                                            CalcStep.Calculation(
                                                CalcStep.CellValue(CellRef(i, group * groupSize + 2)),
                                                CalcStep.CellValue(CellRef(i, group * groupSize + 3)),
                                                MinusOperation()
                                            ),
                                            PlusOperation()
                                        )
                                        calcCell(
                                            modifier = Modifier.size(cellSize.value),
                                            calculation = complexCalc,
                                            cells = cells,
                                        )
                                    }
                                    if (group < columns - 1) {
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            InfoPane(cellSize = cellSize, modifier = Modifier.weight(0.3f))
        }
    }
}


