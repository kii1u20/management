package org.w1001.schedule

import org.w1001.schedule.cells.specialCharMap

data class CellRef(val row: Int, val col: Int)

// Represents a calculation step
sealed class CalcStep {
    data class CellValue(val cellRef: CellRef) : CalcStep()
    data class Calculation(val left: CalcStep, val right: CalcStep, val operation: Operation) : CalcStep()
}

abstract class Operation {
    abstract fun execute(a: Int, b: Int): Int
}

class PlusOperation : Operation() {
    override fun execute(a: Int, b: Int): Int {
        return a + b
    }
}

class MinusOperation : Operation() {
    override fun execute(a: Int, b: Int): Int {
        return b - a
    }
}

class MathEngine {
    companion object {
        fun evaluateStep(step: CalcStep, cells: List<List<CellData>>): Int {
            return when (step) {
                is CalcStep.CellValue -> {
                    val content = cells[step.cellRef.row][step.cellRef.col].content.value
                    content.toIntOrNull() ?: specialCharMap[content] ?: 0
                }
                is CalcStep.Calculation -> {
                    val leftValue = evaluateStep(step.left, cells)
                    val rightValue = evaluateStep(step.right, cells)
                    step.operation.execute(leftValue, rightValue)
                }
            }
        }
    }
}