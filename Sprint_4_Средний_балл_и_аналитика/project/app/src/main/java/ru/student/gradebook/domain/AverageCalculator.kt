package ru.student.gradebook.domain

import ru.student.gradebook.data.Grade
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Средний балл = сумма оценок / количество оценок (все оценки равнозначны).
 * При отсутствии оценок возвращается null — деления на ноль нет.
 * Единый формат отображения во всём приложении: два знака после запятой, округление «половина вверх».
 */
object AverageCalculator {
    fun average(grades: List<Grade>): Double? {
        if (grades.isEmpty()) return null
        return grades.sumOf { it.value }.toDouble() / grades.size
    }

    fun format(value: Double?, empty: String = "нет оценок"): String {
        if (value == null) return empty
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')
    }

    fun formatDelta(value: Double?): String {
        if (value == null) return "—"
        val text = format(Math.abs(value))
        return when {
            value > 0.004 -> "+$text"
            value < -0.004 -> "−$text"
            else -> text
        }
    }
}
