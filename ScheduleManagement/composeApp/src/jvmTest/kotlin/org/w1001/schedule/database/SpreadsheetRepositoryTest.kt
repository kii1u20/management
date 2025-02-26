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

    @Test
    fun `test save document with empty cells`() = runBlocking {
        val cells = List(31) { List(4) { CellData(mutableStateOf("")) } }
        val docName = "Empty Cells Test ${System.currentTimeMillis()}"
        val id = repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = cells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )
        val loadedDoc = repository.loadDocument(id, testDatabase, testCollection)
        assertNotNull(loadedDoc)
        assertEquals("", loadedDoc.cells[0][0])
    }

    @Test
    fun `test save document with special characters in name`() = runBlocking {
        val cells = List(31) { List(4) { CellData(mutableStateOf("")) } }
        val docName = "Special @#$%^&* ${System.currentTimeMillis()}"
        val id = repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = cells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )
        val loadedDoc = repository.loadDocument(id, testDatabase, testCollection)
        assertNotNull(loadedDoc)
        assertEquals(docName, loadedDoc.name)
    }

    @Test
    fun `test update non-existent document`(): Unit = runBlocking {
        val nonExistentId = ObjectId()
        val cells = List(31) { List(4) { CellData(mutableStateOf("")) } }
        
        assertFailsWith<IllegalStateException> {
            repository.updateSpreadsheet(
                id = nonExistentId,
                type = "schedule1",
                columnNames = listOf("Col1"),
                cells = cells,
                name = "Non-existent Doc",
                databaseName = testDatabase,
                collectionName = testCollection
            )
        }
    }

    @Test
    fun `test load document from non-existent collection`() = runBlocking {
        val id = ObjectId()
        val loadedDoc = repository.loadDocument(
            id,
            testDatabase,
            "non_existent_collection_${System.currentTimeMillis()}"
        )
        assertNull(loadedDoc)
    }

    @Test
    fun `test save document with high cell content`() = runBlocking {
        val largeContent = "A".repeat(1000)  // Test with 1000 character string
        val cells = List(31) { List(4) { CellData(mutableStateOf(largeContent)) } }
        val docName = "Large Content Test ${System.currentTimeMillis()}"
        
        val id = repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = cells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )
        
        val loadedDoc = repository.loadDocument(id, testDatabase, testCollection)
        assertNotNull(loadedDoc)
        assertEquals(largeContent, loadedDoc.cells[0][0])
    }

    @Test
    fun `test delete document by name`() = runBlocking {
        // First create a document
        val cells = List(31) { List(4) { CellData(mutableStateOf("test")) } }
        val docName = "Delete Test ${System.currentTimeMillis()}"
        
        val id = repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = cells,
            name = docName,
            databaseName = testDatabase,
            collectionName = testCollection
        )
        
        // Verify document exists
        var loadedDoc = repository.loadDocument(id, testDatabase, testCollection)
        assertNotNull(loadedDoc)
        
        // Delete the document
        val deleteResult = repository.deleteDocumentByName(testDatabase, testCollection, docName)
        assertTrue(deleteResult)
        
        // Verify document no longer exists
        loadedDoc = repository.loadDocument(id, testDatabase, testCollection)
        assertNull(loadedDoc)
        
        // Try to delete non-existent document
        val secondDeleteResult = repository.deleteDocumentByName(testDatabase, testCollection, "non-existent-doc")
        assertFalse(secondDeleteResult)
    }

    @Test
    fun `test delete collection`() = runBlocking {
        // Create a new collection
        val tempCollection = "temp_collection_${System.currentTimeMillis()}"
        val created = repository.createCollection(testDatabase, tempCollection)
        assertTrue(created)
        
        // Add a document to the collection
        val cells = List(31) { List(4) { CellData(mutableStateOf("test")) } }
        repository.saveSpreadsheet(
            type = "schedule1",
            columnNames = listOf("Col1"),
            cells = cells,
            name = "Test Doc",
            databaseName = testDatabase,
            collectionName = tempCollection
        )
        
        // Verify collection exists
        var collections = repository.getCollectionNames(testDatabase)
        assertTrue(collections.contains(tempCollection))
        
        // Delete the collection
        val deleteResult = repository.deleteCollection(testDatabase, tempCollection)
        assertTrue(deleteResult)
        
        // Verify collection no longer exists
        collections = repository.getCollectionNames(testDatabase)
        assertFalse(collections.contains(tempCollection))
        
        // Try to delete non-existent collection
        val secondDeleteResult = repository.deleteCollection(testDatabase, "non-existent-collection")
        assertFalse(secondDeleteResult)
    }
}
