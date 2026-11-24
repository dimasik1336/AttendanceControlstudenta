package ru.student.gradebook.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.student.gradebook.data.AppDatabase
import ru.student.gradebook.data.Discipline
import ru.student.gradebook.data.Grade
import ru.student.gradebook.data.GradebookRepository
import ru.student.gradebook.domain.AverageCalculator
import ru.student.gradebook.domain.GradeFilter
import ru.student.gradebook.domain.GradeQuery
import ru.student.gradebook.domain.RiskAnalyzer
import ru.student.gradebook.domain.RiskReport
import ru.student.gradebook.domain.SortOrder
import ru.student.gradebook.domain.WeightedCalculator

/** Сводка по одной дисциплине для списка на главном экране. */
data class DisciplineSummary(
    val discipline: Discipline,
    val grades: List<Grade>,
    val average: Double?,
    val weightedAverage: Double?,
    val risk: RiskReport
)

data class HomeState(
    val isLoading: Boolean = true,
    val summaries: List<DisciplineSummary> = emptyList(),
    val overallAverage: Double? = null,
    val overallWeighted: Double? = null,
    val gradeCount: Int = 0,
    val allGrades: List<Grade> = emptyList()
)

class GradebookViewModel(app: Application) : ViewModel() {
    private val repo = GradebookRepository(AppDatabase.get(app))
    private val prefs = app.getSharedPreferences("gradebook_settings", Context.MODE_PRIVATE)

    /** Порог риска (усложнённая версия): ниже него дисциплина выделяется с объяснением причины. */
    private val _threshold = MutableStateFlow(
        prefs.getFloat(KEY_THRESHOLD, RiskAnalyzer.DEFAULT_THRESHOLD.toFloat()).toDouble()
    )
    val threshold: StateFlow<Double> = _threshold

    val disciplines: StateFlow<List<Discipline>> = repo.disciplines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val home: StateFlow<HomeState> = combine(repo.disciplines, repo.grades, _threshold) { disciplines, grades, threshold ->
        val byDiscipline = grades.groupBy { it.disciplineId }
        HomeState(
            isLoading = false,
            summaries = disciplines.map { d ->
                val list = byDiscipline[d.id].orEmpty()
                DisciplineSummary(
                    discipline = d,
                    grades = list,
                    average = AverageCalculator.average(list),
                    weightedAverage = WeightedCalculator.weightedAverage(list),
                    risk = RiskAnalyzer.analyze(list, threshold)
                )
            },
            overallAverage = AverageCalculator.average(grades),
            overallWeighted = WeightedCalculator.weightedAverage(grades),
            gradeCount = grades.size,
            allGrades = grades
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    // ---- состояние фильтров экрана дисциплины ----
    private val _filter = MutableStateFlow(GradeFilter())
    val filter: StateFlow<GradeFilter> = _filter
    private val _sort = MutableStateFlow(SortOrder.DATE_DESC)
    val sort: StateFlow<SortOrder> = _sort

    fun setFilter(filter: GradeFilter) = _filter.update { filter }
    fun resetFilter() = _filter.update { GradeFilter() }
    fun setSort(order: SortOrder) = _sort.update { order }

    /** Оценки выбранной дисциплины с учётом фильтра и сортировки. */
    fun visibleGrades(all: List<Grade>, disciplineId: Long): List<Grade> =
        GradeQuery.apply(all, _filter.value.copy(disciplineId = disciplineId), _sort.value)

    fun setThreshold(value: Double) {
        _threshold.update { value }
        prefs.edit().putFloat(KEY_THRESHOLD, value.toFloat()).apply()
    }

    // ---- операции с данными ----
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    fun consumeMessage() = _message.update { null }

    fun addDiscipline(name: String, teacher: String?) = viewModelScope.launch {
        if (!repo.addDiscipline(name, teacher)) _message.update { "Дисциплина «${name.trim()}» уже есть в списке" }
    }

    fun updateDiscipline(discipline: Discipline) = viewModelScope.launch { repo.updateDiscipline(discipline) }

    fun deleteDiscipline(discipline: Discipline) = viewModelScope.launch {
        repo.deleteDiscipline(discipline)
        _message.update { "Дисциплина и её оценки удалены" }
    }

    fun saveGrade(grade: Grade, onDone: () -> Unit) = viewModelScope.launch {
        repo.saveGrade(grade)
        onDone()
    }

    fun deleteGrade(grade: Grade) = viewModelScope.launch {
        repo.deleteGrade(grade)
        _message.update { "Оценка удалена" }
    }

    fun loadDemo() = viewModelScope.launch {
        repo.loadDemo()
        _message.update { "Демо-данные загружены" }
    }

    fun clearAll() = viewModelScope.launch {
        repo.clearAll()
        _message.update { "Все данные удалены" }
    }

    fun removeDuplicates() = viewModelScope.launch {
        val removed = repo.removeDuplicateGrades()
        _message.update { if (removed == 0) "Дубликатов не найдено" else "Удалено дубликатов: $removed" }
    }

    suspend fun getGrade(id: Long): Grade? = repo.getGrade(id)

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = GradebookViewModel(app) as T
    }

    private companion object {
        const val KEY_THRESHOLD = "risk_threshold"
    }
}
