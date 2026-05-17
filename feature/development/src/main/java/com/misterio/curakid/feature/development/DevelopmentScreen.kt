@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.Question
import com.misterio.core.ui.TrackFab
import com.misterio.core.ui.components.AiReportCard
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.feature.development.R


// ─── Models ──────────────────────────────────────────────────────────────────

enum class MilestoneCategory(
    val label: String,
    val iconBg: Color,
    val iconTint: Color
) {
    MOTOR(label = "Моторика", iconBg = CuraKidColors.Blue95, iconTint = CuraKidColors.Blue40),
    SPEECH(label = "Мовлення", iconBg = CuraKidColors.Purple90, iconTint = CuraKidColors.Purple30),
    COGNITION(label = "Когніція", iconBg = CuraKidColors.Teal90, iconTint = CuraKidColors.Teal30),
    SOCIAL(label = "Соціальний", iconBg = CuraKidColors.Amber90, iconTint = CuraKidColors.Amber30);

    companion object {
        fun fromDomain(domain: String): MilestoneCategory = when (domain.uppercase()) {
            "MOTOR", "GROSS_MOTOR", "FINE_MOTOR" -> MOTOR
            "SPEECH", "LANGUAGE" -> SPEECH
            "COGNITION", "COGNITIVE" -> COGNITION
            "SOCIAL" -> SOCIAL
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

@Composable
fun DevelopmentScreen(
    onAddMeasurements: () -> Unit = {},
) {
    val viewModel: DevelopmentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val currentPeriod = uiState.currentPeriod
    val summary =
        currentPeriod?.questions?.toSummary(uiState.answers) ?: MilestoneSummary(0, 0, 0, 0)
    var showSummary by remember(currentPeriod?.id) {
        mutableStateOf(summary.completed == summary.totalQuestions && summary.totalQuestions > 0)
    }

    TrackFab(show = true, icon = Icons.Default.Add, onClick = onAddMeasurements)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = rememberListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        itemWrapper {
            ScreenHeader(title = "Розвиток", childName = selectedChild?.name ?: "")
        }

        item {
            PeriodTimeline(
                periods = uiState.lifePeriods,
                selectedId = currentPeriod?.id ?: "",
                onSelect = { code ->
                    viewModel.selectPeriod(code)
                }
            )
        }

        if (currentPeriod != null) {
            itemWrapper {
                PeriodProgressRow(
                    periodLabel = currentPeriod.label + " місяць",
                    summary = summary
                )
            }
        }

        // itemWrapper {
        //     MeasurementsCard(measurements = measurements)
        // }

        itemsIndexed(
            items = currentPeriod?.questions ?: emptyList(),
            key = { _, it -> it.id }
        ) { _, question ->
            QuizCard(
                question = question,
                selectedOptionValue = uiState.answers.find { it.questionId == question.id }?.optionId?.value,
                onAnswerChange = { value ->
                    if (value == null) {
                        viewModel.onAnswerCleared(question.id)
                        showSummary = false
                        return@QuizCard
                    }
                    val targetValue = when (value) {
                        "YES" -> "1.0"
                        "PARTIAL" -> "0.5"
                        "NO" -> "0.0"
                        else -> value
                    }
                    val optionId = question.options.find { it.value == targetValue }?.id
                        ?: run {
                            Log.e(
                                "DevelopmentScreen",
                                "onAnswerChange: targetValue $targetValue (from $value) not found in question ${question.id}"
                            )
                            Log.e(
                                "DevelopmentScreen",
                                "Available options: ${question.options.map { it.value }}"
                            )
                            return@QuizCard
                        }
                    Log.d(
                        "DevelopmentScreen",
                        "onAnswerChange: questionId=${question.id}, optionId=$optionId"
                    )
                    viewModel.onAnswerSelected(question.id, optionId)
                    // check if all answered after this change
                    val updatedCompleted =
                        summary.completed + (if (uiState.answers.none { it.questionId == question.id }) 1 else 0)
                    if (updatedCompleted == summary.totalQuestions) showSummary = true
                }
            )
        }

        // Summary card after all questions answered
        itemWrapper {
            AnimatedVisibility(
                visible = showSummary,
                enter = expandVertically() + fadeIn()
            ) {
                PeriodSummaryCard(summary = summary)
            }
        }

        itemWrapper {
            AiReportCard(
                title = "Аналіз розвитку від ШІ",
                reportText = uiState.aiReports.firstOrNull { it.lifePeriodId == currentPeriod?.id }?.reportText
                    ?: "",
                recommendations = uiState.aiReports.firstOrNull { it.lifePeriodId == currentPeriod?.id }?.recommendations
                    ?: "",
            )
        }

        itemWrapper {
            Button(
                onClick = { viewModel.completePeriod() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
            ) {
                Text("Завершити період")
            }
//            if (uiState.aiReports.isNotEmpty() && uiState.aiReports.any { it.periodId == currentPeriod?.id }) {
//                Button(
//                    onClick = { viewModel.completePeriod() },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !uiState.isLoading,
//                ) {
//                    Text("regenerate")
//                }
//            } else {
//                Button(
//                    onClick = { viewModel.completePeriod() },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !uiState.isLoading,
//                ) {
//                    Text("Завершити період")
//                }
//            }
        }
    }

    if (uiState.showEarlyTransitionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissEarlyTransitionDialog() },
            title = { Text("Ранній перехід") },
            text = {
                Text(
                    uiState.error
                        ?: "Ви ще не досягли необхідного віку або не відповіли на всі питання. Бажаєте продовжити?"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.completePeriod(allowEarlyTransition = true) }) {
                    Text("Так, продовжити")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissEarlyTransitionDialog() }) {
                    Text("Скасувати")
                }
            }
        )
    }

