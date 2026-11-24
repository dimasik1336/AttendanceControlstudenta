package ru.student.gradebook.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun dateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun epochDayToDate(day: Long): LocalDate = LocalDate.ofEpochDay(day)

    @TypeConverter
    fun workTypeToString(type: WorkType): String = type.name

    @TypeConverter
    fun stringToWorkType(name: String): WorkType =
        runCatching { WorkType.valueOf(name) }.getOrDefault(WorkType.OTHER)
}
