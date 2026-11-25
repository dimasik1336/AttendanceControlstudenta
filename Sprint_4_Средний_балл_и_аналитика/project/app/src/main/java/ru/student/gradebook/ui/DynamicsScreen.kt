package ru.student.gradebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.student.gradebook.data.Discipline
import ru.student.gradebook.domain.AverageCalculator
import ru.student.gradebook.domain.GradeFilter
import ru.student.gradebook.domain.GradeRules
import ru.student.gradebook.domain.PeriodAnalytics
import ru.student.gradebook.domain.PeriodStat

/** Динамика успеваемости: средний балл по месяцам (таблица и столбчатая диаграмма). */
@Composable
fun DynamicsScreen(viewModel: GradebookViewModel, onBack: () -> Unit) {
    val home by viewModel.home.collectAsStateWithLifecycle()
    val disciplines by viewModel.disciplines.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<Discipline?>(null) }

    val grades = home.allGrades.filter(GradeFilter(disciplineId = selected?.id)::matches)
    val stats = PeriodAnalytics.byMonth(grades)

    Scaffold(topBar = { BackTopBar("Динамика", onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DropdownField(
                    label = "Дисциплина", options = disciplines, selected = selected,
                    optionLabel = { it.name }, onSelect = { selected = it }, allowNone = "Все дисциплины"
                )
            }
            if (stats.isEmpty()) {
                item {
                    EmptyState("Данных для динамики нет", "Добавьте оценки хотя бы за один месяц — здесь появится сравнение средних баллов по периодам.")
                }
            } else {
                item { TrendSummary(stats) }
                item { BarChart(stats) }
                item { Text("По периодам", style = MaterialTheme.typography.titleSmall) }
                items(stats, key = { it.period.toString() }) { StatRow(it) }
                item {
                    Text(
                        "Изменение считается относительно среднего балла предыдущего периода. " +
                            "«Нарастающим итогом» — средний балл по всем оценкам до конца месяца включительно.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendSummary(stats: List<PeriodStat>) {
    val last = stats.last()
    val text = when {
        stats.size == 1 -> "Пока есть данные только за один период: ${PeriodAnalytics.label(last.period)}."
        last.delta == null -> ""
        last.delta > 0.004 -> "В ${PeriodAnalytics.label(last.period)} средний балл вырос на ${AverageCalculator.format(last.delta)}."
        last.delta < -0.004 -> "В ${PeriodAnalytics.label(last.period)} средний балл снизился на ${AverageCalculator.format(-last.delta)}."
        else -> "В ${PeriodAnalytics.label(last.period)} средний балл не изменился."
    }
    SectionCard { Text(text, style = MaterialTheme.typography.bodyLarge) }
}

@Composable
private fun BarChart(stats: List<PeriodStat>) {
    SectionCard {
        Text("Средний балл по месяцам", style = MaterialTheme.typography.titleSmall)
        Row(
            Modifier.fillMaxWidth().height(170.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            stats.forEach { stat ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(AverageCalculator.format(stat.average), style = MaterialTheme.typography.labelMedium)
                    Box(
                        Modifier
                            .width(36.dp)
                            .height((stat.average / GradeRules.MAX_GRADE * 110).dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(PeriodAnalytics.label(stat.period).substringBefore(' ').take(3), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun StatRow(stat: PeriodStat) {
    SectionCard {
        Text(PeriodAnalytics.label(stat.period), style = MaterialTheme.typography.titleSmall)
        LabeledRow("Средний балл", AverageCalculator.format(stat.average))
        LabeledRow("Изменение", AverageCalculator.formatDelta(stat.delta))
        LabeledRow("Нарастающим итогом", AverageCalculator.format(stat.cumulativeAverage))
        LabeledRow("Оценок", stat.count.toString())
    }
}