//    if (uiState.completionResult != null) {
//        AlertDialog(
//            onDismissRequest = { /* Handle completion */ },
//            title = { Text("Період завершено!") },
//            text = {
//                Text("Вітаємо! Період успішно завершено.")
//            },
//            confirmButton = {
//                TextButton(onClick = { /* Handle completion */ }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun MilestoneHeader(
    childName: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CurakidTheme.paddings.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.development),
                style = CurakidTheme.typography.headlineSmall,
                color = CurakidTheme.colors.onBackground,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = stringResource(R.string.observe_development),
                style = CurakidTheme.typography.bodySmall
            )
        }

        Text(
            text = childName,
            style = CurakidTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
            )
        )
    }
}

@Composable
private fun PeriodTimeline(
    periods: List<LifePeriod>,
    selectedId: String,
    onSelect: (LifePeriod) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium),
        horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
    ) {
        items(periods) { period ->
            PeriodItem(
                label = period.label + " міс",
                isSelected = period.id == selectedId,
                onClick = { onSelect(period) }
            )
        }
    }
}

@Composable
private fun PeriodItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val barColor = if (isSelected) CuraKidColors.Blue40 else CuraKidColors.Blue90
    val labelColor = if (isSelected) CuraKidColors.Blue40 else CuraKidColors.Neutral80

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(58.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = CurakidTheme.typography.labelSmall.copy(color = labelColor),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ─── Period progress row ──────────────────────────────────────────────────────

@Composable
private fun PeriodProgressRow(
    periodLabel: String,
    summary: MilestoneSummary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = summary.progressFraction,
        animationSpec = tween(600),
        label = "period_progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = periodLabel,
                style = CurakidTheme.typography.titleMedium.copy(color = CuraKidColors.Blue30)
            )
            Text(
                text = "${summary.completed} / ${summary.totalQuestions} виконано",
                style = CurakidTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(CurakidTheme.paddings.extraSmall))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = CuraKidColors.Blue40,
            trackColor = CuraKidColors.Blue90,
            strokeCap = StrokeCap.Round
        )
    }
}


