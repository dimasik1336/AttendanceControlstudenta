package ru.student.gradebook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary = Color(0xFF1F5FA8),
    secondary = Color(0xFF2E7D6B),
    tertiary = Color(0xFFB26A00),
    error = Color(0xFFB3261E)
)

val GoodColor = Color(0xFF2E7D32)
val WarnColor = Color(0xFFC62828)
val WarnBackground = Color(0xFFFDECEA)

@Composable
fun GradeBookTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
