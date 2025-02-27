package org.w1001.schedule.printing

import org.w1001.schedule.DocumentState
import java.awt.print.PrinterException

/**
 * Interface defining the contract for document printing functionality.
 * Different document types can implement this interface.
 */
interface DocumentPrinter<T : DocumentState> {
    /**
     * The document state to be printed
     */
    val documentState: T
    
    /**
     * Prints the document, showing a printer dialog and handling the printing process
     * 
     * @return true if printing was successful or canceled by user, false if an error occurred
     */
    fun print(): Boolean
    
    /**
     * Returns the job name to be displayed in the printer dialog
     */
    fun getJobName(): String
    
    companion object {
        /**
         * Creates an appropriate DocumentPrinter implementation for the given document state
         */
        fun forDocument(docState: DocumentState): DocumentPrinter<*> {
            return when (docState) {
                is DocumentState.ScheduleState -> SchedulePrinter(docState)
                is DocumentState.Empty -> throw IllegalArgumentException("Cannot print empty document")
                // Add new document types here as they are created
                // is DocumentState.SomeOtherType -> SomeOtherTypePrinter(docState)
            }
        }
    }
}
