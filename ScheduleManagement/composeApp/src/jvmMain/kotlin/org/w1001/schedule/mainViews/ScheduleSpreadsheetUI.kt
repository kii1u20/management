package org.w1001.schedule.mainViews

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.mongodb.MongoSocketException
import com.mongodb.MongoTimeoutException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.w1001.schedule.*
import org.w1001.schedule.cells.calcCell
import org.w1001.schedule.cells.mergedCell
import org.w1001.schedule.cells.spreadsheetCell
import org.w1001.schedule.components.*

val cellSize = mutableStateOf(DpSize(50.dp, 25.dp))

private val logger = KotlinLogging.logger("ScheduleSpreadsheetUI.kt")

@Composable
fun ScheduleSpreadsheetUI(
    viewModel: AppViewModel
) {
    val focusManager = LocalFocusManager.current
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val docState = viewModel.documentState.value as DocumentState.ScheduleState

    var previouslySelectedCell by remember { mutableStateOf<CellData?>(null) }
    var previouslySelectedRow by remember { mutableStateOf<CellData?>(null) }

    fun moveSelection(direction: Direction): Boolean {
        val currentCell = previouslySelectedCell ?: return false
        val nextCell = docState.getAdjacentCell(currentCell, direction) ?: return false

        // Update selection
        currentCell.isSelected.value = false
        nextCell.isSelected.value = true
        previouslySelectedCell = nextCell

        // Update row selection
        val newRowIndex = docState.cells.indexOfFirst { row -> row.any { it === nextCell } }
        previouslySelectedRow?.isSelected?.value = false
        docState.dayCellsData[newRowIndex].isSelected.value = true
        previouslySelectedRow = docState.dayCellsData[newRowIndex]

        return true
    }

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

        Box(modifier = Modifier.fillMaxSize().focusable().onPreviewKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                val isModifierPressed = if (System.getProperty("os.name").lowercase().contains("mac")) {
                    keyEvent.isMetaPressed  // Command key on macOS
                } else {
                    keyEvent.isCtrlPressed  // Control key on Windows/Linux
                }

                if (isModifierPressed) {
                    when (keyEvent.key) {
                        Key.DirectionUp -> moveSelection(Direction.UP)
                        Key.DirectionDown -> moveSelection(Direction.DOWN)
                        Key.DirectionLeft -> moveSelection(Direction.LEFT)
                        Key.DirectionRight -> moveSelection(Direction.RIGHT)
                        Key.Equals -> {
                            cellSize.value = DpSize(
                                (cellSize.value.width + 10.dp).coerceAtLeast(10.dp).coerceAtMost(200.dp),
                                (cellSize.value.height + 5.dp).coerceAtLeast(5.dp).coerceAtMost(100.dp)
                            )
                            true
                        }
                        Key.Minus -> {
                            cellSize.value = DpSize(
                                (cellSize.value.width - 10.dp).coerceAtLeast(10.dp),
                                (cellSize.value.height - 5.dp).coerceAtLeast(5.dp)
                            )
                            true
                        }
                        else -> false
                    }
                } else false
            } else false
        }) {
            Row(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    previouslySelectedCell?.isSelected?.value = false
                    previouslySelectedCell = null

                    previouslySelectedRow?.isSelected?.value = false
                    previouslySelectedRow = null
                })
            }) {
                Box(modifier = Modifier.fillMaxSize().weight(0.8f)) {
                    Column(Modifier.padding(end = 12.dp, bottom = 12.dp)) {
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
                                    createDayCell(docState.dayCellsData[i])
                                }
                            }
                            Column(
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
                                                if (previouslySelectedCell?.equals(it) == true) return@ScheduleRow
                                                previouslySelectedCell?.isSelected?.value = false
                                                it.isSelected.value = true
                                                previouslySelectedCell = it

                                                println(it.hashCode())
                                                previouslySelectedRow?.isSelected?.value = false
                                                docState.dayCellsData[i].isSelected.value = true
                                                previouslySelectedRow = docState.dayCellsData[i]
                                            },
                                            calcCellBindings = docState.calcCellBindings,
                                            viewModel = viewModel
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (verticalScrollState.maxValue > 0) {
                        VerticalScrollbar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd),
                            adapter = rememberScrollbarAdapter(verticalScrollState),
                            style = LocalScrollbarStyle.current.copy(
                                unhoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                hoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                                thickness = 10.dp
                            )
                        )
                    }
                    if (horizontalScrollState.maxValue > 0) {
                        HorizontalScrollbar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter),
                            adapter = rememberScrollbarAdapter(horizontalScrollState),
                            style = LocalScrollbarStyle.current.copy(
                                unhoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                hoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                                thickness = 10.dp
                            )
                        )
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
    LazyRow(Modifier.width(calculateRowWidth(workTime, cells)), userScrollEnabled = false) {
        item { Spacer(modifier = Modifier.width(10.dp)) }

        items(columns, key = { group -> "$rowIndex-$group" }) { group ->
            val groupCells = remember(cells[rowIndex], group, groupSize) {
                (0 until groupSize).map { idx -> cells[rowIndex][group * groupSize + idx] }
            }
            val specialValue = groupCells.firstOrNull { viewModel.specialMergeSet.contains(it.content.value) }
                ?.content?.value

            if (specialValue != null) {
                key(groupCells) {
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

            createCalcCell(calcCellBindings, group, rowIndex, cells, workTime, groupSize)

            if (group < columns - 1) {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
fun createDayCell(cellData: CellData) {
    spreadsheetCell(
        cellData = cellData,
        isSelected = cellData.isSelected.value,
        onClick = {},
        enabled = false,
        modifier = Modifier.size(cellSize.value),
    )
}

@Composable
fun createCalcCell(
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

// Needed for the LazyRow, since the parent column is horizontally scrollable too,
// and throws an exception without defined size
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


