package org.w1001.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import org.w1001.schedule.cells.specialCharMap
import java.math.BigDecimal

data class CellRef(val row: Int, val col: Int)

private val logger = KotlinLogging.logger("MathEngine.kt")

// Represents a calculation step
sealed class CalcStep {
    data class CellValue(val cellRef: CellRef) : CalcStep()
    data class Calculation(val left: CalcStep, val right: CalcStep, val operation: Operation) : CalcStep()
}

abstract class Operation {
    abstract fun execute(a: BigDecimal, b: BigDecimal): BigDecimal
}

class PlusOperation : Operation() {
    override fun execute(a: BigDecimal, b: BigDecimal): BigDecimal {
        return a.plus(b).stripTrailingZeros()
    }
}

class MinusOperation : Operation() {
    override fun execute(a: BigDecimal, b: BigDecimal): BigDecimal {
        return a.minus(b).stripTrailingZeros()
//        return a - b
    }
}



class MathEngine {
    companion object {
        @Throws(Exception::class)
        fun evaluateStep(step: CalcStep, cells: List<List<CellData>>): BigDecimal {
            return try { //Maybe remore the try catch to allow passing of the exception to the caller
                when (step) {
                    is CalcStep.CellValue -> {
                        val content = cells[step.cellRef.row][step.cellRef.col].content.value.trim()
                        content.toBigDecimalOrNull() ?: specialCharMap[content] ?: BigDecimal.ZERO
                    }
                    is CalcStep.Calculation -> {
                        val leftValue = evaluateStep(step.left, cells)
                        val rightValue = evaluateStep(step.right, cells)
                        step.operation.execute(leftValue, rightValue)
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { e.stackTraceToString() }
                BigDecimal.ZERO
            }
        }
    }
}