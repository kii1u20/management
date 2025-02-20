package org.w1001.schedule.mainViews

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mongodb.MongoSocketException
import com.mongodb.MongoTimeoutException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.w1001.schedule.*
import org.w1001.schedule.cells.calcCell
import org.w1001.schedule.cells.mergedCell
import org.w1001.schedule.cells.spreadsheetCell
import org.w1001.schedule.components.*
import org.w1001.schedule.database.SpreadsheetRepository

val cellSize = mutableStateOf(DpSize(50.dp, 25.dp))

private val logger = KotlinLogging.logger("ScheduleSpreadsheetUI.kt")

@Composable
fun ScheduleSpreadsheetUI(
    viewModel: AppViewModel
) {
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val focusManager = LocalFocusManager.current
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val scope = rememberCoroutineScope()
    val repository = remember { SpreadsheetRepository() }
    var currentDocumentId by remember { mutableStateOf<ObjectId?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val docState = viewModel.documentState.value as DocumentState.ScheduleState

    var previouslySelectedCell by remember { mutableStateOf<CellData?>(null) }

    MaterialTheme {
        if (viewModel.isSaving) {
            LoadingDialog("Saving document...")
        }

        if (errorMessage != null) {
            WarningDialog(
                message = errorMessage!!,
                onDismiss = { errorMessage = null }
            )
        }

        Row(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                selectedCell = null
            })
        }) {
            Column(Modifier.weight(0.8f)) {
                HeadingRow(
                    workTime = docState.workTime.value,
                    columns = docState.numberOfColumns.value.toInt(),
                    horizontalScrollState = horizontalScrollState,
                    calcCellBindings = docState.calcCellBindings,
                    docState = docState
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        Modifier.verticalScroll(verticalScrollState)
                            .width(cellSize.value.width), // Make the column scrollable
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (i in docState.cells.indices) {
                            createDayCell(i, selectedCell)
                        }
                    }
                    Column( //NOT FILING MAX SIZE OF ROW
                        Modifier.verticalScroll(verticalScrollState)
                            .horizontalScroll(horizontalScrollState).weight(1f), // Make the column scrollable
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (i in docState.cells.indices) {
                            key(i) { // Add key to help with recomposition
                                ScheduleRow(
                                    rowIndex = i,
                                    cells = docState.cells,
                                    columns = docState.numberOfColumns.value.toInt(),
                                    workTime = docState.workTime.value,
                                    onCellSelected = {
                                        println(it.hashCode())
                                        if (previouslySelectedCell?.equals(it) == true) return@ScheduleRow
                                        previouslySelectedCell?.isSelected?.value = false
                                        it.isSelected.value = true
                                        previouslySelectedCell = it
//                                        selectedCell = it
                                    },
                                    calcCellBindings = docState.calcCellBindings,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
            InfoPane(
                cellSize = cellSize,
                viewModel = viewModel,
                onSave = {
                    scope.launch {
                        try {
                            viewModel.saveDocument()
                        } catch (e: Exception) {
                            logger.error { e.stackTraceToString() }
                            errorMessage = when (e) {
                                is MongoSocketException -> "No internet connection"
                                is MongoTimeoutException -> "No internet connection"
                                else -> e.message ?: "An unknown error occurred"
                            }
                            viewModel.isSaving = false
                        }
                    }
                },
                modifier = Modifier.weight(0.2f)
            )
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
    onCellSelected: (CellData) -> Unit,
    calcCellBindings: HashMap<Int, MutableList<MutableState<Int>>>,
    viewModel: AppViewModel
) {
    val groupSize = remember { if (workTime == 1) 2 else 4 }
    LazyRow(Modifier.width(calculateRowWidth(workTime, cells))) {
        item { Spacer(modifier = Modifier.width(10.dp)) }

        items(columns, key = { group -> "$rowIndex-$group" }) { group ->
            val groupCells = remember(cells[rowIndex], group, groupSize) {
                (0 until groupSize).map { idx -> cells[rowIndex][group * groupSize + idx] }
            }
            val specialValue = groupCells.firstOrNull { viewModel.specialMergeSet.contains(it.content.value) }
                ?.content?.value

            if (specialValue != null) {
                RightClickMenu(
                    cellDataGroup = groupCells,
                    onRightClick = {
                        onCellSelected(groupCells.last())
                    }
                ) {
                    mergedCell(
                        cellDataList = groupCells,
                        isSelected = groupCells.last().isSelected.value,
                        onClick = { onCellSelected(groupCells.last()) },
                        modifier = Modifier.size(
                            if (workTime == 1) cellSize.value.width * groupSize
                            else cellSize.value.width * groupSize + 5.dp,
                            cellSize.value.height
                        ),
                        value = specialValue
                    )
                }
            } else {
                for (idx in groupCells.indices) {
                    val cellData = groupCells[idx]
                    key(cellData) {
                        RightClickMenu(
                            cellDataGroup = groupCells,
                            onRightClick = {
                                onCellSelected(groupCells.last())
                            }
                        ) {
                            spreadsheetCell(
                                cellData = cellData,
                                isSelected = cellData.isSelected.value,
                                onClick = { onCellSelected(cellData) },
                                enabled = true,
                                modifier = Modifier.size(cellSize.value)
//                                    .recomposeHighlighter()
                            )
                        }
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

fun calculateRowWidth(workTime: Int, cells: List<List<CellData>>): Dp {
    val cellsInRow = cells[0].size
    val columns = if (workTime == 1) cellsInRow / 2 else cellsInRow / 4

    return with(cellSize.value) {
        // Base width from all regular cells
        val baseCellsWidth = width * cellsInRow

        // Width from calculation columns (one per group)
        val calcColumnsWidth = width * columns

        // Initial spacer (10.dp) + spacing between groups (10.dp per group except last)
        val groupSpacersWidth = 10.dp + (10.dp * (columns - 1))

        // Extra 5.dp spacers in workTime == 2 (one per group)
        val extraSpacersWidth = if (workTime == 2) {
            5.dp * columns
        } else {
            0.dp
        }

        baseCellsWidth + calcColumnsWidth + groupSpacersWidth + extraSpacersWidth
    }
}


