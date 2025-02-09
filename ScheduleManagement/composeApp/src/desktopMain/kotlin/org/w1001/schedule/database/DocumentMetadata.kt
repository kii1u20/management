package org.w1001.schedule.database

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class DocumentMetadata(
    @BsonId
    val id: ObjectId,
    val name: String
)