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
import org.w1001.schedule.cells.calcCell
import org.w1001.schedule.cells.mergedCell
import org.w1001.schedule.cells.spreadsheetCell

data class CellData(var content: MutableState<String>)

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
    val calcCellBindings = remember {
        // Initialize with all bindings upfront
        hashMapOf<Int, MutableList<MutableState<Int>>>().apply {
            for (group in 0 until columns) {
                this[group] = MutableList(31) { mutableStateOf(0) }
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
                HeadingRow(
                    workTime = workTime,
                    columns = columns,
                    horizontalScrollState = horizontalScrollState,
                    calcCellBindings = calcCellBindings
                )

                Row() {
                    Column(
                        Modifier.verticalScroll(verticalScrollState), // Make the column scrollable
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (i in cells.indices) {
                            createDayCell(i, selectedCell)
                        }
                    }
                    Column(
                        Modifier.verticalScroll(verticalScrollState)
                            .horizontalScroll(horizontalScrollState),
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (i in cells.indices) {
                            key(i) { // Add key to help with recomposition
                                ScheduleRow(
                                    rowIndex = i,
                                    cells = cells,
                                    columns = columns,
                                    workTime = workTime,
                                    selectedCell = selectedCell,
                                    onCellSelected = { selectedCell = it },
                                    calcCellBindings = calcCellBindings
                                )
                            }
                        }
                    }
                }
            }
            InfoPane(cellSize = cellSize, modifier = Modifier.weight(0.3f))
        }
    }
}

// First add this new composable
@Composable
private fun ScheduleRow(
    rowIndex: Int,
    cells: List<List<CellData>>,
    columns: Int,
    workTime: Int,
    selectedCell: Pair<Int, Int>?,
    onCellSelected: (Pair<Int, Int>) -> Unit,
    calcCellBindings: HashMap<Int, MutableList<MutableState<Int>>>
) {
    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        val groupSize = if (workTime == 1) 2 else 4

        for (group in 0 until columns) {
            val groupCells = (0 until groupSize).map { idx -> cells[rowIndex][group * groupSize + idx] }
            val specialValue = groupCells.firstOrNull { specialMergeSet.contains(it.content.value) }?.content?.value

            if (specialValue != null) {
                val size = if (workTime == 1) {
                    cellSize.value.width * groupSize
                } else {
                    cellSize.value.width * groupSize + 5.dp
                }
                RightClickMenu(
                    cellDataGroup = groupCells,
                    onRightClick = {
                        onCellSelected(Pair(rowIndex, group * groupSize + groupSize - 1))
                    }
                ) {
                    mergedCell(
                        cellDataList = groupCells,
                        isSelected = selectedCell == Pair(rowIndex, group * groupSize + groupSize - 1),
                        onClick = { onCellSelected(Pair(rowIndex, group * groupSize + groupSize - 1)) },
                        modifier = Modifier.size(size, cellSize.value.height),
                        value = specialValue
                    )
                }
            } else {
                groupCells.forEachIndexed { idx, cell ->
                    RightClickMenu(
                        cellDataGroup = groupCells,
                        onRightClick = {
                            onCellSelected(Pair(rowIndex, group * groupSize + groupSize - 1))
                        }
                    ) {
                        spreadsheetCell(
                            cellData = cell,
                            isSelected = selectedCell == Pair(rowIndex, group * groupSize + idx),
                            onClick = { onCellSelected(Pair(rowIndex, group * groupSize + idx)) },
                            enabled = true,
                            modifier = Modifier.size(cellSize.value)
                        )
                    }

                    if (workTime == 2 && idx == groupSize / 2 - 1) {
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }
            }

            createCalculationColumn(calcCellBindings, group, rowIndex, cells, workTime, groupSize)

            if (group < columns - 1) {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
fun createDayCell(row: Int, selectedCell: Pair<Int, Int>?) {
    spreadsheetCell(
        cellData = CellData(mutableStateOf("${row + 1}")),
        isSelected = if ((selectedCell?.first ?: false) == row) true else false,
        onClick = {},
        enabled = false,
        modifier = Modifier.size(cellSize.value),
    )
}

@Composable
fun createCalculationColumn(
    calcCellBindings: HashMap<Int, MutableList<MutableState<Int>>>,
    group: Int,
    i: Int,
    cells: List<List<CellData>>,
    workTime: Int,
    groupSize: Int
) {
    val groupBindings = calcCellBindings[group]!!
    val calcBinding = groupBindings[i]
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
            resultBinding = calcBinding
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
            resultBinding = calcBinding
        )
    }
}


