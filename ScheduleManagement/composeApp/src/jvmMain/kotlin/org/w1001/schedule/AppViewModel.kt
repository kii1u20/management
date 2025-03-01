package org.w1001.schedule

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.bson.types.ObjectId
import org.w1001.schedule.database.SpreadsheetDocument
import org.w1001.schedule.database.SpreadsheetRepository
import java.math.BigDecimal
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

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

    lateinit var repository:SpreadsheetRepository
    var isRepositoryInitialized by mutableStateOf(false)

    val specialMergeSet = hashSetOf("A", "B", "C")

    val fontSize: MutableState<Float> = mutableStateOf(14f)
    val enableAutoFontSize: MutableState<Boolean> = mutableStateOf(true)

    // Print settings
    var companyName by mutableStateOf("")
    var storeName by mutableStateOf("")
    val createdBy = "Ivan Ivanov" // Constant creator name
    var showPrintDialog by mutableStateOf(false)

    private val logger = KotlinLogging.logger("AppViewModel.kt")

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        SwingUtilities.invokeAndWait {
            runBlocking {
                if (!isInternetAvailable()) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Няма връзка с интернет. Проверете връзката и опитайте отново",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    exitProcess(1)
                }
            }
        }
        scope.launch {
            CredentialManager.hasCredentialsFlow.collect { hasCredentials ->
                if (hasCredentials) {
                    createRepository()
                } else {
                    isRepositoryInitialized = false
                }
            }
        }
    }

    suspend fun isInternetAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = java.net.Socket()
                val socketAddress = java.net.InetSocketAddress("8.8.8.8", 53)
                socket.connect(socketAddress, 3000) // 3 seconds timeout
                socket.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun createRepository() {
        val connString = CredentialManager.getConnectionString() ?: return
        repository = SpreadsheetRepository()
        repository.initRepository(connString)
        isRepositoryInitialized = ::repository.isInitialized
    }

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
        val columnsInt = columns.toIntOrNull() ?: 0
        if (columnsInt <= 0) {
            logger.error { "Invalid number of columns: $columns" }
            throw IllegalArgumentException("Invalid number of columns passed to createNewSchedule in AppViewModel.kt: $columns")
        } else if (name.isEmpty()) {
            logger.error { "Invalid name: $name" }
            throw IllegalArgumentException("Invalid name passed to createNewSchedule in AppViewModel.kt: $name")
        }
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
    private fun createCalcBindings(columns: Int): HashMap<Int, MutableList<MutableState<BigDecimal>>> {
        return hashMapOf<Int, MutableList<MutableState<BigDecimal>>>().apply {
            for (group in 0 until columns) {
                this[group] = MutableList(31) { mutableStateOf(BigDecimal("0.0")) }
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
            if (currentDatabase.isEmpty() || currentCollection.isEmpty()) {
                logger.error { "Database or collection name is empty" }
                throw IllegalStateException("Database or collection name is empty")
            }
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

    fun printCurrentDocument() {
        showPrintDialog = true
    }

    fun executePrint(companyName: String, storeName: String) {
        this.companyName = companyName
        this.storeName = storeName

        val state = documentState.value
        SpreadsheetPrinter.printDocument(state)
    }
}
