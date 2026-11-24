package ru.student.gradebook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.student.gradebook.data.AppDatabase
import ru.student.gradebook.data.Discipline
import ru.student.gradebook.data.Grade
import ru.student.gradebook.data.GradebookRepository
import ru.student.gradebook.data.WorkType
import java.time.LocalDate

/** Проверка Room: добавление, чтение, изменение, удаление, каскад и устранение дубликатов. */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: GradebookRepository

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = GradebookRepository(db)
    }

    @After fun tearDown() = db.close()

    @Test fun insertReadUpdateDeleteGrade() = runBlocking {
        val dId = db.disciplineDao().insert(Discipline(name = "Математика"))
        val gId = db.gradeDao().insert(Grade(disciplineId = dId, value = 4, date = LocalDate.of(2026, 11, 1), workType = WorkType.CONTROL, comment = "тест"))

        val saved = db.gradeDao().getById(gId)!!
        assertEquals(4, saved.value)
        assertEquals(WorkType.CONTROL, saved.workType)
        assertEquals(LocalDate.of(2026, 11, 1), saved.date)

        db.gradeDao().update(saved.copy(value = 5))
        assertEquals(5, db.gradeDao().getById(gId)!!.value)

        db.gradeDao().delete(saved)
        assertNull(db.gradeDao().getById(gId))
    }

    @Test fun deletingDisciplineDeletesItsGrades() = runBlocking {
        val dId = db.disciplineDao().insert(Discipline(name = "Физика"))
        db.gradeDao().insert(Grade(disciplineId = dId, value = 3, date = LocalDate.of(2026, 10, 1), workType = WorkType.LAB))
        db.disciplineDao().delete(db.disciplineDao().getById(dId)!!)
        assertEquals(0, db.gradeDao().count())
    }

    @Test fun duplicateDisciplineNameIsRejected() = runBlocking {
        assertTrue(repo.addDiscipline("Базы данных", "Сидорова О. Н."))
        assertEquals(false, repo.addDiscipline("  базы данных ", null))
        assertEquals(1, db.disciplineDao().count())
    }

    @Test fun demoDataLoadsOnceAndHas16Grades() = runBlocking {
        repo.loadDemo()
        repo.loadDemo() // повторная загрузка не создаёт копий
        assertEquals(5, repo.disciplines.first().size)
        assertEquals(16, repo.grades.first().size)
    }

    @Test fun duplicateGradesAreRemoved() = runBlocking {
        val dId = db.disciplineDao().insert(Discipline(name = "Английский язык"))
        val g = Grade(disciplineId = dId, value = 4, date = LocalDate.of(2026, 11, 12), workType = WorkType.CONTROL)
        db.gradeDao().insert(g)
        db.gradeDao().insert(g)
        db.gradeDao().insert(g.copy(value = 5))
        assertEquals(1, repo.removeDuplicateGrades())
        assertEquals(2, db.gradeDao().count())
    }

    @Test fun clearAllRemovesEverything() = runBlocking {
        repo.loadDemo()
        repo.clearAll()
        assertTrue(repo.isEmpty())
        assertEquals(0, db.gradeDao().count())
    }
}
