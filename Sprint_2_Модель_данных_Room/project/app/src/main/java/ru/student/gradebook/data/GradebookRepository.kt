package ru.student.gradebook.data

import kotlinx.coroutines.flow.Flow

class GradebookRepository(private val db: AppDatabase) {
    private val disciplineDao = db.disciplineDao()
    private val gradeDao = db.gradeDao()

    val disciplines: Flow<List<Discipline>> = disciplineDao.observeAll()
    val grades: Flow<List<Grade>> = gradeDao.observeAll()

    suspend fun getDiscipline(id: Long): Discipline? = disciplineDao.getById(id)
    suspend fun getGrade(id: Long): Grade? = gradeDao.getById(id)

    /** Возвращает false, если дисциплина с таким названием уже есть. */
    suspend fun addDiscipline(name: String, teacher: String?): Boolean {
        if (disciplineDao.countByName(name.trim()) > 0) return false
        disciplineDao.insert(Discipline(name = name.trim(), teacherName = teacher?.trim()?.ifBlank { null }))
        return true
    }

    suspend fun updateDiscipline(discipline: Discipline) = disciplineDao.update(discipline)

    /** Оценки дисциплины удаляются каскадно (внешний ключ ON DELETE CASCADE). */
    suspend fun deleteDiscipline(discipline: Discipline) = disciplineDao.delete(discipline)

    suspend fun saveGrade(grade: Grade) {
        if (grade.id == 0L) gradeDao.insert(grade) else gradeDao.update(grade)
    }

    suspend fun deleteGrade(grade: Grade) = gradeDao.delete(grade)

    suspend fun isEmpty(): Boolean = disciplineDao.count() == 0

    suspend fun loadDemo() {
        for (d in SeedData.demo) {
            if (disciplineDao.countByName(d.name) > 0) continue
            val id = disciplineDao.insert(Discipline(name = d.name, teacherName = d.teacher))
            gradeDao.insertAll(d.grades.map { Grade(disciplineId = id, value = it.value, date = it.date, workType = it.type, comment = it.comment) })
        }
    }

    /** Полная очистка данных (удаление тестовых записей перед сдачей). */
    suspend fun clearAll() = disciplineDao.deleteAll()

    /**
     * Удаляет дублирующиеся оценки (одинаковые дисциплина, значение, дата, вид работы и комментарий).
     * Возвращает число удалённых записей.
     */
    suspend fun removeDuplicateGrades(): Int {
        val seen = HashSet<List<Any>>()
        val duplicates = ArrayList<Long>()
        for (g in gradeDao.getAll().sortedBy { it.id }) {
            val key = listOf(g.disciplineId, g.value, g.date, g.workType, g.comment.trim())
            if (!seen.add(key)) duplicates += g.id
        }
        if (duplicates.isNotEmpty()) gradeDao.deleteByIds(duplicates)
        return duplicates.size
    }
}
