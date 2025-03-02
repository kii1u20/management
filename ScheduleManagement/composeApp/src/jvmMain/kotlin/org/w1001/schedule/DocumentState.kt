package org.w1001.schedule

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.math.BigDecimal

sealed interface DocumentState {
    data class ScheduleState(
        val documentName: MutableState<String>,
        val numberOfColumns: MutableState<String>,
        val columnNames: SnapshotStateList<MutableState<String>>,
        val workTime: MutableState<Int>,
        val cells: SnapshotStateList<SnapshotStateList<CellData>>,
        val dayCellsData: List<CellData>,
        val calcCellBindings: HashMap<Int, MutableList<MutableState<BigDecimal>>>,
        val documentSettings: Map<String, String>
    ) : DocumentState

    object Empty : DocumentState
}

fun DocumentState.ScheduleState.getAdjacentCell(
    currentCell: CellData,
    direction: Direction
): CellData? {
    val rowIndex = cells.indexOfFirst { row -> row.any { it === currentCell } }
    val colIndex = cells[rowIndex].indexOfFirst { it === currentCell }

    return when (direction) {
        Direction.UP -> if (rowIndex > 0) cells[rowIndex - 1][colIndex] else null
        Direction.DOWN -> if (rowIndex < cells.size - 1) cells[rowIndex + 1][colIndex] else null
        Direction.LEFT -> if (colIndex > 0) cells[rowIndex][colIndex - 1] else null
        Direction.RIGHT -> if (colIndex < cells[rowIndex].size - 1) cells[rowIndex][colIndex + 1] else null
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}