package ru.student.gradebook.domain

import java.math.BigDecimal
import java.math.RoundingMode
import ru.student.gradebook.data.Grade

/** Результат расчёта «какая оценка нужна за следующую работу». */
sealed interface TargetResult {
    /** Цель вне допустимого диапазона 2–5. */
    data object InvalidTarget : TargetResult

    /** Цель уже достигнута: даже минимальная оценка не опустит средний балл ниже цели. */
    data class AnyGrade(val averageWithMinGrade: Double) : TargetResult

    /** Нужна оценка [grade] (минимальная целая); [exact] — точное расчётное значение, [resultingAverage] — итоговый средний балл. */
    data class Required(val grade: Int, val exact: BigDecimal, val resultingAverage: Double) : TargetResult

    /** Цель недостижима одной оценкой; [maxPossibleAverage] — лучший возможный результат при оценке 5. */
    data class Unreachable(val exact: BigDecimal, val maxPossibleAverage: Double) : TargetResult
}

/**
 * Требуемая следующая оценка = цель × (количество оценок + 1) − сумма текущих оценок.
 * Обобщение для весов: g ≥ (цель × (W + w) − S) / w, где S — сумма оценка×вес, W — сумма весов, w — вес следующей работы.
 * В базовой версии вес каждой оценки равен 1.
 */
object TargetCalculator {
    private val minGrade = BigDecimal(GradeRules.MIN_GRADE)
    private val maxGrade = BigDecimal(GradeRules.MAX_GRADE)

    fun calculate(grades: List<Grade>, target: Double): TargetResult =
        solve(
            weightedSum = BigDecimal(grades.sumOf { it.value }),
            totalWeight = BigDecimal(grades.size),
            nextWeight = BigDecimal.ONE,
            target = target
        )

    internal fun solve(weightedSum: BigDecimal, totalWeight: BigDecimal, nextWeight: BigDecimal, target: Double): TargetResult {
        if (target.isNaN() || target.isInfinite()) return TargetResult.InvalidTarget
        val t = BigDecimal.valueOf(target)
        if (t < minGrade || t > maxGrade) return TargetResult.InvalidTarget

        val numerator = t.multiply(totalWeight.add(nextWeight)).subtract(weightedSum)
        val exact = numerator.divide(nextWeight, 2, RoundingMode.HALF_UP)
        val needed = numerator.divide(nextWeight, 0, RoundingMode.CEILING).toInt()
        val newTotal = totalWeight.add(nextWeight)

        fun averageWith(grade: BigDecimal): Double =
            weightedSum.add(grade.multiply(nextWeight)).divide(newTotal, 6, RoundingMode.HALF_UP).toDouble()

        return when {
            needed <= GradeRules.MIN_GRADE -> TargetResult.AnyGrade(averageWith(minGrade))
            needed > GradeRules.MAX_GRADE -> TargetResult.Unreachable(exact, averageWith(maxGrade))
            else -> TargetResult.Required(needed, exact, averageWith(BigDecimal(needed)))
        }
    }
}
