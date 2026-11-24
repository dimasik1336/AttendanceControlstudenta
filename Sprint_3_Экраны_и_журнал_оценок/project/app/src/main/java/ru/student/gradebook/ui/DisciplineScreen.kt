package ru.student.gradebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.student.gradebook.data.Discipline
import ru.student.gradebook.data.Grade
import ru.student.gradebook.data.WorkType
import ru.student.gradebook.domain.AverageCalculator
import ru.student.gradebook.domain.GradeRules
import ru.student.gradebook.domain.RiskAnalyzer
import ru.student.gradebook.domain.ScenarioForecast
import ru.student.gradebook.domain.SortOrder
import ru.student.gradebook.domain.TargetCalculator
import ru.student.gradebook.domain.WeightedCalculator
import ru.student.gradebook.domain.WorkWeights
import ru.student.gradebook.ui.theme.WarnBackground
import ru.student.gradebook.ui.theme.WarnColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplineScreen(
    viewModel: GradebookViewModel,
    disciplineId: Long,
    onBack: () -> Unit,
    onAddGrade: (Long) -> Unit,
    onEditGrade: (Long, Long) -> Unit
) {
    val home by viewModel.home.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val summary = home.summaries.firstOrNull { it.discipline.id == disciplineId }

    var gradeToDelete by remember { mutableStateOf<Grade?>(null) }
    var confirmDeleteDiscipline by remember { mutableStateOf(false) }
    var fromText by remember { mutableStateOf("") }
    var toText by remember { mutableStateOf("") }
    var nextType by remember { mutableStateOf(WorkType.CONTROL) }

    LaunchedEffect(disciplineId) { viewModel.resetFilter() }

    if (summary == null) {
        Scaffold(topBar = { BackTopBar("Дисциплина", onBack) }) { padding ->
            EmptyState("Дисциплина не найдена", "Возможно, она была удалена.", Modifier.padding(padding))
        }
        return
    }

    val discipline = summary.discipline
    val visible = viewModel.visibleGrades(summary.grades, disciplineId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(discipline.name, maxLines = 1) },
                navigationIcon = { BackIcon(onBack) },
                actions = {
                    IconButton(onClick = { confirmDeleteDiscipline = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить дисциплину")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddGrade(disciplineId) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Оценка") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    discipline.teacherName?.let { Text("Преподаватель: $it", style = MaterialTheme.typography.bodyMedium) }
                    LabeledRow("Средний балл", AverageCalculator.format(summary.average))
                    LabeledRow("Взвешенный средний балл", AverageCalculator.format(summary.weightedAverage))
                    LabeledRow("Оценок", summary.grades.size.toString())
                }
            }
            if (summary.risk.needsAttention) {
                item {
                    Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = WarnBackground)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Требует внимания", style = MaterialTheme.typography.titleSmall, color = WarnColor)
                            summary.risk.reasons.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
            }
            item { GoalCard(discipline, summary.grades, nextType, onTypeChange = { nextType = it }) { viewModel.updateDiscipline(it) } }
            item { ScenarioCard(summary.grades, nextType) }
            item {
                FilterCard(
                    workType = filter.workType,
                    fromText = fromText,
                    toText = toText,
                    sort = sort,
                    onWorkType = { viewModel.setFilter(filter.copy(workType = it)) },
                    onFrom = { text ->
                        fromText = text
                        applyDate(text) { viewModel.setFilter(filter.copy(from = it)) }
                    },
                    onTo = { text ->
                        toText = text
                        applyDate(text) { viewModel.setFilter(filter.copy(to = it)) }
                    },
                    onSort = { viewModel.setSort(it) },
                    onReset = { fromText = ""; toText = ""; viewModel.resetFilter() }
                )
            }
            if (summary.grades.isEmpty()) {
                item {
                    EmptyState("Оценок по этой дисциплине пока нет", "Нажмите «Оценка», чтобы добавить первую запись.")
                }
            } else if (visible.isEmpty()) {
                item {
                    EmptyState("Ничего не найдено", "Нет оценок, подходящих под выбранные фильтры. Измените условия или сбросьте фильтр.")
                }
            } else {
                item {
                    Text(
                        "Оценки: ${visible.size} из ${summary.grades.size}" +
                            if (filter.isActive) " · средний по выборке ${AverageCalculator.format(AverageCalculator.average(visible))}" else "",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                items(visible, key = { it.id }) { grade ->
                    GradeRow(grade, onEdit = { onEditGrade(disciplineId, grade.id) }, onDelete = { gradeToDelete = grade })
                }
            }
        }
    }

    gradeToDelete?.let { g ->
        ConfirmDialog(
            title = "Удалить оценку?",
            text = "Оценка ${g.value} от ${GradeRules.formatDate(g.date)} будет удалена.",
            confirmLabel = "Удалить",
            onConfirm = { viewModel.deleteGrade(g); gradeToDelete = null },
            onDismiss = { gradeToDelete = null }
        )
    }
    if (confirmDeleteDiscipline) {
        val n = summary.grades.size
        ConfirmDialog(
            title = "Удалить дисциплину?",
            text = if (n == 0) "Дисциплина «${discipline.name}» будет удалена."
            else "Вместе с дисциплиной «${discipline.name}» будут удалены все её оценки: $n ${RiskAnalyzer.plural(n, "запись", "записи", "записей")}.",
            confirmLabel = "Удалить",
            onConfirm = {
                confirmDeleteDiscipline = false
                viewModel.deleteDiscipline(discipline)
                onBack()
            },
            onDismiss = { confirmDeleteDiscipline = false }
        )
    }
}

