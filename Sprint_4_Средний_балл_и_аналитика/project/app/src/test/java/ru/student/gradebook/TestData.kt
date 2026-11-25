package ru.student.gradebook

import ru.student.gradebook.data.Grade
import ru.student.gradebook.data.WorkType
import java.time.LocalDate

/** Контрольный набор оценок (совпадает с демо-данными приложения), id дисциплин: 1..5. */
object TestData {
    private var nextId = 1L

    fun grade(disciplineId: Long, value: Int, y: Int, m: Int, d: Int, type: WorkType, comment: String = "") =
        Grade(id = nextId++, disciplineId = disciplineId, value = value, date = LocalDate.of(y, m, d), workType = type, comment = comment)

    val math = listOf(
        grade(1, 5, 2026, 9, 12, WorkType.CONTROL),
        grade(1, 4, 2026, 9, 26, WorkType.PRACTICE),
        grade(1, 3, 2026, 10, 10, WorkType.LAB),
        grade(1, 5, 2026, 11, 7, WorkType.ANSWER)
    )
    val kotlin = listOf(
        grade(2, 5, 2026, 9, 15, WorkType.LAB),
        grade(2, 5, 2026, 10, 3, WorkType.PRACTICE),
        grade(2, 4, 2026, 10, 24, WorkType.CONTROL),
        grade(2, 5, 2026, 11, 14, WorkType.LAB)
    )
    val databases = listOf(
        grade(3, 3, 2026, 9, 18, WorkType.CONTROL),
        grade(3, 4, 2026, 10, 16, WorkType.PRACTICE),
        grade(3, 3, 2026, 11, 6, WorkType.LAB)
    )
    val english = listOf(
        grade(4, 4, 2026, 9, 20, WorkType.ANSWER),
        grade(4, 4, 2026, 10, 18, WorkType.ANSWER),
        grade(4, 4, 2026, 11, 12, WorkType.CONTROL)
    )
    val physics = listOf(
        grade(5, 3, 2026, 9, 22, WorkType.CONTROL),
        grade(5, 3, 2026, 10, 20, WorkType.LAB)
    )
    val all = math + kotlin + databases + english + physics
}
