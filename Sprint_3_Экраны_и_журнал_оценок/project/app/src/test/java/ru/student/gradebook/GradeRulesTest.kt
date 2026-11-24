package ru.student.gradebook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.student.gradebook.domain.GradeRules
import java.time.LocalDate

class GradeRulesTest {
    private val today = LocalDate.of(2026, 11, 23)

    @Test fun rangeBoundaries() {
        assertFalse(GradeRules.isValid(1))
        assertTrue(GradeRules.isValid(2))
        assertTrue(GradeRules.isValid(5))
        assertFalse(GradeRules.isValid(6))
    }

    @Test fun validInput() {
        val r = GradeRules.validateGrade("4", "20.11.2026", "ок", today)
        assertFalse(r.errors.hasErrors)
        assertEquals(4, r.value)
        assertEquals(LocalDate.of(2026, 11, 20), r.date)
    }

    @Test fun emptyAndNonNumericValue() {
        assertNotNull(GradeRules.validateGrade("", "20.11.2026", "", today).errors.value)
        assertNotNull(GradeRules.validateGrade("abc", "20.11.2026", "", today).errors.value)
        assertNotNull(GradeRules.validateGrade("4,5", "20.11.2026", "", today).errors.value)
    }

    @Test fun outOfRangeValue() {
        assertNotNull(GradeRules.validateGrade("1", "20.11.2026", "", today).errors.value)
        assertNotNull(GradeRules.validateGrade("6", "20.11.2026", "", today).errors.value)
        assertNull(GradeRules.validateGrade("2", "20.11.2026", "", today).errors.value)
    }

    @Test fun invalidDates() {
        assertNotNull(GradeRules.validateGrade("4", "", "", today).errors.date)
        assertNotNull(GradeRules.validateGrade("4", "2026-11-20", "", today).errors.date)
        assertNotNull(GradeRules.validateGrade("4", "31.02.2026", "", today).errors.date)
        assertNotNull(GradeRules.validateGrade("4", "24.11.2026", "", today).errors.date) // в будущем
    }

    @Test fun commentLength() {
        assertNull(GradeRules.validateGrade("4", "20.11.2026", "x".repeat(200), today).errors.comment)
        assertNotNull(GradeRules.validateGrade("4", "20.11.2026", "x".repeat(201), today).errors.comment)
    }

    @Test fun disciplineName() {
        assertNotNull(GradeRules.validateDisciplineName("   "))
        assertNull(GradeRules.validateDisciplineName("Математика"))
        assertNotNull(GradeRules.validateDisciplineName("я".repeat(61)))
    }

    @Test fun targetParsing() {
        assertEquals(4.5, GradeRules.parseTarget("4,5")!!, 1e-9)
        assertEquals(4.5, GradeRules.parseTarget(" 4.5 ")!!, 1e-9)
        assertNull(GradeRules.parseTarget("пять"))
    }
}
