package ru.student.gradebook.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val HOME = "home"
    const val DISCIPLINE = "discipline/{id}"
    const val GRADE_FORM = "grade_form/{disciplineId}?gradeId={gradeId}"
    const val DYNAMICS = "dynamics"
    const val TARGET = "target"

    fun discipline(id: Long) = "discipline/$id"
    fun gradeForm(disciplineId: Long, gradeId: Long = -1L) = "grade_form/$disciplineId?gradeId=$gradeId"
}

@Composable
fun AppNavigation(viewModel: GradebookViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenDiscipline = { nav.navigate(Routes.discipline(it)) },
                onOpenDynamics = { nav.navigate(Routes.DYNAMICS) },
                onOpenTarget = { nav.navigate(Routes.TARGET) }
            )
        }
        composable(Routes.DISCIPLINE, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            DisciplineScreen(
                viewModel = viewModel,
                disciplineId = entry.arguments?.getLong("id") ?: -1L,
                onBack = { nav.popBackStack() },
                onAddGrade = { nav.navigate(Routes.gradeForm(it)) },
                onEditGrade = { disciplineId, gradeId -> nav.navigate(Routes.gradeForm(disciplineId, gradeId)) }
            )
        }
        composable(
            Routes.GRADE_FORM,
            arguments = listOf(
                navArgument("disciplineId") { type = NavType.LongType },
                navArgument("gradeId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { entry ->
            GradeFormScreen(
                viewModel = viewModel,
                disciplineId = entry.arguments?.getLong("disciplineId") ?: -1L,
                gradeId = entry.arguments?.getLong("gradeId") ?: -1L,
                onClose = { nav.popBackStack() }
            )
        }
        composable(Routes.DYNAMICS) { DynamicsScreen(viewModel, onBack = { nav.popBackStack() }) }
        composable(Routes.TARGET) { TargetScreen(viewModel, onBack = { nav.popBackStack() }) }
    }
}
