package org.w1001.schedule

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class DocumentMetadata(
    @BsonId
    val id: ObjectId,
    val name: String
)