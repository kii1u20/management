package org.w1001.schedule

sealed class DocumentType {
    override fun toString(): String = this::class.simpleName ?: super.toString()

    data object Schedule1 : DocumentType()
    data object Schedule2 : DocumentType()
    // Add more document types here as needed

    companion object {
        fun values(): List<DocumentType> = DocumentType::class.nestedClasses
            .filter { it.objectInstance is DocumentType }
            .mapNotNull { it.objectInstance as? DocumentType }
    }
}