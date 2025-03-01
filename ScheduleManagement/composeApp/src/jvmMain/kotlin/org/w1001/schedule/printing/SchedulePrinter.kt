package org.w1001.schedule.printing

import io.github.oshai.kotlinlogging.KotlinLogging
import org.w1001.schedule.DocumentState
import java.awt.*
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterException
import java.awt.print.PrinterJob
import java.math.BigDecimal
import javax.swing.JOptionPane
import kotlin.math.min

private val logger = KotlinLogging.logger("SchedulePrinter")

/**
 * Implementation of DocumentPrinter for ScheduleState documents
 */
class SchedulePrinter(override val documentState: DocumentState.ScheduleState) : DocumentPrinter<DocumentState.ScheduleState> {
    
    override fun getJobName(): String = "Schedule Spreadsheet - ${documentState.documentName.value}"
    
    override fun print(): Boolean {
        try {
            val job = PrinterJob.getPrinterJob()
            job.setJobName(getJobName())
            
            val printable = SchedulePrintable(documentState)
            job.setPrintable(printable)
            
            if (job.printDialog()) {
                job.print()
                logger.info { "Printing job submitted successfully" }
                return true
            } else {
                logger.info { "Print job was cancelled by user" }
                return true // User canceled, but no error
            }
        } catch (e: PrinterException) {
            logger.error(e) { "Error occurred while printing" }
            JOptionPane.showMessageDialog(
                null,
                "Error printing: ${e.message}",
                "Print Error",
                JOptionPane.ERROR_MESSAGE
            )
            return false
        }
    }

    private class SchedulePrintable(private val docState: DocumentState.ScheduleState) : Printable {
        private val workTime = docState.workTime.value
        private val cellWidth = 40
        private val cellHeight = 20
        private val groupSize = if (workTime == 1) 2 else 4
        private val columns = docState.numberOfColumns.value.toInt()
        private val dayColumnWidth = 30
        private val calcColumnWidth = 40
        private val pageHeaderHeight = 60
        private val baseMargin = 50
        private val columnSpacing = 10 // Define explicit spacing between columns
        private val normalFontSize = 12 // Increased from 10
        private val headerFontSize = 14 // Increased from 10
        private val headerBackgroundColor = Color(230, 230, 230) // Light grey color for backgrounds
        private val cornerRadius = 8 // Corner radius for rounded rectangles
        private val headerPadding = 1 // Padding around text in headers
        private val headerYPosition = 50 // Adjusted to be closer to data cells

        override fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
            val g2d = graphics as Graphics2D
            g2d.translate(pageFormat.imageableX, pageFormat.imageableY)

            val printableWidth = pageFormat.imageableWidth.toInt()
            val printableHeight = pageFormat.imageableHeight.toInt()
            
            val rowsPerPage = (printableHeight - pageHeaderHeight) / cellHeight
            val totalRows = docState.cells.size
            
            // Calculate column width including spacing
            val columnGroupWidth = (cellWidth * groupSize) + calcColumnWidth + 
                (if (workTime == 2) 5 else 0) + columnSpacing
            
            // Calculate how many columns fit on the page using the full printable width
            // minus margins on both sides
            val availableWidth = printableWidth - (baseMargin * 2)
            val columnsPerPage = (availableWidth / columnGroupWidth).coerceAtLeast(1).toInt()
            
            // Calculate total number of pages needed for rows and columns
            val totalColumns = docState.numberOfColumns.value.toInt()
            val totalColumnPages = (totalColumns + columnsPerPage - 1) / columnsPerPage
            val totalRowPages = (totalRows + rowsPerPage - 1) / rowsPerPage
            val totalPages = totalColumnPages * totalRowPages
            
            // Determine current page's position in the grid
            val columnPageIndex = pageIndex / totalRowPages
            val rowPageIndex = pageIndex % totalRowPages
            
            // Check if this page exists
            if (columnPageIndex >= totalColumnPages || pageIndex >= totalPages) {
                return Printable.NO_SUCH_PAGE
            }
            
            // Calculate which rows and columns to display on this page
            val startRow = rowPageIndex * rowsPerPage
            val endRow = min((rowPageIndex + 1) * rowsPerPage, totalRows)
            
            val startColumn = columnPageIndex * columnsPerPage
            val endColumn = min((columnPageIndex + 1) * columnsPerPage, totalColumns)
            val columnsOnThisPage = endColumn - startColumn
            
