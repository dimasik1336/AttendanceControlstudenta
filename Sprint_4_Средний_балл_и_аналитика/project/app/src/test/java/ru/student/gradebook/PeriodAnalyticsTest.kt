package ru.student.gradebook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.student.gradebook.domain.AverageCalculator
import ru.student.gradebook.domain.PeriodAnalytics
import java.time.YearMonth

class PeriodAnalyticsTest {
    @Test fun emptyInputGivesNoPeriods() {
        assertTrue(PeriodAnalytics.byMonth(emptyList()).isEmpty())
    }

    @Test fun monthlyDynamicsOfControlSet() {
        val s = PeriodAnalytics.byMonth(TestData.all)
        assertEquals(listOf(YearMonth.of(2026, 9), YearMonth.of(2026, 10), YearMonth.of(2026, 11)), s.map { it.period })
        assertEquals(listOf(6, 6, 4), s.map { it.count })
        assertEquals("4,00", AverageCalculator.format(s[0].average))
        assertEquals("3,83", AverageCalculator.format(s[1].average))
        assertEquals("4,25", AverageCalculator.format(s[2].average))
    }

    @Test fun deltaAndCumulative() {
        val s = PeriodAnalytics.byMonth(TestData.all)
        assertNull(s[0].delta)
        assertEquals("−0,17", AverageCalculator.formatDelta(s[1].delta))
        assertEquals("+0,42", AverageCalculator.formatDelta(s[2].delta))
        assertEquals("3,92", AverageCalculator.format(s[1].cumulativeAverage))
        assertEquals("4,00", AverageCalculator.format(s[2].cumulativeAverage))
    }

    @Test fun labels() {
        assertEquals("Сентябрь 2026", PeriodAnalytics.label(YearMonth.of(2026, 9)))
    }
}
