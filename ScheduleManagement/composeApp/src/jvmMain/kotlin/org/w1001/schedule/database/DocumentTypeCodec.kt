package org.w1001.schedule.database

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.w1001.schedule.DocumentType

class DocumentTypeCodec : Codec<DocumentType> {
    override fun encode(writer: BsonWriter, value: DocumentType, encoderContext: EncoderContext) {
        writer.writeString(when(value) {
            DocumentType.Schedule1 -> "Schedule1"
            DocumentType.Schedule2 -> "Schedule2"
            else -> throw IllegalArgumentException("Unknown DocumentType: $value")
        })
    }

    override fun getEncoderClass(): Class<DocumentType> {
        return DocumentType::class.java
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): DocumentType {
        val value = reader.readString()
        return when (value) {
            "Schedule1" -> DocumentType.Schedule1
            "Schedule2" -> DocumentType.Schedule2
            else -> throw IllegalArgumentException("Unknown DocumentType: $value")
        }
    }
}