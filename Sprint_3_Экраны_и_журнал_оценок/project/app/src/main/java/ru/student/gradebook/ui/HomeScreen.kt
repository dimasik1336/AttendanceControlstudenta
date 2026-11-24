package ru.student.gradebook.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.student.gradebook.domain.AverageCalculator
import ru.student.gradebook.domain.GradeRules
import ru.student.gradebook.domain.RiskAnalyzer
import ru.student.gradebook.ui.theme.WarnBackground
import ru.student.gradebook.ui.theme.WarnColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GradebookViewModel,
    onOpenDiscipline: (Long) -> Unit,
    onOpenDynamics: () -> Unit,
    onOpenTarget: () -> Unit
) {
    val state by viewModel.home.collectAsStateWithLifecycle()
    val threshold by viewModel.threshold.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Успеваемость") },
                actions = {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Меню") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Загрузить демо-данные") }, onClick = { menuOpen = false; viewModel.loadDemo() })
                        DropdownMenuItem(text = { Text("Удалить дубликаты оценок") }, onClick = { menuOpen = false; viewModel.removeDuplicates() })
                        DropdownMenuItem(text = { Text("Очистить все данные") }, onClick = { menuOpen = false; confirmClear = true })
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Дисциплина") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    Text("Общая сводка", style = MaterialTheme.typography.titleMedium)
                    LabeledRow("Средний балл", AverageCalculator.format(state.overallAverage))
                    LabeledRow("Взвешенный средний балл", AverageCalculator.format(state.overallWeighted))
                    LabeledRow(
                        "Оценок внесено",
                        "${state.gradeCount}"
                    )
                    Text(
                        "Средний балл — среднее арифметическое по всем записям (все оценки равнозначны). " +
                            "Взвешенный учитывает вес вида работы.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenDynamics, modifier = Modifier.weight(1f)) { Text("Динамика") }
                    OutlinedButton(onClick = onOpenTarget, modifier = Modifier.weight(1f)) { Text("Целевой балл") }
                }
            }
            item { ThresholdCard(threshold) { viewModel.setThreshold(it) } }

            if (!state.isLoading && state.summaries.isEmpty()) {
                item {
                    EmptyState(
                        title = "Дисциплин пока нет",
                        hint = "Добавьте первую дисциплину кнопкой «Дисциплина» или загрузите демонстрационные данные."
                    ) {
                        Button(onClick = { viewModel.loadDemo() }) { Text("Загрузить демо-данные") }
                    }
                }
            } else {
                item { Text("Дисциплины", style = MaterialTheme.typography.titleMedium) }
                items(state.summaries, key = { it.discipline.id }) { summary ->
                    DisciplineCard(summary) { onOpenDiscipline(summary.discipline.id) }
                }
            }
        }
    }

    if (showAddDialog) {
        DisciplineDialog(
            onConfirm = { name, teacher ->
                viewModel.addDiscipline(name, teacher)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
    if (confirmClear) {
        ConfirmDialog(
            title = "Очистить все данные?",
            text = "Будут удалены все дисциплины и оценки. Действие нельзя отменить.",
            confirmLabel = "Очистить",
            onConfirm = { confirmClear = false; viewModel.clearAll() },
            onDismiss = { confirmClear = false }
        )
    }
}

@Composable
private fun ThresholdCard(threshold: Double, onChange: (Double) -> Unit) {
    var slider by remember(threshold) { mutableFloatStateOf(threshold.toFloat()) }
    SectionCard {
        LabeledRow("Порог внимания", AverageCalculator.format(slider.toDouble()))
        Slider(
            value = slider,
            onValueChange = { slider = it },
            onValueChangeFinished = { onChange(Math.round(slider * 10) / 10.0) },
            valueRange = 2.5f..4.5f,
            steps = 19
        )
        Text("Дисциплины со средним баллом ниже порога выделяются с объяснением причины.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DisciplineCard(summary: DisciplineSummary, onClick: () -> Unit) {
    val attention = summary.risk.needsAttention
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (attention) WarnBackground else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(summary.discipline.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    summary.discipline.teacherName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
                Text(
                    AverageCalculator.format(summary.average, empty = "—"),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (attention) WarnColor else MaterialTheme.colorScheme.primary
                )
            }
            val n = summary.grades.size
            Text(
                "$n ${RiskAnalyzer.plural(n, "оценка", "оценки", "оценок")} · взвешенный ${AverageCalculator.format(summary.weightedAverage, "—")}",
                style = MaterialTheme.typography.bodySmall
            )
            if (attention) {
                Text("Требует внимания", style = MaterialTheme.typography.labelLarge, color = WarnColor)
                summary.risk.reasons.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun DisciplineDialog(onConfirm: (String, String?) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая дисциплина") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; error = null },
                    label = { Text("Название") }, singleLine = true,
                    isError = error != null, supportingText = error?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = teacher, onValueChange = { teacher = it },
                    label = { Text("Преподаватель (необязательно)") }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val e = GradeRules.validateDisciplineName(name)
                if (e != null) error = e else onConfirm(name.trim(), teacher.trim().ifBlank { null })
            }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
