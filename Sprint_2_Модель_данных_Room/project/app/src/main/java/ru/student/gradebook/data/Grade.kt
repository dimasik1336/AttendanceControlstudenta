package ru.student.gradebook.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Оценка. Связь Discipline 1 — N Grade.
 * При удалении дисциплины её оценки удаляются каскадно (UI предварительно просит подтверждение).
 */
@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Discipline::class,
            parentColumns = ["id"],
            childColumns = ["disciplineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("disciplineId")]
)
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val disciplineId: Long,
    val value: Int,
    val date: LocalDate,
    val workType: WorkType,
    val comment: String = ""
)
