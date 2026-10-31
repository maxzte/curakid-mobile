package com.misterio.curakid.feature.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.misterio.core.domain.model.Child
import com.misterio.core.ui.theme.CurakidTheme

enum class MedicalCategory(
    val title: String,
    val getIconBg: @Composable () -> Color,
    val getIconTint: @Composable () -> Color,
    val getChipBg: @Composable () -> Color,
    val getChipText: @Composable () -> Color,
    val suggestions: List<String>
) {
    ALLERGIES(
        title = "Алергії",
        getIconBg = { CurakidTheme.colors.errorContainer },
        getIconTint = { CurakidTheme.colors.error },
        getChipBg = { CurakidTheme.colors.errorContainer },
        getChipText = { CurakidTheme.colors.error },
        suggestions = listOf(
            "Пеніцилін", "Арахіс", "Лактоза", "Глютен",
            "Пилок", "Шерсть тварин", "Укуси комах",
            "Латекс", "Морепродукти", "Яйця", "Мед"
        )
    ),
    CHRONIC(
        title = "Хронічні хвороби",
        getIconBg = { CurakidTheme.colors.tertiaryContainer },
        getIconTint = { CurakidTheme.colors.tertiary },
        getChipBg = { CurakidTheme.colors.tertiaryContainer },
        getChipText = { CurakidTheme.colors.tertiary },
        suggestions = listOf(
            "Бронхіальна астма", "Цукровий діабет 1 типу",
            "Атопічний дерматит", "Епілепсія",
            "Целіакія", "Муковісцидоз", "Гіпотиреоз",
            "Аутизм (ASD)", "СДУГ"
        )
    ),
    PAST(
        title = "Перенесені хвороби",
        getIconBg = { CurakidTheme.colors.secondaryContainer },
        getIconTint = { CurakidTheme.colors.secondary },
        getChipBg = { CurakidTheme.colors.secondaryContainer },
        getChipText = { CurakidTheme.colors.secondary },
        suggestions = listOf(
            "Вітряна віспа", "Кір", "Краснуха",
            "Паротит", "Коклюш", "Скарлатина",
            "Мононуклеоз", "Гепатит A", "COVID-19"
        )
    ),
    FEATURES(
        title = "Особливості",
        getIconBg = { CurakidTheme.colors.primaryContainer },
        getIconTint = { CurakidTheme.colors.primary },
        getChipBg = { CurakidTheme.colors.primaryContainer },
        getChipText = { CurakidTheme.colors.primary },
        suggestions = listOf(
            "Недоношена/ний", "Носить окуляри",
            "Слуховий апарат", "Вегетаріанець",
            "Веган", "Ортодонтичне лікування",
            "Логопедична терапія", "ЛФК"
        )
    );

    val icon: ImageVector
        get() = when (this) {
            ALLERGIES -> Icons.Default.Warning
            CHRONIC -> Icons.Default.MonitorHeart
            PAST -> Icons.Default.CheckCircle
            FEATURES -> Icons.Default.Star
        }
}

fun Child.getMedicalItems(category: MedicalCategory): List<String> = when (category) {
    MedicalCategory.ALLERGIES -> allergies
    MedicalCategory.CHRONIC -> chronicDiseases
    MedicalCategory.PAST -> pastDiseases
    MedicalCategory.FEATURES -> specialFeatures
}

fun Child.withMedicalItems(category: MedicalCategory, items: List<String>): Child = when (category) {
    MedicalCategory.ALLERGIES -> copy(allergies = items)
    MedicalCategory.CHRONIC -> copy(chronicDiseases = items)
    MedicalCategory.PAST -> copy(pastDiseases = items)
    MedicalCategory.FEATURES -> copy(specialFeatures = items)
}

val Child.initials: String
    get() = name.split(" ").let { parts ->
        val first = parts.firstOrNull()?.firstOrNull() ?: ""
        val last = if (parts.size > 1) parts[1].firstOrNull() ?: "" else ""
        "$first$last"
    }.uppercase()
