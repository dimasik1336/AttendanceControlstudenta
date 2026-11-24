package ru.student.gradebook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Учебная дисциплина.
 */
@Entity(tableName = "disciplines")
data class Discipline(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val teacherName: String? = null
)
