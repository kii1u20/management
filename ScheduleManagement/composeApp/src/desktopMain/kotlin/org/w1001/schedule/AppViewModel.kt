package org.w1001.schedule

import androidx.compose.runtime.*
import org.bson.types.ObjectId

data class CellData(var content: MutableState<String>)

class AppViewModel {
    // From main.kt
    val inMainMenu = mutableStateOf(true)
    val numberOfColumns = mutableStateOf("3")
    val workTime = mutableStateOf(1)
    val columnNames = mutableStateListOf<MutableState<String>>().apply {
        for (i in 0 until numberOfColumns.value.toInt()) {
            add(mutableStateOf("Column ${i + 1}"))
        }
    }

    // From App.kt
    var currentDocumentId by mutableStateOf<ObjectId?>(null)
    val specialMergeSet = hashSetOf("A", "B", "C")

    // Cells list // EXPERIENT THIS BEING MUTABLE LIST
    val cells = List(31) { row ->
        List(if (workTime.value == 1) numberOfColumns.value.toInt() * 2
        else numberOfColumns.value.toInt() * 4) { col ->
            CellData(mutableStateOf(""))
        }.toMutableStateList()
    }.toMutableStateList()

    val calcCellBindings = hashMapOf<Int, MutableList<MutableState<Int>>>().apply {
        for (group in 0 until numberOfColumns.value.toInt()) {
            this[group] = MutableList(31) { mutableStateOf(0) }
        }
    }

    fun loadDocument(document: SpreadsheetDocument) {
        workTime.value = document.workTime
        numberOfColumns.value = if (document.workTime == 1) {
            (document.cells[0].size / 2).toString()
        } else {
            (document.cells[0].size / 4).toString()
        }

        // Update cells list with new dimensions
        cells.clear()
        cells.addAll(document.cells.map { row ->
            row.map { CellData(mutableStateOf(it)) }.toMutableStateList()
        }.toMutableStateList())

        // Update calcCellBindings with new dimensions
        calcCellBindings.clear()
        for (group in 0 until numberOfColumns.value.toInt()) {
            calcCellBindings[group] = MutableList(31) { mutableStateOf(0) }
        }

        inMainMenu.value = false
    }
}