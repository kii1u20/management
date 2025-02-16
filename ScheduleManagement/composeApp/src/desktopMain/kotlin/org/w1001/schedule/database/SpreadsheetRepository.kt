package org.w1001.schedule.database

import com.mongodb.MongoWriteException
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.w1001.schedule.CellData

class SpreadsheetRepository {
    //MongoDB Atlas backend connection string
    private val connectionString =
        "mongodb+srv://ivanovikristian01:hBL5k2xzhg943z3s@cluster0.ckezs.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
    //Azure CosmosDB for MongoDB backend connection string
//    private val connectionString =
//        "mongodb://managementapp:KdfhqDIK0oLdoYZ7vJby9QC7l0cqFRdGRPXENb3ofz89BeMCPrgqqVZrJ8kdcJCQASGaFbiuRfwRACDbMQDmkw==@managementapp.mongo.cosmos.azure.com:10255/?ssl=true&retrywrites=false&replicaSet=globaldb&maxIdleTimeMS=120000&appName=@managementapp@"
    private val client = MongoClient.create(connectionString)

//    private suspend fun ensureUniqueNameIndex(databaseName: String, collectionName: String) {
//        val database = client.getDatabase(databaseName)
//        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
//        collection.createIndex(Indexes.ascending("name"), IndexOptions().unique(true))
//    }

    suspend fun saveSpreadsheet(
        type: String,
        columnNames: List<String>,
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
                columnNames = columnNames,
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
        columnNames: List<String>,
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
            columnNames = columnNames,
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

    suspend fun loadDocumentMetadata(
        databaseName: String,
        collectionName: String
    ): List<DocumentMetadata> {
        val database: MongoDatabase = client.getDatabase(databaseName)
        val collection = database.getCollection<DocumentMetadata>(collectionName)

        return collection.find()
            .projection(org.bson.Document(mapOf(
                "_id" to 1,
                "name" to 1
            )))
            .toList()
    }

    suspend fun loadDocument(
        id: ObjectId,
        databaseName: String,
        collectionName: String
    ): SpreadsheetDocument? {
        val database: MongoDatabase = client.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
        return collection.find(org.bson.Document("_id", id)).firstOrNull()
    }

    suspend fun getDatabases(): List<String> {
        return client.listDatabaseNames()
            .toList()
            .filter { it != "admin" && it != "local" }
    }
//
//    suspend fun getUniqueDocumentTypes(databaseName: String, collectionName: String): List<String> {
//        val database: MongoDatabase = client.getDatabase(databaseName)
//        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
//
//        return collection.distinct<String>("type").toList()
//    }

    suspend fun getCollectionNames(databaseName: String): List<String> {
        val database: MongoDatabase = client.getDatabase(databaseName)
        return database.listCollectionNames().toList().sorted()
    }

    suspend fun createCollection(databaseName: String, collectionName: String): Boolean {
        val database: MongoDatabase = client.getDatabase(databaseName)
        return try {
            // Check if collection already exists
            val collections = database.listCollectionNames().toList()
            if (collections.contains(collectionName)) {
                return false
            }

            database.createCollection(collectionName)
            true
        } catch (e: Exception) {
            // Log the error if needed
            println("Failed to create collection: ${e.message}")
            false
        }
    }

    suspend fun deleteDocumentByName(databaseName: String, collectionName: String, documentName: String): Boolean {
        val database = client.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
        val result = collection.deleteOne(org.bson.Document("name", documentName))
        return result.deletedCount > 0
    }

    suspend fun deleteCollection(databaseName: String, collectionName: String): Boolean {
        val database = client.getDatabase(databaseName)
        return try {
            database.getCollection<SpreadsheetDocument>(collectionName).drop()
            true
        } catch (e: Exception) {
            false
        }
    }
}