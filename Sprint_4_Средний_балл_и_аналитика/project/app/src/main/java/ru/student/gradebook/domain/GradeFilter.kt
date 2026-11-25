package ru.student.gradebook.domain

import ru.student.gradebook.data.Grade
import ru.student.gradebook.data.WorkType
import java.time.LocalDate

/** Фильтр оценок: дисциплина, вид работы и период (границы включительно). */
data class GradeFilter(
    val disciplineId: Long? = null,
    val workType: WorkType? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null
) {
    val isActive: Boolean get() = workType != null || from != null || to != null

    fun matches(grade: Grade): Boolean =
        (disciplineId == null || grade.disciplineId == disciplineId) &&
            (workType == null || grade.workType == workType) &&
            (from == null || !grade.date.isBefore(from)) &&
            (to == null || !grade.date.isAfter(to))
}

enum class SortOrder(val label: String) {
    DATE_DESC("Сначала новые"),
    DATE_ASC("Сначала старые"),
    VALUE_DESC("Оценка: по убыванию"),
    VALUE_ASC("Оценка: по возрастанию")
}

object GradeQuery {
    fun apply(grades: List<Grade>, filter: GradeFilter, order: SortOrder): List<Grade> {
        val filtered = grades.filter(filter::matches)
        return when (order) {
            SortOrder.DATE_DESC -> filtered.sortedWith(compareByDescending<Grade> { it.date }.thenByDescending { it.id })
            SortOrder.DATE_ASC -> filtered.sortedWith(compareBy<Grade> { it.date }.thenBy { it.id })
            SortOrder.VALUE_DESC -> filtered.sortedWith(compareByDescending<Grade> { it.value }.thenByDescending { it.date })
            SortOrder.VALUE_ASC -> filtered.sortedWith(compareBy<Grade> { it.value }.thenByDescending { it.date })
        }
    }
}
