package ru.student.gradebook.data

import java.time.LocalDate

/** Демонстрационные данные (контрольный набор, по которому сверяются расчёты). */
object SeedData {
    data class SeedDiscipline(val name: String, val teacher: String?, val grades: List<SeedGrade>)
    data class SeedGrade(val value: Int, val date: LocalDate, val type: WorkType, val comment: String = "")

    val demo: List<SeedDiscipline> = listOf(
        SeedDiscipline(
            "Математика", "Иванова Е. А.", listOf(
                SeedGrade(5, LocalDate.of(2026, 9, 12), WorkType.CONTROL, "Пределы"),
                SeedGrade(4, LocalDate.of(2026, 9, 26), WorkType.PRACTICE),
                SeedGrade(3, LocalDate.of(2026, 10, 10), WorkType.LAB, "Ошибка в расчётах"),
                SeedGrade(5, LocalDate.of(2026, 11, 7), WorkType.ANSWER)
            )
        ),
        SeedDiscipline(
            "Программирование на Kotlin", "Петров С. В.", listOf(
                SeedGrade(5, LocalDate.of(2026, 9, 15), WorkType.LAB),
                SeedGrade(5, LocalDate.of(2026, 10, 3), WorkType.PRACTICE),
                SeedGrade(4, LocalDate.of(2026, 10, 24), WorkType.CONTROL),
                SeedGrade(5, LocalDate.of(2026, 11, 14), WorkType.LAB, "Room и DAO")
            )
        ),
        SeedDiscipline(
            "Базы данных", "Сидорова О. Н.", listOf(
                SeedGrade(3, LocalDate.of(2026, 9, 18), WorkType.CONTROL),
                SeedGrade(4, LocalDate.of(2026, 10, 16), WorkType.PRACTICE),
                SeedGrade(3, LocalDate.of(2026, 11, 6), WorkType.LAB)
            )
        ),
        SeedDiscipline(
            "Английский язык", "Орлова М. Д.", listOf(
                SeedGrade(4, LocalDate.of(2026, 9, 20), WorkType.ANSWER),
                SeedGrade(4, LocalDate.of(2026, 10, 18), WorkType.ANSWER),
                SeedGrade(4, LocalDate.of(2026, 11, 12), WorkType.CONTROL)
            )
        ),
        SeedDiscipline(
            "Физика", null, listOf(
                SeedGrade(3, LocalDate.of(2026, 9, 22), WorkType.CONTROL),
                SeedGrade(3, LocalDate.of(2026, 10, 20), WorkType.LAB)
            )
        )
    )
}
