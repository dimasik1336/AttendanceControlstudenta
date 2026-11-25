package ru.student.gradebook.domain

import ru.student.gradebook.data.Grade
import java.time.YearMonth

/**
 * Динамика успеваемости: средний балл по календарным месяцам.
 * [cumulativeAverage] — средний балл нарастающим итогом, [delta] — изменение к предыдущему периоду.
 */
data class PeriodStat(
    val period: YearMonth,
    val count: Int,
    val average: Double,
    val cumulativeAverage: Double,
    val delta: Double?
)

object PeriodAnalytics {
    private val monthNames = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    fun label(period: YearMonth): String = "${monthNames[period.monthValue - 1]} ${period.year}"

    fun byMonth(grades: List<Grade>): List<PeriodStat> {
        if (grades.isEmpty()) return emptyList()
        val groups = grades.groupBy { YearMonth.from(it.date) }.toSortedMap()
        val result = ArrayList<PeriodStat>()
        val running = ArrayList<Grade>()
        var previous: Double? = null
        for ((period, list) in groups) {
            running += list
            val avg = AverageCalculator.average(list)!!
            result += PeriodStat(
                period = period,
                count = list.size,
                average = avg,
                cumulativeAverage = AverageCalculator.average(running)!!,
                delta = previous?.let { avg - it }
            )
            previous = avg
        }
        return result
    }
}
