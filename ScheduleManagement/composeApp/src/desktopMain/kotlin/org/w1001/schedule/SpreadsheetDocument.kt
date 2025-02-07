package org.w1001.schedule

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class SpreadsheetDocument(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("name")
    val name: String,
    val type: String,
    val workTime: Int,
    val cells: List<List<String>>,
    val createdAt: Long = System.currentTimeMillis()
)