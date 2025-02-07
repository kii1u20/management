package org.w1001.schedule

import com.mongodb.MongoWriteException
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class SpreadsheetRepository {
    private val connectionString =
        "mongodb+srv://ivanovikristian01:hBL5k2xzhg943z3s@cluster0.ckezs.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
    private val client = MongoClient.create(connectionString)

//    private suspend fun ensureUniqueNameIndex(databaseName: String, collectionName: String) {
//        val database = client.getDatabase(databaseName)
//        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
//        collection.createIndex(Indexes.ascending("name"), IndexOptions().unique(true))
//    }

    suspend fun saveSpreadsheet(
        type: String,
        workTime: Int,
        cells: List<List<CellData>>,
        name: String,
        databaseName: String,
        collectionName: String
    ): ObjectId {
//        ensureUniqueNameIndex(databaseName, collectionName)
        try {
            val database: MongoDatabase = client.getDatabase(databaseName)
            val collection = database.getCollection<SpreadsheetDocument>(collectionName)
            val flattenedCells = cells.map { row ->
                row.map { it.content.value }
            }

            val document = SpreadsheetDocument(
                type = type,
                workTime = workTime,
                cells = flattenedCells,
                name = name
            )

            collection.insertOne(document)
            return document.id
        } catch (e: MongoWriteException) {
            if (e.error.code == 11000) { // Duplicate key error
                throw IllegalArgumentException("A document with name '$name' already exists")
            }
            throw e
        }

    }

    suspend fun updateSpreadsheet(
        id: ObjectId,
        type: String,
        workTime: Int,
        cells: List<List<CellData>>,
        name: String,
        databaseName: String,
        collectionName: String
    ) {
        val database: MongoDatabase = client.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)

        val flattenedCells = cells.map { row ->
            row.map { it.content.value }
        }

        val updatedDocument = SpreadsheetDocument(
            id = id,
            type = type,
            workTime = workTime,
            cells = flattenedCells,
            name = name
        )

        collection.replaceOne(
            filter = org.bson.Document("_id", id),
            replacement = updatedDocument
        )
    }

    suspend fun loadDocuments(databaseName: String, collectionName: String): List<SpreadsheetDocument> {
        val database: MongoDatabase = client.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
        return collection.find().toList()
    }
}