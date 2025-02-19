package org.w1001.schedule

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.w1001.schedule.database.SpreadsheetDocument
import kotlin.test.*

class AppViewModelTest {
    private lateinit var viewModel: AppViewModel

    @BeforeTest
    fun setup() {
        viewModel = AppViewModel()
    }

    @Test
    fun `test create new schedule1`() {
        val columnNames = SnapshotStateList<MutableState<String>>()
        viewModel.createNewSchedule("3", columnNames, "Test Schedule", true)

        val state = viewModel.documentState.value as? DocumentState.ScheduleState
        assertNotNull(state)
        assertEquals("Test Schedule", state.documentName.value)
        assertEquals("3", state.numberOfColumns.value)
        assertEquals(1, state.workTime.value)
        assertEquals(31, state.cells.size)
        assertEquals(6, state.cells[0].size) // 3 columns * 2 for schedule1
    }

    @Test
    fun `test create new schedule2`() {
        val columnNames = SnapshotStateList<MutableState<String>>()
        viewModel.createNewSchedule("3", columnNames, "Test Schedule", false)

        val state = viewModel.documentState.value as? DocumentState.ScheduleState
        assertNotNull(state)
        assertEquals(2, state.workTime.value)
        assertEquals(12, state.cells[0].size) // 3 columns * 4 for schedule2
    }

    @Test
    fun `test load schedule document`() {
        val document = SpreadsheetDocument(
            id = ObjectId(),
            type = "schedule1",
            name = "Test Load",
            columnNames = listOf("Col1", "Col2"),
            cells = List(31) { List(4) { "" } }
        )

        viewModel.loadDocument(document)

        assertTrue(viewModel.isDocumentLoaded)
        assertEquals(document.id, viewModel.currentDocumentId)
        assertEquals(document.name, viewModel.loadedDocumentName)
        assertEquals(DocumentType.Schedule1, viewModel.currentDocumentType)
    }

    @Test
    fun `test clear loaded document`() {
        // First load a document
        val document = SpreadsheetDocument(
            id = ObjectId(),
            type = "schedule1",
            name = "Test Clear",
            columnNames = listOf("Col1", "Col2"),
            cells = List(31) { List(4) { "" } }
        )
        viewModel.loadDocument(document)

        // Then clear it
        viewModel.clearLoadedDocument()

        assertFalse(viewModel.isDocumentLoaded)
        assertNull(viewModel.currentDocumentId)
        assertNull(viewModel.loadedDocumentName)
        assertNull(viewModel.currentDocumentType)
        assertTrue(viewModel.documentState.value is DocumentState.Empty)
    }

    @Test
    fun `test save document without loading first`() {
        viewModel.createNewSchedule("3", SnapshotStateList(), "New Doc", true)
        
        assertFalse(viewModel.isDocumentLoaded)
        assertNull(viewModel.currentDocumentId)
        
        runBlocking {
            viewModel.saveDocument()
        }
        
        assertTrue(viewModel.isDocumentLoaded)
        assertNotNull(viewModel.currentDocumentId)
        assertEquals("New Doc", viewModel.loadedDocumentName)
    }

    @Test
    fun `test invalid document type loading`() {
        val document = SpreadsheetDocument(
            id = ObjectId(),
            type = "invalid_type",
            name = "Invalid Doc",
            columnNames = listOf(),
            cells = listOf()
        )

        assertFailsWith<IllegalArgumentException> {
            viewModel.loadDocument(document)
        }
    }
}
