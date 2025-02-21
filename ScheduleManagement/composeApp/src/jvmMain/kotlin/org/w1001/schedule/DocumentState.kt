package org.w1001.schedule

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList

sealed interface DocumentState {
    data class ScheduleState(
        val documentName: MutableState<String>,
        val numberOfColumns: MutableState<String>,
        val columnNames: SnapshotStateList<MutableState<String>>,
        val workTime: MutableState<Int>,
        val cells: SnapshotStateList<SnapshotStateList<CellData>>,
        val dayCellsData: List<CellData>,
        val calcCellBindings: HashMap<Int, MutableList<MutableState<Int>>>
    ) : DocumentState

    object Empty : DocumentState
}