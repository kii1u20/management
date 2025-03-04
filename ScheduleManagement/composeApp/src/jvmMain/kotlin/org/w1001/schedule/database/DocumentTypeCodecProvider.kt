package org.w1001.schedule.database

import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.w1001.schedule.DocumentType

class DocumentTypeCodecProvider : CodecProvider {
    override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        if (clazz == DocumentType::class.java) {
            @Suppress("UNCHECKED_CAST")
            return DocumentTypeCodec() as Codec<T>
        }
        return null
    }
}