@Composable
private fun QuizCard(
    question: Question,
    selectedOptionValue: String?,
    onAnswerChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val paddings = CurakidTheme.paddings
    val category = MilestoneCategory.fromDomain(question.domain)

    OutlinedCard(
        modifier = modifier
            .padding(horizontal = paddings.medium)
            .fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {

            // Category row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = paddings.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CurakidTheme.Radius.sm)
                        .background(category.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.iconTint,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = category.label,
                    style = CurakidTheme.typography.labelMedium.copy(color = CuraKidColors.Neutral40)
                )
            }

            // Question text
            Text(
                text = question.text,
                style = CurakidTheme.typography.bodyMedium.copy(
                    color = CuraKidColors.Blue30,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Answer row — if already answered show badge + edit
            if (selectedOptionValue != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnswerBadge(optionValue = selectedOptionValue)
                    Text(
                        text = "Змінити",
                        style = CurakidTheme.typography.labelMedium.copy(color = CuraKidColors.Neutral40),
                        modifier = Modifier.clickable { onAnswerChange(null) }
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnswerOption(
                        label = "Так",
                        icon = Icons.Default.Check,
                        activeColor = CuraKidColors.Teal40,
                        activeBg = CuraKidColors.Teal90,
                        activeBorder = CuraKidColors.Teal40,
                        isSelected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange("YES") }
                    )
                    AnswerOption(
                        label = "Частково",
                        icon = Icons.Default.Remove,
                        activeColor = CuraKidColors.Amber30,
                        activeBg = CuraKidColors.Amber90,
                        activeBorder = CuraKidColors.Amber30,
                        isSelected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange("PARTIAL") }
                    )
                    AnswerOption(
                        label = "Ні",
                        icon = Icons.Default.Close,
                        activeColor = CuraKidColors.Red30,
                        activeBg = CuraKidColors.Red90,
                        activeBorder = CuraKidColors.Red30,
                        isSelected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange("NO") }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
    label: String,
    icon: ImageVector,
    activeColor: Color,
    activeBg: Color,
    activeBorder: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(CurakidTheme.Radius.md)
            .background(if (isSelected) activeBg else CuraKidColors.White)
            .border(
                0.5.dp,
                if (isSelected) activeBorder else CuraKidColors.Blue90,
                CurakidTheme.Radius.md
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else CuraKidColors.Neutral40,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = CurakidTheme.typography.labelSmall.copy(
                color = if (isSelected) activeColor else CuraKidColors.Neutral40
            )
        )
    }
}

@Composable
private fun AnswerBadge(optionValue: String) {
    val (bg, color, icon, text) = when (optionValue) {
        "YES", "1.0" -> Quadruple(
            CuraKidColors.Teal90,
            CuraKidColors.Teal40,
            Icons.Default.Check,
            "Виконує"
        )

        "PARTIAL", "0.5" -> Quadruple(
            CuraKidColors.Amber90,
            CuraKidColors.Amber30,
            Icons.Default.Remove,
            "Частково"
        )

        "NO", "0.0" -> Quadruple(
            CuraKidColors.Red90,
            CuraKidColors.Red30,
            Icons.Default.Close,
            "Не виконує"
        )

        else -> Quadruple(
            CuraKidColors.Blue95, CuraKidColors.Blue40,
            Icons.AutoMirrored.Filled.HelpOutline, ""
        )
    }
    Row(
        modifier = Modifier
            .clip(CurakidTheme.Radius.full)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
        Text(text, style = CurakidTheme.typography.labelSmall.copy(color = color))
    }
}

// ─── Period summary card ──────────────────────────────────────────────────────

@Composable
private fun PeriodSummaryCard(summary: MilestoneSummary) {
    val paddings = CurakidTheme.paddings

    val hasIssues = summary.answeredNo > 0
    val (titleColor, icon) = if (hasIssues) Pair(
        CuraKidColors.Red30, Icons.Default.Info
    ) else Pair(
        CuraKidColors.Teal30, Icons.Default.CheckCircle
    )

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
    ) {
        Column(modifier = Modifier.padding(paddings.medium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = paddings.small)
            ) {
                Icon(icon, null, tint = titleColor, modifier = Modifier.size(20.dp))
                Text(
                    text = if (hasIssues) "Зверніть увагу" else "Чудово!",
                    style = CurakidTheme.typography.bodyMedium.copy(color = titleColor)
                )
            }

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(paddings.small),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                SummaryStatChip(
                    count = summary.answeredYes,
                    label = "Виконує",
                    bg = CuraKidColors.Teal90,
                    color = CuraKidColors.Teal40
                )
                SummaryStatChip(
                    count = summary.answeredPartial,
                    label = "Частково",
                    bg = CuraKidColors.Amber90,
                    color = CuraKidColors.Amber30
                )
                SummaryStatChip(
                    count = summary.answeredNo,
                    label = "Не виконує",
                    bg = CuraKidColors.Red90,
                    color = CuraKidColors.Red30
                )
            }

            Text(
                text = if (hasIssues)
                    "Є кілька навичок, що ще не розвинулись. Рекомендуємо обговорити з педіатром на наступному візиті."
                else
                    "Всі навички цього періоду розвиваються відповідно до норми. Продовжуйте в тому ж дусі!",
                style = CurakidTheme.typography.bodySmall.copy(color = titleColor)
            )
        }
    }
}

@Composable
private fun SummaryStatChip(count: Int, label: String, bg: Color, color: Color) {
    Row(
        modifier = Modifier
            .clip(CurakidTheme.Radius.full)
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$count",
            style = CurakidTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = label,
            style = CurakidTheme.typography.labelSmall.copy(color = color)
        )
    }
}

@Composable
private fun AddMeasurementsButton(onClick: () -> Unit) {
    val paddings = CurakidTheme.paddings

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddings.medium),
        shape = CurakidTheme.Radius.lg,
        colors = ButtonDefaults.buttonColors(containerColor = CuraKidColors.Blue40)
    ) {
        Icon(Icons.Default.Straighten, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            text = "Додати заміри",
            style = CurakidTheme.typography.bodyMedium.copy(color = CuraKidColors.White),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}


// ─── Helpers ─────────────────────────────────────────────────────────────────

private val MilestoneCategory.icon
    get() = when (this) {
        MilestoneCategory.MOTOR -> Icons.AutoMirrored.Filled.DirectionsRun
        MilestoneCategory.SPEECH -> Icons.Default.RecordVoiceOver
        MilestoneCategory.COGNITION -> Icons.Default.Psychology
        MilestoneCategory.SOCIAL -> Icons.Default.People
    }

private fun List<Question>.toSummary(answers: List<AnswerUI>) = MilestoneSummary(
    totalQuestions = size,
    answeredYes = count { q -> answers.any { it.questionId == q.id && (it.optionId.value == "YES" || it.optionId.value == "1.0") } },
    answeredPartial = count { q -> answers.any { it.questionId == q.id && (it.optionId.value == "PARTIAL" || it.optionId.value == "0.5") } },
    answeredNo = count { q -> answers.any { it.questionId == q.id && (it.optionId.value == "NO" || it.optionId.value == "0.0") } }
)

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
