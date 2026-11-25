package ru.student.gradebook.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.student.gradebook.data.Discipline
import ru.student.gradebook.data.WorkType
import ru.student.gradebook.domain.AverageCalculator
import ru.student.gradebook.domain.GradeRules
import ru.student.gradebook.domain.TargetCalculator
import ru.student.gradebook.domain.TargetResult
import ru.student.gradebook.domain.WeightedCalculator
import ru.student.gradebook.domain.WorkWeights
import java.math.BigDecimal

/** Словесное объяснение результата расчёта целевого балла (единое для всех экранов). */
fun describeTarget(result: TargetResult): String = when (result) {
    TargetResult.InvalidTarget ->
        "целевой балл должен быть в диапазоне от ${GradeRules.MIN_GRADE} до ${GradeRules.MAX_GRADE}."
    is TargetResult.AnyGrade ->
        "цель уже достигнута: даже оценка ${GradeRules.MIN_GRADE} оставит средний балл не ниже цели " +
            "(${AverageCalculator.format(result.averageWithMinGrade)})."
    is TargetResult.Required ->
        "нужна оценка ${result.grade} (расчётное значение ${plain(result.exact)}). " +
            "Итоговый средний балл составит ${AverageCalculator.format(result.resultingAverage)}."
    is TargetResult.Unreachable ->
        "цель недостижима одной оценкой: потребовалась бы оценка ${plain(result.exact)}, а максимум — ${GradeRules.MAX_GRADE}. " +
            "При оценке ${GradeRules.MAX_GRADE} средний балл составит ${AverageCalculator.format(result.maxPossibleAverage)}. " +
            "Поставьте промежуточную цель."
}

private fun plain(value: BigDecimal): String = value.stripTrailingZeros().toPlainString().replace('.', ',')

@Composable
fun TargetScreen(viewModel: GradebookViewModel, onBack: () -> Unit) {
    val home by viewModel.home.collectAsStateWithLifecycle()
    val disciplines by viewModel.disciplines.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<Discipline?>(null) }
    var targetText by remember { mutableStateOf("") }
    var nextType by remember { mutableStateOf(WorkType.CONTROL) }

    val grades = if (selected == null) home.allGrades else home.allGrades.filter { it.disciplineId == selected?.id }
    val target = GradeRules.parseTarget(targetText)

    Scaffold(topBar = { BackTopBar("Целевой средний балл", onBack) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownField(
                label = "Для чего считать", options = disciplines, selected = selected,
                optionLabel = { it.name }, onSelect = { selected = it }, allowNone = "Общий средний балл"
            )
            OutlinedTextField(
                value = targetText, onValueChange = { targetText = it },
                label = { Text("Желаемый средний балл") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = targetText.isNotBlank() && target == null,
                supportingText = { Text(if (targetText.isNotBlank() && target == null) "Введите число, например 4,5" else "Например, 4,5") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Вид следующей работы", options = WorkType.entries.toList(), selected = nextType,
                optionLabel = { "${it.label} (вес ${WorkWeights.label(it)})" }, onSelect = { t -> t?.let { nextType = it } }
            )
            SectionCard {
                LabeledRow("Сейчас: средний балл", AverageCalculator.format(AverageCalculator.average(grades)))
                LabeledRow("Сейчас: взвешенный", AverageCalculator.format(WeightedCalculator.weightedAverage(grades)))
                LabeledRow("Оценок учтено", grades.size.toString())
            }
            if (target != null) {
                SectionCard {
                    Text("Результат", style = MaterialTheme.typography.titleSmall)
                    if (grades.isEmpty()) {
                        Text("Оценок пока нет — расчёт выполнен для первой оценки.", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Для среднего арифметического: " + describeTarget(TargetCalculator.calculate(grades, target)), style = MaterialTheme.typography.bodyMedium)
                    Text("Для взвешенного среднего: " + describeTarget(WeightedCalculator.requiredNext(grades, target, nextType)), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text("Введите желаемый средний балл, и приложение рассчитает, какая оценка нужна за следующую работу.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