            // Calculate total width needed for data columns only
            val dataColumnsWidth = columnsOnThisPage * columnGroupWidth - columnSpacing
            
            // Center data columns in the available printable width
            val dataColumnsStartX = (printableWidth - dataColumnsWidth) / 2
            
            // Position day column to the left of the centered data columns
            val dayColumnX = dataColumnsStartX - dayColumnWidth - columnSpacing
            
            // Print the document title/header, centered across entire page width
            g2d.font = Font("Arial", Font.BOLD, 16)
            val titleText = docState.documentName.value
            val titleFontMetrics = g2d.fontMetrics
            val titleWidth = titleFontMetrics.stringWidth(titleText)
            val titleX = (printableWidth - titleWidth) / 2
            g2d.drawString(titleText, titleX, 30)
            
            // Calculate vertical measurements for headers
            g2d.font = Font("Arial", Font.BOLD, headerFontSize)
            val headerFontMetrics = g2d.fontMetrics
            val bgHeight = headerFontMetrics.height + (headerPadding * 2)
            val bgY = headerYPosition - headerFontMetrics.ascent - headerPadding
            
            // Print "Day" header
            printDayHeader(g2d, headerFontMetrics, bgY, bgHeight, dayColumnX)
            
            // Start position for data columns
            var xPosition = dataColumnsStartX
            
            // Print column headers for this page's columns
            for (colIdx in startColumn until endColumn) {
                printColumnHeader(g2d, colIdx, xPosition, headerFontMetrics, bgY, bgHeight)
                // Move to next column group
                xPosition += columnGroupWidth
            }
            
            // Print the rows for this page
            g2d.font = Font("Arial", Font.PLAIN, normalFontSize)
            val fontMetrics = g2d.fontMetrics
            
            for (rowIdx in startRow until endRow) {
                val yPosition = pageHeaderHeight + (rowIdx - startRow) * cellHeight
                
                // Print day cell
                printDayCell(g2d, rowIdx, yPosition, fontMetrics, dayColumnX)
                
                // Reset xPosition for data cells
                xPosition = dataColumnsStartX
                
                // Print this page's columns for the current row
                for (colGroup in startColumn until endColumn) {
                    xPosition = printRowCells(g2d, rowIdx, colGroup, xPosition, yPosition, fontMetrics)
                }
            }
            
            // Draw page information
            g2d.font = Font("Arial", Font.ITALIC, 8)
            val pageInfo = "Page ${pageIndex + 1} of $totalPages (Row page ${rowPageIndex + 1}/${totalRowPages}, Column page ${columnPageIndex + 1}/${totalColumnPages})"
            g2d.drawString(pageInfo, printableWidth - 200, printableHeight - 10)
            
            return Printable.PAGE_EXISTS
        }
        
        // Update the functions to accept the dynamic margin
        private fun printDayHeader(g2d: Graphics2D, fontMetrics: FontMetrics, bgY: Int, bgHeight: Int, margin: Int) {
            val dayHeader = "Day"
            val dayHeaderWidth = fontMetrics.stringWidth(dayHeader)
            
            // Draw day header background with dynamic margin
            g2d.color = headerBackgroundColor
            g2d.fillRoundRect(
                margin,
                bgY,
                dayColumnWidth,
                bgHeight,
                cornerRadius,
                cornerRadius
            )
            
            // Center the text within the background
            val dayHeaderX = margin + (dayColumnWidth - dayHeaderWidth) / 2
            g2d.color = Color.BLACK
            g2d.drawString(dayHeader, dayHeaderX, headerYPosition)
        }
        
        private fun printDayCell(g2d: Graphics2D, rowIdx: Int, yPosition: Int, fontMetrics: FontMetrics, margin: Int) {
            // Draw day cell with dynamic margin
            g2d.drawRect(margin, yPosition, dayColumnWidth, cellHeight)
            
            // Center text in day cell
            val dayCellText = docState.dayCellsData[rowIdx].content.value
            val dayTextWidth = fontMetrics.stringWidth(dayCellText)
            val dayTextX = margin + (dayColumnWidth - dayTextWidth) / 2
            val textY = yPosition + (cellHeight - fontMetrics.height) / 2 + fontMetrics.ascent
            
            g2d.drawString(dayCellText, dayTextX, textY)
        }
        
