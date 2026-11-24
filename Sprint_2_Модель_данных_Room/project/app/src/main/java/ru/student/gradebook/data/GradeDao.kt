package ru.student.gradebook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Insert
    suspend fun insert(grade: Grade): Long

    @Insert
    suspend fun insertAll(grades: List<Grade>)

    @Update
    suspend fun update(grade: Grade)

    @Delete
    suspend fun delete(grade: Grade)

    @Query("SELECT * FROM grades ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE disciplineId = :disciplineId ORDER BY date DESC, id DESC")
    fun observeByDiscipline(disciplineId: Long): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE id = :id")
    suspend fun getById(id: Long): Grade?

    @Query("SELECT * FROM grades")
    suspend fun getAll(): List<Grade>

    @Query("SELECT COUNT(*) FROM grades")
    suspend fun count(): Int

    @Query("DELETE FROM grades WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
