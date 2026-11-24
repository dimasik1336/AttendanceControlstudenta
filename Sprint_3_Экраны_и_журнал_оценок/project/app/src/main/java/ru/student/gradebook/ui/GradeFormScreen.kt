package ru.student.gradebook.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.student.gradebook.data.Grade
import ru.student.gradebook.data.WorkType
import ru.student.gradebook.domain.GradeRules
import java.time.LocalDate

/** Форма добавления и редактирования оценки (gradeId < 0 — новая оценка). */
@Composable
fun GradeFormScreen(viewModel: GradebookViewModel, disciplineId: Long, gradeId: Long, onClose: () -> Unit) {
    val disciplines by viewModel.disciplines.collectAsStateWithLifecycle()
    val isEdit = gradeId >= 0

    var selectedDisciplineId by remember { mutableLongStateOf(disciplineId) }
    var valueText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(GradeRules.formatDate(LocalDate.now())) }
    var workType by remember { mutableStateOf(WorkType.PRACTICE) }
    var comment by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(GradeRules.GradeErrors()) }
    var disciplineError by remember { mutableStateOf<String?>(null) }

    // при редактировании подставляем сохранённые значения
    LaunchedEffect(gradeId) {
        if (isEdit) {
            viewModel.getGrade(gradeId)?.let { g ->
                selectedDisciplineId = g.disciplineId
                valueText = g.value.toString()
                dateText = GradeRules.formatDate(g.date)
                workType = g.workType
                comment = g.comment
            }
        }
    }

    Scaffold(topBar = { BackTopBar(if (isEdit) "Редактирование оценки" else "Новая оценка", onClose) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DropdownField(
                label = "Дисциплина",
                options = disciplines,
                selected = disciplines.firstOrNull { it.id == selectedDisciplineId },
                optionLabel = { it.name },
                onSelect = { d -> d?.let { selectedDisciplineId = it.id; disciplineError = null } },
                errorText = disciplineError
            )
            OutlinedTextField(
                value = valueText,
                onValueChange = { valueText = it; errors = errors.copy(value = null) },
                label = { Text("Оценка (${GradeRules.MIN_GRADE}–${GradeRules.MAX_GRADE})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errors.value != null,
                supportingText = errors.value?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it; errors = errors.copy(date = null) },
                label = { Text("Дата (дд.мм.гггг)") },
                singleLine = true,
                isError = errors.date != null,
                supportingText = errors.date?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Тип работы",
                options = WorkType.entries.toList(),
                selected = workType,
                optionLabel = { it.label },
                onSelect = { t -> t?.let { workType = it } }
            )
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it; errors = errors.copy(comment = null) },
                label = { Text("Комментарий") },
                isError = errors.comment != null,
                supportingText = { Text(errors.comment ?: "${comment.length}/${GradeRules.MAX_COMMENT_LENGTH}") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val result = GradeRules.validateGrade(valueText, dateText, comment)
                        val discipline = disciplines.firstOrNull { it.id == selectedDisciplineId }
                        errors = result.errors
                        disciplineError = if (discipline == null) "Выберите дисциплину" else null
                        if (!result.errors.hasErrors && discipline != null) {
                            viewModel.saveGrade(
                                Grade(
                                    id = if (isEdit) gradeId else 0,
                                    disciplineId = discipline.id,
                                    value = result.value!!,
                                    date = result.date!!,
                                    workType = workType,
                                    comment = comment.trim()
                                ),
                                onDone = onClose
                            )
                        }
                    }
                ) { Text("Сохранить") }
            }
        }
    }
}
