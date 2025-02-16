package org.w1001.schedule

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals

class MathEngineTest {
    @Test
    fun `test basic plus operation`() {
        val operation = PlusOperation()
        assertEquals(5, operation.execute(2, 3))
        assertEquals(0, operation.execute(0, 0))
        assertEquals(-1, operation.execute(-2, 1))
    }

    @Test
    fun `test basic minus operation`() {
        val operation = MinusOperation()
        assertEquals(1, operation.execute(2, 3)) // 3-2
        assertEquals(0, operation.execute(1, 1))
        assertEquals(3, operation.execute(-2, 1)) // 1-(-2)
    }

    @Test
    fun `test cell value evaluation with numbers`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("5")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(5, MathEngine.evaluateStep(step, cells))
    }

    @Test
    fun `test cell value evaluation with special characters`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("A")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(8, MathEngine.evaluateStep(step, cells))
    }

    @Test
    fun `test cell value evaluation with invalid input`() {
        val cells = listOf(
            listOf(CellData(mutableStateOf("invalid")))
        )
        val step = CalcStep.CellValue(CellRef(0, 0))
        assertEquals(0, MathEngine.evaluateStep(step, cells))
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

        assertEquals(0, MathEngine.evaluateStep(calculation, cells)) // (5+3)-8
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

        assertEquals(-1, MathEngine.evaluateStep(calculation, cells)) // (5+3)-8
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

        assertEquals(3, MathEngine.evaluateStep(calculation, cells))
    }
}
