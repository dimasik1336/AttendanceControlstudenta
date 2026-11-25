package ru.student.gradebook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.student.gradebook.data.WorkType
import ru.student.gradebook.domain.AverageCalculator

class AverageCalculatorTest {
    @Test fun planExample() {
        // из плана: оценки 5, 4, 3, 5 → (5 + 4 + 3 + 5) / 4 = 4,25
        assertEquals(4.25, AverageCalculator.average(TestData.math)!!, 1e-9)
        assertEquals("4,25", AverageCalculator.format(AverageCalculator.average(TestData.math)))
    }

    @Test fun emptyListHasNoAverageAndNoDivisionByZero() {
        assertNull(AverageCalculator.average(emptyList()))
        assertEquals("нет оценок", AverageCalculator.format(null))
    }

    @Test fun controlSet() {
        assertEquals("4,75", AverageCalculator.format(AverageCalculator.average(TestData.kotlin)))
        assertEquals("3,33", AverageCalculator.format(AverageCalculator.average(TestData.databases)))
        assertEquals("4,00", AverageCalculator.format(AverageCalculator.average(TestData.english)))
        assertEquals("3,00", AverageCalculator.format(AverageCalculator.average(TestData.physics)))
        assertEquals("4,00", AverageCalculator.format(AverageCalculator.average(TestData.all)))
        assertEquals(16, TestData.all.size)
    }

    @Test fun singleGrade() {
        val one = listOf(TestData.grade(9, 5, 2026, 11, 1, WorkType.OTHER))
        assertEquals(5.0, AverageCalculator.average(one)!!, 1e-9)
    }

    @Test fun roundingIsHalfUp() {
        assertEquals("0,13", AverageCalculator.format(0.125))
        assertEquals("3,67", AverageCalculator.format(11.0 / 3))
    }

    @Test fun deltaFormatting() {
        assertEquals("+0,42", AverageCalculator.formatDelta(0.4167))
        assertEquals("−0,17", AverageCalculator.formatDelta(-0.1667))
        assertEquals("0,00", AverageCalculator.formatDelta(0.0))
        assertEquals("—", AverageCalculator.formatDelta(null))
    }
}
