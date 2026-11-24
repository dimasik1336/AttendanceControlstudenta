package ru.student.gradebook.data

/** Вид учебной работы, за которую выставлена оценка. */
enum class WorkType(val label: String) {
    CONTROL("Контрольная"),
    PRACTICE("Практическая"),
    LAB("Лабораторная"),
    ANSWER("Ответ на занятии"),
    OTHER("Другое")
}
