package ru.student.gradebook.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/** Правила оценивания и проверки ввода (пятибалльная система, оценки 2–5, одинаковый вес в базовой версии). */
object GradeRules {
    const val MIN_GRADE = 2
    const val MAX_GRADE = 5
    const val MAX_COMMENT_LENGTH = 200
    const val MAX_NAME_LENGTH = 60

    /** Строгий разбор: 31.02.2026 не считается корректной датой. */
    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT)

    fun isValid(value: Int): Boolean = value in MIN_GRADE..MAX_GRADE

    fun parseDate(text: String): LocalDate? =
        runCatching { LocalDate.parse(text.trim(), dateFormatter) }.getOrNull()

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    /** Ошибки по полям формы оценки; null — поле корректно. */
    data class GradeErrors(
        val value: String? = null,
        val date: String? = null,
        val comment: String? = null
    ) {
        val hasErrors: Boolean get() = value != null || date != null || comment != null
    }

    data class GradeValidation(val value: Int?, val date: LocalDate?, val errors: GradeErrors)

    fun validateGrade(valueText: String, dateText: String, comment: String, today: LocalDate = LocalDate.now()): GradeValidation {
        val trimmed = valueText.trim()
        val value = trimmed.toIntOrNull()
        val valueError = when {
            trimmed.isEmpty() -> "Введите оценку"
            value == null -> "Оценка должна быть целым числом"
            !isValid(value) -> "Допустимы оценки от $MIN_GRADE до $MAX_GRADE"
            else -> null
        }
        val date = parseDate(dateText)
        val dateError = when {
            dateText.isBlank() -> "Введите дату"
            date == null -> "Формат даты: дд.мм.гггг"
            date.isAfter(today) -> "Дата не может быть в будущем"
            else -> null
        }
        val commentError = if (comment.length > MAX_COMMENT_LENGTH) "Не более $MAX_COMMENT_LENGTH символов" else null
        return GradeValidation(
            value = if (valueError == null) value else null,
            date = if (dateError == null) date else null,
            errors = GradeErrors(valueError, dateError, commentError)
        )
    }

    fun validateDisciplineName(name: String): String? = when {
        name.isBlank() -> "Введите название дисциплины"
        name.trim().length > MAX_NAME_LENGTH -> "Не более $MAX_NAME_LENGTH символов"
        else -> null
    }

    /** Целевой средний балл из текста: допускает запятую и точку. */
    fun parseTarget(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()
}