        private fun printColumnHeader(
            g2d: Graphics2D, 
            colIdx: Int, 
            xPos: Int, 
            fontMetrics: FontMetrics,
            bgY: Int,
            bgHeight: Int
        ) {
            // Get column name with colon
            val columnName = if (colIdx < docState.columnNames.size) {
                docState.columnNames[colIdx].value + ":"
            } else {
                "Column ${colIdx + 1}:"
            }
            
            // Calculate the sum for this column
            val sum = docState.calcCellBindings[colIdx]?.sumOf { it.value } ?: BigDecimal.ZERO
            val sumText = sum.toString()
            
            // Calculate positions and widths
            val columnWidth = cellWidth * groupSize
            
            // Calculate total width for the column group
            val totalGroupWidth = columnWidth + calcColumnWidth + 
                (if (workTime == 2) 5 else 0) // Include the 5dp space for workTime==2
            
            // Draw a unified background for the entire column group
            g2d.color = headerBackgroundColor
            g2d.fillRoundRect(
                xPos,
                bgY,
                totalGroupWidth,
                bgHeight,
                cornerRadius,
                cornerRadius
            )
            g2d.color = Color.BLACK
            
            // Position the column name in the center of the data cells
            val textWidth = fontMetrics.stringWidth(columnName)
            val dataCellsCenterX = xPos + (columnWidth / 2)
            val textX = dataCellsCenterX - (textWidth / 2)
            g2d.drawString(columnName, textX, headerYPosition)
            
            // Position the sum text in the center of the calc cell
            val sumWidth = fontMetrics.stringWidth(sumText)
            val calcCellX = xPos + columnWidth + (if (workTime == 2) 5 else 0)
            val calcCellCenterX = calcCellX + (calcColumnWidth / 2)
            val sumX = calcCellCenterX - (sumWidth / 2)
            g2d.drawString(sumText, sumX, headerYPosition)
        }
        
        private fun printRowCells(
            g2d: Graphics2D, 
            rowIdx: Int, 
            colGroup: Int, 
            startX: Int, 
            yPosition: Int,
            fontMetrics: FontMetrics
        ): Int {
            var xPosition = startX
            val textY = yPosition + (cellHeight - fontMetrics.height) / 2 + fontMetrics.ascent
            
            // Print data cells for this column group
            for (idx in 0 until groupSize) {
                val cellIdx = colGroup * groupSize + idx
                val cellContent = if (cellIdx < docState.cells[rowIdx].size) {
                    docState.cells[rowIdx][cellIdx].content.value
                } else ""
                
                // Special merge handling
                if (cellContent in setOf("A", "B", "C")) {
                    val mergedCellWidth = cellWidth * groupSize
                    g2d.drawRect(xPosition, yPosition, mergedCellWidth, cellHeight)
                    
                    // Center text in merged cell
                    val textWidth = fontMetrics.stringWidth(cellContent)
                    val textX = xPosition + (mergedCellWidth - textWidth) / 2
                    
                    g2d.drawString(cellContent, textX, textY)
                    xPosition += mergedCellWidth
                    break
                } else {
                    g2d.drawRect(xPosition, yPosition, cellWidth, cellHeight)
                    
                    // Center text in regular cell
                    val textWidth = fontMetrics.stringWidth(cellContent)
                    val textX = xPosition + (cellWidth - textWidth) / 2
                    
                    g2d.drawString(cellContent, textX, textY)
                    xPosition += cellWidth
                }
                
                // Add gap between pairs in workTime == 2
                if (workTime == 2 && idx == groupSize / 2 - 1) {
                    xPosition += 5
                }
            }
            
            // Draw calc cell
            g2d.drawRect(xPosition, yPosition, calcColumnWidth, cellHeight)
            val calcValue = docState.calcCellBindings[colGroup]?.get(rowIdx)?.value ?: BigDecimal.ZERO
            val calcText = calcValue.toString()
            
            // Center text in calc cell
            val textWidth = fontMetrics.stringWidth(calcText)
            val textX = xPosition + (calcColumnWidth - textWidth) / 2
            
            g2d.drawString(calcText, textX, textY)
            
            xPosition += calcColumnWidth + columnSpacing
            
            return xPosition
        }
    }
}
