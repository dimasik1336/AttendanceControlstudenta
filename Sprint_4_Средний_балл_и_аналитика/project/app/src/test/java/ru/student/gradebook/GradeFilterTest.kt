package ru.student.gradebook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.student.gradebook.data.WorkType
import ru.student.gradebook.domain.GradeFilter
import ru.student.gradebook.domain.GradeQuery
import ru.student.gradebook.domain.SortOrder
import java.time.LocalDate

class GradeFilterTest {
    @Test fun byDiscipline() {
        val r = GradeQuery.apply(TestData.all, GradeFilter(disciplineId = 3), SortOrder.DATE_ASC)
        assertEquals(3, r.size)
        assertTrue(r.all { it.disciplineId == 3L })
    }

    @Test fun byWorkType() {
        val r = GradeQuery.apply(TestData.all, GradeFilter(workType = WorkType.LAB), SortOrder.DATE_ASC)
        assertEquals(5, r.size) // Математика, Kotlin ×2, БД, Физика
    }

    @Test fun byPeriodBoundsAreInclusive() {
        val f = GradeFilter(from = LocalDate.of(2026, 10, 1), to = LocalDate.of(2026, 10, 31))
        assertEquals(6, GradeQuery.apply(TestData.all, f, SortOrder.DATE_ASC).size)
    }

    @Test fun combinedFilters() {
        val f = GradeFilter(disciplineId = 2, workType = WorkType.LAB, from = LocalDate.of(2026, 11, 1))
        val r = GradeQuery.apply(TestData.all, f, SortOrder.DATE_ASC)
        assertEquals(1, r.size)
        assertEquals(LocalDate.of(2026, 11, 14), r.first().date)
    }

    @Test fun noMatchesGivesEmptyList() {
        val f = GradeFilter(disciplineId = 5, workType = WorkType.ANSWER)
        assertTrue(GradeQuery.apply(TestData.all, f, SortOrder.DATE_ASC).isEmpty())
    }

    @Test fun sortByDate() {
        val asc = GradeQuery.apply(TestData.math, GradeFilter(), SortOrder.DATE_ASC)
        assertEquals(listOf(5, 4, 3, 5), asc.map { it.value })
        val desc = GradeQuery.apply(TestData.math, GradeFilter(), SortOrder.DATE_DESC)
        assertEquals(listOf(5, 3, 4, 5), desc.map { it.value })
    }

    @Test fun sortByValue() {
        val desc = GradeQuery.apply(TestData.math, GradeFilter(), SortOrder.VALUE_DESC)
        assertEquals(listOf(5, 5, 4, 3), desc.map { it.value })
        val asc = GradeQuery.apply(TestData.math, GradeFilter(), SortOrder.VALUE_ASC)
        assertEquals(listOf(3, 4, 5, 5), asc.map { it.value })
    }
}
