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
    var inMainMenu = mutableStateOf(true)

    // Document state
    private var _documentState: MutableState<DocumentState> = mutableStateOf(DocumentState.Empty)
    val documentState: State<DocumentState> = _documentState

    // Document metadata
    var currentDocumentId by mutableStateOf<ObjectId?>(null)
    var isDocumentLoaded by mutableStateOf(false)

    //Database metadata
    var currentDatabase by mutableStateOf("")
    var currentCollection by mutableStateOf("")

    lateinit var repository:SpreadsheetRepository
    var isRepositoryInitialized by mutableStateOf(false)

    val specialMergeSet = hashSetOf("A", "B", "C")

    val fontSize: MutableState<Float> = mutableStateOf(14f)
    val enableAutoFontSize: MutableState<Boolean> = mutableStateOf(true)

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

    fun createNewDocument(
        docState: DocumentState
    ) {
        this.clearLoadedDocument()
        _documentState.value = docState
        inMainMenu.value = false
        logger.info { "New document created successfully" }
    }

    fun loadDocument(document: SpreadsheetDocument) {
        when (document.type) {
            DocumentType.Schedule1 -> loadScheduleDocument(document, true)
            DocumentType.Schedule2 -> loadScheduleDocument(document, false)
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
            type = document.type,
            numberOfColumns = mutableStateOf(columns),
            columnNames = SnapshotStateList<MutableState<String>>().apply {
                addAll(document.columnNames.map { mutableStateOf(it) })
            },
            workTime = if (isSchedule1) mutableStateOf(1) else mutableStateOf(2),
            cells = document.cells.map { row ->
                row.map { CellData(mutableStateOf(it)) }.toMutableStateList()
            }.toMutableStateList(),
            dayCellsData = createDayCellsData(),
            calcCellBindings = createCalcBindings(columns.toInt()),
            documentSettings = document.documentSettings
        )

        _documentState.value = scheduleState
        isDocumentLoaded = true
        currentDocumentId = document.id
        inMainMenu.value = false
        logger.info { "Schedule Document loaded: ${document.name} ; database: $currentDatabase ; collection: $currentCollection" }
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
        if (isDocumentLoaded && currentDocumentId != null) {
            repository.updateSpreadsheet(
                id = currentDocumentId!!,
                type = state.type,
                columnNames = state.columnNames.map { it.value },
                cells = state.cells,
                name = state.documentName.value,
                documentSettings = state.documentSettings,
                databaseName = currentDatabase,
                collectionName = currentCollection
            )
            logger.info { "Schedule Document updated: ${state.documentName.value} ; database: $currentDatabase ; collection: $currentCollection" }
        } else {
            repository.saveSpreadsheet(
                type = state.type,
                columnNames = state.columnNames.map { it.value },
                cells = state.cells,
                name = state.documentName.value,
                documentSettings = state.documentSettings,
                databaseName = currentDatabase,
                collectionName = currentCollection
            )
            isDocumentLoaded = true
            logger.info { "Schedule Document saved: ${state.documentName.value} ; database: $currentDatabase ; collection: $currentCollection" }
        }
    }
}
