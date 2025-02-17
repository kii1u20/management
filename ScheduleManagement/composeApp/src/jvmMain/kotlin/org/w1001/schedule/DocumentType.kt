package org.w1001.schedule

sealed interface DocumentType {
    object Schedule1 : DocumentType
    object Schedule2 : DocumentType
    // Add more document types here as needed
}