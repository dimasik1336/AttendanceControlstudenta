package ru.student.gradebook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplineDao {
    @Insert
    suspend fun insert(discipline: Discipline): Long

    @Update
    suspend fun update(discipline: Discipline)

    @Delete
    suspend fun delete(discipline: Discipline)

    @Query("SELECT * FROM disciplines ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Discipline>>

    @Query("SELECT * FROM disciplines WHERE id = :id")
    suspend fun getById(id: Long): Discipline?

    @Query("SELECT COUNT(*) FROM disciplines")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM disciplines WHERE LOWER(name) = LOWER(:name)")
    suspend fun countByName(name: String): Int

    @Query("DELETE FROM disciplines")
    suspend fun deleteAll()
}