/** Пустая строка сбрасывает границу периода; корректная дата применяется; некорректный ввод игнорируется до исправления. */
private fun applyDate(text: String, apply: (java.time.LocalDate?) -> Unit) {
    if (text.isBlank()) apply(null) else GradeRules.parseDate(text)?.let(apply)
}

@Composable
private fun GradeRow(grade: Grade, onEdit: () -> Unit, onDelete: () -> Unit) {
    val color = when (grade.value) {
        5 -> Color(0xFF2E7D32)
        4 -> Color(0xFF558B2F)
        3 -> Color(0xFFEF6C00)
        else -> Color(0xFFC62828)
    }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                Text(grade.value.toString(), color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f)) {
                Text("${grade.workType.label} · вес ${WorkWeights.label(grade.workType)}", style = MaterialTheme.typography.titleSmall)
                Text(GradeRules.formatDate(grade.date), style = MaterialTheme.typography.bodySmall)
                if (grade.comment.isNotBlank()) Text(grade.comment, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Редактировать") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Удалить") }
        }
    }
}

@Composable
private fun FilterCard(
    workType: WorkType?,
    fromText: String,
    toText: String,
    sort: SortOrder,
    onWorkType: (WorkType?) -> Unit,
    onFrom: (String) -> Unit,
    onTo: (String) -> Unit,
    onSort: (SortOrder) -> Unit,
    onReset: () -> Unit
) {
    SectionCard {
        Text("Фильтры и сортировка", style = MaterialTheme.typography.titleSmall)
        DropdownField(
            label = "Тип работы", options = WorkType.entries.toList(), selected = workType,
            optionLabel = { it.label }, onSelect = onWorkType, allowNone = "Все типы"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val fromError = fromText.isNotBlank() && GradeRules.parseDate(fromText) == null
            val toError = toText.isNotBlank() && GradeRules.parseDate(toText) == null
            OutlinedTextField(
                value = fromText, onValueChange = onFrom, label = { Text("С даты") }, singleLine = true,
                isError = fromError, supportingText = { Text(if (fromError) "Формат: дд.мм.гггг" else "дд.мм.гггг") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = toText, onValueChange = onTo, label = { Text("По дату") }, singleLine = true,
                isError = toError, supportingText = { Text(if (toError) "Формат: дд.мм.гггг" else "дд.мм.гггг") },
                modifier = Modifier.weight(1f)
            )
        }
        DropdownField(
            label = "Сортировка", options = SortOrder.entries.toList(), selected = sort,
            optionLabel = { it.label }, onSelect = { s -> s?.let(onSort) }
        )
        TextButton(onClick = onReset) { Text("Сбросить фильтры") }
    }
}

@Composable
private fun GoalCard(
    discipline: Discipline,
    grades: List<Grade>,
    nextType: WorkType,
    onTypeChange: (WorkType) -> Unit,
    onSave: (Discipline) -> Unit
) {
    var text by remember(discipline.id, discipline.targetAverage) {
        mutableStateOf(discipline.targetAverage?.let { AverageCalculator.format(it) } ?: "")
    }
    val target = GradeRules.parseTarget(text)
    val error = text.isNotBlank() && (target == null || target < GradeRules.MIN_GRADE || target > GradeRules.MAX_GRADE)
    SectionCard {
        Text("Цель по дисциплине", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = text, onValueChange = { text = it },
            label = { Text("Целевой средний балл") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = error,
            supportingText = { Text(if (error) "Введите число от 2 до 5" else "Например, 4,5") },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownField(
            label = "Вид следующей работы", options = WorkType.entries.toList(), selected = nextType,
            optionLabel = { "${it.label} (вес ${WorkWeights.label(it)})" }, onSelect = { t -> t?.let(onTypeChange) }
        )
        if (target != null && !error) {
            Text("Для среднего арифметического: " + describeTarget(TargetCalculator.calculate(grades, target)), style = MaterialTheme.typography.bodyMedium)
            Text("Для взвешенного среднего: " + describeTarget(WeightedCalculator.requiredNext(grades, target, nextType)), style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !error, onClick = { onSave(discipline.copy(targetAverage = if (text.isBlank()) null else target)) }) { Text("Сохранить цель") }
            if (discipline.targetAverage != null) {
                OutlinedButton(onClick = { text = ""; onSave(discipline.copy(targetAverage = null)) }) { Text("Убрать") }
            }
        }
    }
}

@Composable
private fun ScenarioCard(grades: List<Grade>, nextType: WorkType) {
    val scenarios = ScenarioForecast.compare(grades, nextType)
    SectionCard {
        Text("Сценарный прогноз", style = MaterialTheme.typography.titleSmall)
        Text("Если следующая работа — «${nextType.label}»:", style = MaterialTheme.typography.bodySmall)
        scenarios.forEach { s ->
            Text(
                "Оценка ${s.grade}: средний ${AverageCalculator.format(s.average)} (${AverageCalculator.formatDelta(s.averageDelta)}), " +
                    "взвешенный ${AverageCalculator.format(s.weightedAverage)} (${AverageCalculator.formatDelta(s.weightedDelta)})",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
