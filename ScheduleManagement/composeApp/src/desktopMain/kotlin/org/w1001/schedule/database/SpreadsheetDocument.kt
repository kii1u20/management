package org.w1001.schedule.database

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class SpreadsheetDocument(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("name")
    val name: String,
    val type: String,
    val columnNames: List<String>,
    val cells: List<List<String>>,
    val createdAt: Long = System.currentTimeMillis()
)