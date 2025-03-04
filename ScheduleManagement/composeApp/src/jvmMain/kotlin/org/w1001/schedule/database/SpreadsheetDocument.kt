package org.w1001.schedule.database

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import org.w1001.schedule.DocumentType

data class SpreadsheetDocument(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("name")
    val name: String,
    val type: DocumentType,
    val columnNames: List<String>,
    val cells: List<List<String>>,
    val createdAt: Long = System.currentTimeMillis(),
    val documentSettings: Map<String, String>// Flexible field for document settings
)