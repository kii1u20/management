package org.w1001.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import org.w1001.schedule.printing.DocumentPrinter

private val logger = KotlinLogging.logger("SpreadsheetPrinter")

/**
 * Main entry point for printing documents in the application.
 * Uses the DocumentPrinter strategy pattern to delegate to appropriate
 * printer implementations.
 */
class SpreadsheetPrinter {
    companion object {
        /**
         * Prints a document based on its type.
         *
         * @param docState The document state to print
         */
        fun printDocument(docState: DocumentState) {
            try {
                val printer = DocumentPrinter.forDocument(docState)
                printer.print()
            } catch (e: IllegalArgumentException) {
                logger.error { "Cannot print document: ${e.message}" }
            }
        }
    }
}

