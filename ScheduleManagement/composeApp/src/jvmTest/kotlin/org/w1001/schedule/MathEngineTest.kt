package org.w1001.schedule

import androidx.compose.runtime.mutableStateOf
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class MathEngineTest {

    @Test
    fun `hhh`() {
        val operation = MinusOperation()
        assertEquals(BigDecimal("1"), operation.execute(1.2.toBigDecimal(), 0.2.toBigDecimal()))
    }

    @Test
    fun `test basic plus operation`() {
        val operation = PlusOperation()
        assertEquals(BigDecimal("5"), operation.execute(BigDecimal("2"), BigDecimal("3")))
        assertEquals(BigDecimal("0"), operation.execute(BigDecimal("0"), BigDecimal("0")))
        assertEquals(BigDecimal("-1"), operation.execute(BigDecimal("-2"), BigDecimal("1")))
    }

    @Test
    fun `test basic minus operation`() {
        val operation = MinusOperation()
        assertEquals(BigDecimal("1"), operation.execute(BigDecimal("3"), BigDecimal("2")))
        assertEquals(BigDecimal("0"), operation.execute(BigDecimal("1"), BigDecimal("1")))
        assertEquals(BigDecimal("3"), operation.execute(BigDecimal("1"), BigDecimal("-2")))
    }

    @Test
    fun `test cell value evaluation with numbers`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("5")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(BigDecimal("5"), MathEngine.evaluateStep(step, cells))
    }

    @Test
    fun `test cell value evaluation with special characters`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("A")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(BigDecimal("8"), MathEngine.evaluateStep(step, cells))
    }

    @Test
    fun `test cell value evaluation with invalid input`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("invalid")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(BigDecimal("0"), MathEngine.evaluateStep(step, cells))
    }

    @Test
    fun `test complex calculation`() {
        val cells = listOf(
            listOf(
                CellData(mutableStateOf("5")),
                CellData(mutableStateOf("3")),
                CellData(mutableStateOf("A"))
            )
        )

        val calculation = CalcStep.Calculation(
            left = CalcStep.Calculation(
                left = CalcStep.CellValue(CellRef(0, 0)),
                right = CalcStep.CellValue(CellRef(0, 1)),
                operation = PlusOperation()
            ),
            right = CalcStep.CellValue(CellRef(0, 2)),
            operation = MinusOperation()
        )

        assertEquals(BigDecimal("0"), MathEngine.evaluateStep(calculation, cells)) // (5+3)-8
    }

    @Test
    fun `test complex calculation right minus left`() {
        val cells = listOf(
            listOf(
                CellData(mutableStateOf("5")),
                CellData(mutableStateOf("4")),
                CellData(mutableStateOf("A"))
            )
        )

        val calculation = CalcStep.Calculation(
            left = CalcStep.Calculation(
                left = CalcStep.CellValue(CellRef(0, 0)),
                right = CalcStep.CellValue(CellRef(0, 1)),
                operation = PlusOperation()
            ),
            right = CalcStep.CellValue(CellRef(0, 2)),
            operation = MinusOperation()
        )

        assertEquals(BigDecimal("1"), MathEngine.evaluateStep(calculation, cells)) // (5+4)-8
    }

    @Test
    fun `test calculation with empty cells`() {
        val cells = listOf(
            listOf(
                CellData(mutableStateOf("")),
                CellData(mutableStateOf("3"))
            )
        )

        val calculation = CalcStep.Calculation(
            left = CalcStep.CellValue(CellRef(0, 0)),
            right = CalcStep.CellValue(CellRef(0, 1)),
            operation = PlusOperation()
        )

        assertEquals(BigDecimal("3"), MathEngine.evaluateStep(calculation, cells))
    }

    @Test
    fun `test cell value out of bounds`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("5")))
        )
        val step = CalcStep.CellValue(CellRef(1, 0))  // row out of bounds
        assertEquals(BigDecimal("0"), MathEngine.evaluateStep(step, cells))

        val step2 = CalcStep.CellValue(CellRef(0, 1))  // column out of bounds
        assertEquals(BigDecimal("0"), MathEngine.evaluateStep(step2, cells))
    }

    @Test
    fun `test cell value with whitespace`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("  5  ")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(BigDecimal("5"), MathEngine.evaluateStep(step, cells))
    }

    @Test
    fun `test cell value with decimal numbers`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("5.5")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(BigDecimal("5.5"), MathEngine.evaluateStep(step, cells))  // should truncate to integer
    }

    @Test
    fun `test plus operation with large numbers`() {
        val operation = PlusOperation()
        val largeNumber = BigDecimal("999999999999999999999999999999")
        assertEquals(largeNumber, operation.execute(largeNumber, BigDecimal("0")))
        assertEquals(largeNumber.add(BigDecimal("1")).toPlainString(), operation.execute(largeNumber, BigDecimal("1")).toPlainString())
    }

    @Test
    fun `test minus operation with large numbers`() {
        val operation = MinusOperation()
        val largeNumber = BigDecimal("999999999999999999999999999999")
        assertEquals(BigDecimal("0"), operation.execute(largeNumber, largeNumber))
        assertEquals(largeNumber, operation.execute(largeNumber, BigDecimal("0")))
    }

    @Test
    fun `test complex calculation with nested operations`() {
        val cells = listOf(
            listOf(
                CellData(mutableStateOf("5")),
                CellData(mutableStateOf("3")),
                CellData(mutableStateOf("2")),
                CellData(mutableStateOf("1"))
            )
        )

        // Testing ((5+3)-(2+1)) = 5
        val calculation = CalcStep.Calculation(
            left = CalcStep.Calculation(
                left = CalcStep.CellValue(CellRef(0, 0)),
                right = CalcStep.CellValue(CellRef(0, 1)),
                operation = PlusOperation()
            ),
            right = CalcStep.Calculation(
                left = CalcStep.CellValue(CellRef(0, 2)),
                right = CalcStep.CellValue(CellRef(0, 3)),
                operation = PlusOperation()
            ),
            operation = MinusOperation()
        )
        assertEquals(BigDecimal("5"), MathEngine.evaluateStep(calculation, cells))
    }
}
