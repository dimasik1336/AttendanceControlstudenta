package ru.student.gradebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ru.student.gradebook.ui.AppNavigation
import ru.student.gradebook.ui.GradebookViewModel
import ru.student.gradebook.ui.theme.GradeBookTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GradebookViewModel by viewModels { GradebookViewModel.Factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GradeBookTheme {
                AppNavigation(viewModel)
            }
        }
    }
}
