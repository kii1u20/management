package org.w1001.schedule.database

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.w1001.schedule.CellData
import kotlin.test.*

class SpreadsheetRepositoryTest {
    private lateinit var repository: SpreadsheetRepository
    private val testDatabase = "test_db"
    private val testCollection = "test_collection"

    @BeforeTest
    fun setup() {
        repository = SpreadsheetRepository()
    }

    @Test
    fun `test save and load document`() = runBlocking {
        val cells = List(31) { row ->
            List(4) { col ->
                CellData(mutableStateOf("$row,$col"))
            }
        }

        val docName = "Test Document ${System.currentTimeMillis()}"
        val id = repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1", "Col2"),
            cells = cells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )

        val loadedDoc = repository.loadDocument(id, testDatabase, testCollection)
        assertNotNull(loadedDoc)
        assertEquals(docName, loadedDoc.name)
        assertEquals("schedule1", loadedDoc.type)
        assertEquals(31, loadedDoc.cells.size)
    }

    @Test
    fun `test duplicate document name`(): Unit = runBlocking {
        val cells = List(31) { List(4) { CellData(mutableStateOf("")) } }
        val docName = "Duplicate Test ${System.currentTimeMillis()}"

        repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = cells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )

        assertFailsWith<IllegalArgumentException> {
            repository.saveSpreadsheet(
                type = "schedule1",
                columnNames = listOf("Col1"),
                cells = cells,
                name = docName,
                databaseName = testDatabase,
                collectionName = testCollection
            )
        }
    }

    @Test
    fun `test update document`() = runBlocking {
        val cells = List(31) { List(4) { CellData(mutableStateOf("original")) } }
        val docName = "Update Test ${System.currentTimeMillis()}"

        val id = repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = cells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )

        val updatedCells = List(31) { List(4) { CellData(mutableStateOf("updated")) } }
        repository.updateSpreadsheet(
            id = id,
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = updatedCells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )

        val loadedDoc = repository.loadDocument(id, testDatabase, testCollection)
        assertNotNull(loadedDoc)
        assertEquals("updated", loadedDoc.cells[0][0])
    }

    @Test
    fun `test load non-existent document`() = runBlocking {
        val nonExistentId = ObjectId()
        val loadedDoc = repository.loadDocument(
            nonExistentId,
            testDatabase,
            testCollection
        )
        assertNull(loadedDoc)
    }

    @Test
    fun `test create and verify collection`() = runBlocking {
        val newCollection = "new_test_collection_${System.currentTimeMillis()}"
        
        val created = repository.createCollection(testDatabase, newCollection)
        assertTrue(created)

        val collections = repository.getCollectionNames(testDatabase)
        assertTrue(collections.contains(newCollection))

        // Try to create same collection again
        val secondAttempt = repository.createCollection(testDatabase, newCollection)
        assertFalse(secondAttempt)
    }
}
