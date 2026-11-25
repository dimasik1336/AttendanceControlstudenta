package ru.student.gradebook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.student.gradebook.domain.TargetCalculator
import ru.student.gradebook.domain.TargetResult

class TargetCalculatorTest {
    private fun required(r: TargetResult): TargetResult.Required = r as TargetResult.Required

    @Test fun requiredGradeIsExactInteger() {
        // Математика: сумма 17, 4 оценки; цель 4,4 → 4,4 × 5 − 17 = 5
        val r = required(TargetCalculator.calculate(TestData.math, 4.4))
        assertEquals(5, r.grade)
        assertEquals(4.4, r.resultingAverage, 1e-9)
    }

    @Test fun roundsUpToNextInteger() {
        // БД: сумма 10, 3 оценки; цель 3,5 → 3,5 × 4 − 10 = 4
        assertEquals(4, required(TargetCalculator.calculate(TestData.databases, 3.5)).grade)
        // Физика: сумма 6, 2 оценки; цель 3,2 → 9,6 − 6 = 3,6 → нужна 4
        val r = required(TargetCalculator.calculate(TestData.physics, 3.2))
        assertEquals(4, r.grade)
        assertEquals(3.33, r.resultingAverage, 0.005)
    }

    @Test fun unreachableGoalIsReported() {
        // Математика, цель 4,5: нужно 5,5 > 5
        val r = TargetCalculator.calculate(TestData.math, 4.5)
        assertTrue(r is TargetResult.Unreachable)
        r as TargetResult.Unreachable
        assertEquals("5.5", r.exact.stripTrailingZeros().toPlainString())
        assertEquals(4.4, r.maxPossibleAverage, 1e-9)
        // Физика, цель 4: нужно 6
        assertTrue(TargetCalculator.calculate(TestData.physics, 4.0) is TargetResult.Unreachable)
    }

    @Test fun alreadyAchievedGoal() {
        // Физика, цель 2,5: 7,5 − 6 = 1,5 → хватит и оценки 2
        assertTrue(TargetCalculator.calculate(TestData.physics, 2.5) is TargetResult.AnyGrade)
        assertTrue(TargetCalculator.calculate(TestData.physics, 2.0) is TargetResult.AnyGrade)
    }

    @Test fun boundaryTargets() {
        // цель 5 при всех пятёрках достижима пятёркой
        val fives = listOf(TestData.grade(9, 5, 2026, 11, 1, ru.student.gradebook.data.WorkType.OTHER))
        assertEquals(5, required(TargetCalculator.calculate(fives, 5.0)).grade)
        // цель 5 при наличии тройки недостижима
        assertTrue(TargetCalculator.calculate(TestData.physics, 5.0) is TargetResult.Unreachable)
    }

    @Test fun invalidTargets() {
        assertEquals(TargetResult.InvalidTarget, TargetCalculator.calculate(TestData.math, 5.5))
        assertEquals(TargetResult.InvalidTarget, TargetCalculator.calculate(TestData.math, 1.9))
        assertEquals(TargetResult.InvalidTarget, TargetCalculator.calculate(TestData.math, Double.NaN))
    }

    @Test fun noGradesYet() {
        // первая оценка должна равняться цели: 4 × 1 − 0 = 4
        assertEquals(4, required(TargetCalculator.calculate(emptyList(), 4.0)).grade)
    }
}
