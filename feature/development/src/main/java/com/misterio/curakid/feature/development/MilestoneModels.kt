package com.misterio.curakid.feature.development

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.misterio.core.domain.model.Question
import com.misterio.core.ui.theme.CuraKidColors

enum class MilestoneCategory(
    val label: String,
    val iconBg: Color,
    val iconTint: Color
) {
    MOTOR(label = "Моторика", iconBg = CuraKidColors.Blue95, iconTint = CuraKidColors.Blue40),
    SPEECH(label = "Мовлення", iconBg = CuraKidColors.Purple90, iconTint = CuraKidColors.Purple30),
    COGNITION(label = "Когніція", iconBg = CuraKidColors.Teal90, iconTint = CuraKidColors.Teal30),
    SOCIAL(label = "Соціальний", iconBg = CuraKidColors.Amber90, iconTint = CuraKidColors.Amber30),
    SENSORY(label = "Сенсорика", iconBg = CuraKidColors.Red90, iconTint = CuraKidColors.Red30),
    SELF_CARE(label = "Самообслуговування", iconBg = CuraKidColors.Teal90, iconTint = CuraKidColors.Teal30);

    val icon: ImageVector
        get() = when (this) {
            MOTOR -> Icons.AutoMirrored.Filled.DirectionsRun
            SPEECH -> Icons.Default.RecordVoiceOver
            COGNITION -> Icons.Default.Psychology
            SOCIAL -> Icons.Default.People
            SENSORY -> Icons.Default.Lightbulb
            SELF_CARE -> Icons.Default.Check
        }

    companion object {
        fun fromDomain(domain: String): MilestoneCategory = when (domain.uppercase()) {
            "MOTOR_GROSS", "MOTOR_FINE" -> MOTOR
            "SPEECH" -> SPEECH
            "COGNITIVE" -> COGNITION
            "SOCIAL_EMOTIONAL" -> SOCIAL
            "SENSORY" -> SENSORY
            "SELF_CARE" -> SELF_CARE
            else -> MOTOR
        }
    }
}

data class MilestoneSummary(
    val totalQuestions: Int,
    val answeredYes: Int,
    val answeredPartial: Int,
    val answeredNo: Int
) {
    val completed get() = answeredYes + answeredPartial + answeredNo
    val progressFraction get() = if (totalQuestions == 0) 0f else completed.toFloat() / totalQuestions
}

fun List<Question>.toSummary(answers: List<AnswerUI>): MilestoneSummary = MilestoneSummary(
    totalQuestions = size,
    answeredYes = count { q -> answers.any { it.questionId == q.id && (it.option?.value == 100.0) } },
    answeredPartial = count { q -> answers.any { it.questionId == q.id && (it.option?.value == 50.0) } },
    answeredNo = count { q -> answers.any { it.questionId == q.id && (it.option?.value == 0.0) } }
)
