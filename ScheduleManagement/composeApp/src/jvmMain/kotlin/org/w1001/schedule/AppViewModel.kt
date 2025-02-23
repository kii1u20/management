package org.w1001.schedule

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.types.ObjectId
import org.w1001.schedule.database.SpreadsheetDocument
import org.w1001.schedule.database.SpreadsheetRepository

data class CellData(var content: MutableState<String>, var isSelected: MutableState<Boolean> = mutableStateOf(false))

class AppViewModel {
    var isSaving by mutableStateOf(false)
    var documentTypes = mutableStateListOf("schedule1", "schedule2")
    var inMainMenu = mutableStateOf(true)

    // Document state
    private var _documentState: MutableState<DocumentState> = mutableStateOf(DocumentState.Empty)
    val documentState: State<DocumentState> = _documentState

    // Document metadata
    var currentDocumentId by mutableStateOf<ObjectId?>(null)
    var isDocumentLoaded by mutableStateOf(false)
    var currentDocumentType by mutableStateOf<DocumentType?>(null)

    //Database metadata
    var currentDatabase by mutableStateOf("")
    var currentCollection by mutableStateOf("")

    val repository = SpreadsheetRepository()
    val specialMergeSet = hashSetOf("A", "B", "C")

    val fontSize: MutableState<Float> = mutableStateOf(14f)
    val enableAutoFontSize: MutableState<Boolean> = mutableStateOf(true)

    private val logger = KotlinLogging.logger("AppViewModel.kt")

    fun clearDocumentState() {
        _documentState.value = DocumentState.Empty
        logger.info { "Document state cleared" }
    }

    fun createNewSchedule(
        columns: String,
        columnNames: SnapshotStateList<MutableState<String>>,
        name: String,
        isSchedule1: Boolean
    ) {
        val scheduleState = DocumentState.ScheduleState(
            documentName = mutableStateOf(name),
            numberOfColumns = mutableStateOf(columns),
            columnNames = if (columnNames.isEmpty()) {
                SnapshotStateList<MutableState<String>>().apply {
                    for (i in 0 until columns.toInt()) {
                        add(mutableStateOf("Column ${i + 1}"))
                    }
                }
            } else {
                columnNames
            },
            workTime = if (isSchedule1) mutableStateOf(1) else mutableStateOf(2),
            cells = createScheduleCells(columns.toInt(), isSchedule1),
            dayCellsData = createDayCellsData(),
            calcCellBindings = createCalcBindings(columns.toInt())
        )

        clearLoadedDocument()
        _documentState.value = scheduleState
        currentDocumentType = if (isSchedule1) DocumentType.Schedule1 else DocumentType.Schedule2
        inMainMenu.value = false
        logger.info { "New schedule document created" }
    }

    fun loadDocument(document: SpreadsheetDocument) {
        when (document.type) {
            "schedule1" -> loadScheduleDocument(document, true)
            "schedule2" -> loadScheduleDocument(document, false)
            else -> throw IllegalArgumentException("Unknown document type: ${document.type}")
        }
    }

    private fun loadScheduleDocument(document: SpreadsheetDocument, isSchedule1: Boolean) {
        val columns = if (isSchedule1) {
            (document.cells[0].size / 2).toString()
        } else {
            (document.cells[0].size / 4).toString()
        }

        val scheduleState = DocumentState.ScheduleState(
            documentName = mutableStateOf(document.name),
            numberOfColumns = mutableStateOf(columns),
            columnNames = SnapshotStateList<MutableState<String>>().apply {
                addAll(document.columnNames.map { mutableStateOf(it) })
            },
            workTime = if (isSchedule1) mutableStateOf(1) else mutableStateOf(2),
            cells = document.cells.map { row ->
                row.map { CellData(mutableStateOf(it)) }.toMutableStateList()
            }.toMutableStateList(),
            dayCellsData = createDayCellsData(),
            calcCellBindings = createCalcBindings(columns.toInt())
        )

        _documentState.value = scheduleState
        currentDocumentType = if (isSchedule1) DocumentType.Schedule1 else DocumentType.Schedule2
        isDocumentLoaded = true
        currentDocumentId = document.id
        inMainMenu.value = false
        logger.info { "Schedule Document loaded: ${document.name} ; database: $currentDatabase ; collection: $currentCollection" }
    }

    private fun createScheduleCells(
        columns: Int,
        isSchedule1: Boolean
    ): SnapshotStateList<SnapshotStateList<CellData>> {
        return List(31) { row ->
            List(
                if (isSchedule1) columns * 2 else columns * 4
            ) { col ->
                CellData(mutableStateOf(""))
            }.toMutableStateList()
        }.toMutableStateList()
    }

    private fun createDayCellsData(): List<CellData> {
        return List(31) { row ->
            CellData(mutableStateOf("${row + 1}"))
        }
    }

    //Can be a second hashmap of rowIndex to MutableSate<Int> if needed
    private fun createCalcBindings(columns: Int): HashMap<Int, MutableList<MutableState<Int>>> {
        return hashMapOf<Int, MutableList<MutableState<Int>>>().apply {
            for (group in 0 until columns) {
                this[group] = MutableList(31) { mutableStateOf(0) }
            }
        }
    }

    fun clearLoadedDocument() {
        isDocumentLoaded = false
        currentDocumentId = null
        currentDocumentType = null
        _documentState.value = DocumentState.Empty
        logger.info { "Loaded document cleared" }
    }

    suspend fun saveDocument() {
        isSaving = true
        try {
            when (val state = _documentState.value) {
                is DocumentState.ScheduleState -> saveScheduleDocument(state)
                DocumentState.Empty -> throw IllegalStateException("Cannot save empty document")
            }
        } finally {
            isSaving = false
        }
    }

    private suspend fun saveScheduleDocument(state: DocumentState.ScheduleState) {
        val isSchedule1 = currentDocumentType == DocumentType.Schedule1
        val documentType = if (isSchedule1) "schedule1" else "schedule2"

        if (isDocumentLoaded && currentDocumentId != null) {
            repository.updateSpreadsheet(
                id = currentDocumentId!!,
                type = documentType,
                columnNames = state.columnNames.map { it.value },
                cells = state.cells,
                name = state.documentName.value,
                databaseName = currentDatabase,
                collectionName = currentCollection
            )
            logger.info { "Schedule Document updated: ${state.documentName.value} ; database: $currentDatabase ; collection: $currentCollection" }
        } else {
            currentDocumentId = repository.saveSpreadsheet(
                type = documentType,
                columnNames = state.columnNames.map { it.value },
                cells = state.cells,
                name = state.documentName.value,
                databaseName = currentDatabase,
                collectionName = currentCollection
            )
            isDocumentLoaded = true
            logger.info { "Schedule Document saved: ${state.documentName.value} ; database: $currentDatabase ; collection: $currentCollection" }
        }
    }
}
