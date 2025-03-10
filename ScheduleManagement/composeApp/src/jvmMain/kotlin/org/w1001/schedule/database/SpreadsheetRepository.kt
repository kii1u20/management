package org.w1001.schedule.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.codecs.configuration.CodecRegistries
import org.bson.types.ObjectId
import org.w1001.schedule.CellData
import org.w1001.schedule.DocumentType
import java.util.concurrent.TimeUnit

class SpreadsheetRepository {
    private val logger = KotlinLogging.logger("SpreadsheetRepository.kt")
    private var client: MongoClient? = null

    fun initRepository(connectionString: String) {
        val codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(DocumentTypeCodecProvider()),
            MongoClientSettings.getDefaultCodecRegistry()
        )

        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .codecRegistry(codecRegistry)
            .applyToSocketSettings { builder ->
                builder.connectTimeout(8, TimeUnit.SECONDS)
                builder.readTimeout(8, TimeUnit.SECONDS)
            }
            .applyToServerSettings { builder ->
                builder.heartbeatFrequency(10, TimeUnit.SECONDS)
                builder.minHeartbeatFrequency(500, TimeUnit.MILLISECONDS)
            }
            .applyToClusterSettings { builder ->
                builder.serverSelectionTimeout(8, TimeUnit.SECONDS)
            }
            .build()
        client = MongoClient.create(settings)
    }

    private suspend fun createUniqueNameIndex(databaseName: String, collectionName: String) {
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        val database = client!!.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
        collection.createIndex(Indexes.ascending("name"), IndexOptions().unique(true))
    }

    suspend fun saveSpreadsheet(
        type: DocumentType,
        columnNames: List<String>,
        cells: List<List<CellData>>,
        name: String,
        documentSettings: Map<String, String>,
        databaseName: String,
        collectionName: String
    ): ObjectId {
//        ensureUniqueNameIndex(databaseName, collectionName)
        try {
            if (client == null) throw IllegalStateException("No MongoDB connection string available")
            val database: MongoDatabase = client!!.getDatabase(databaseName)
            val collection = database.getCollection<SpreadsheetDocument>(collectionName)
            val flattenedCells = cells.map { row ->
                row.map { it.content.value }
            }

            val document = SpreadsheetDocument(
                type = type,
                columnNames = columnNames,
                cells = flattenedCells,
                name = name,
                documentSettings = documentSettings
            )

            collection.insertOne(document)
            return document.id
        } catch (e: MongoWriteException) {
            logger.error { e.stackTraceToString() }
            if (e.error.code == 11000) { // Duplicate key error
                throw IllegalArgumentException("A document with name '$name' already exists")
            }
            throw e
        }

    }

    suspend fun updateSpreadsheet(
        id: ObjectId,
        type: DocumentType,
        columnNames: List<String>,
        cells: List<List<CellData>>,
        name: String,
        documentSettings: Map<String, String>,
        databaseName: String,
        collectionName: String
    ) {
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        val database: MongoDatabase = client!!.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)

        val flattenedCells = cells.map { row ->
            row.map { it.content.value }
        }

        val updatedDocument = SpreadsheetDocument(
            id = id,
            type = type,
            columnNames = columnNames,
            cells = flattenedCells,
            name = name,
            documentSettings = documentSettings
        )

        val result = collection.replaceOne(
            filter = org.bson.Document("_id", id),
            replacement = updatedDocument
        )

        if (result.matchedCount == 0L) {
            throw IllegalStateException("Document with id '$id' not found")
        }
    }

    suspend fun loadDocuments(databaseName: String, collectionName: String): List<SpreadsheetDocument> {
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        val database: MongoDatabase = client!!.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
        return collection.find().toList()
    }

    suspend fun loadDocumentMetadata(
        databaseName: String,
        collectionName: String
    ): List<DocumentMetadata> {
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        val database: MongoDatabase = client!!.getDatabase(databaseName)
        val collection = database.getCollection<DocumentMetadata>(collectionName)

        return collection.find()
            .projection(
                org.bson.Document(
                    mapOf(
                        "_id" to 1,
                        "name" to 1
                    )
                )
            )
            .toList()
    }

    suspend fun loadDocument(
        id: ObjectId,
        databaseName: String,
        collectionName: String
    ): SpreadsheetDocument? {
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        val database: MongoDatabase = client!!.getDatabase(databaseName)
        val collection = database.getCollection<SpreadsheetDocument>(collectionName)
        return collection.find(org.bson.Document("_id", id)).firstOrNull()
    }

    suspend fun getDatabases(): List<String> {
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        return client!!.listDatabaseNames()
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
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        val database: MongoDatabase = client!!.getDatabase(databaseName)
        return database.listCollectionNames().toList().sorted()
    }

    suspend fun createCollection(databaseName: String, collectionName: String): Boolean {
        if (client == null) throw IllegalStateException("No MongoDB connection string available")
        val database: MongoDatabase = client!!.getDatabase(databaseName)
        return try {
            // Check if collection already exists
            val collections = database.listCollectionNames().toList()
            if (collections.contains(collectionName)) {
                return false
            }

            database.createCollection(collectionName)
            createUniqueNameIndex(databaseName, collectionName)
            true
        } catch (e: Exception) {
            logger.error { e.stackTraceToString() }
            false
        }
    }

    suspend fun deleteDocumentByName(databaseName: String, collectionName: String, documentName: String): Boolean {
        return try {
            if (client == null) throw IllegalStateException("No MongoDB connection string available")
            val database = client!!.getDatabase(databaseName)
            val collection = database.getCollection<SpreadsheetDocument>(collectionName)
            val result = collection.deleteOne(org.bson.Document("name", documentName))
            return result.deletedCount > 0
        } catch (e: Exception) {
            logger.error { e.stackTraceToString() }
            false
        }
    }

    suspend fun deleteCollection(databaseName: String, collectionName: String): Boolean {
        return try {
            if (client == null) throw IllegalStateException("No MongoDB connection string available")
            val database = client!!.getDatabase(databaseName)
            database.getCollection<SpreadsheetDocument>(collectionName).drop()
            true
        } catch (e: Exception) {
            logger.error { e.stackTraceToString() }
            false
        }
    }
}