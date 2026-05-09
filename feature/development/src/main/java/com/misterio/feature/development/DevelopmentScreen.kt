@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.feature.development

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.Question
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme


// ─── Models ──────────────────────────────────────────────────────────────────

enum class MilestoneAnswer(val value: String) {
    YES("YES"), PARTIAL("PARTIAL"), NO("NO"), UNANSWERED("UNANSWERED")
}

fun String?.toMilestoneAnswer(): MilestoneAnswer = when (this) {
    "YES" -> MilestoneAnswer.YES
    "PARTIAL" -> MilestoneAnswer.PARTIAL
    "NO" -> MilestoneAnswer.NO
    else -> MilestoneAnswer.UNANSWERED
}

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
    viewModel: DevelopmentViewModel = hiltViewModel(),
    onAddMeasurements: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()

    val currentPeriod = uiState.currentPeriod
    val summary = currentPeriod?.questions?.toSummary(uiState.answers) ?: MilestoneSummary(0, 0, 0, 0)
    var showSummary by remember(currentPeriod?.id) {
        mutableStateOf(summary.completed == summary.totalQuestions && summary.totalQuestions > 0)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = rememberListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        itemWrapper {
            MilestoneHeader(
                childName = selectedChild?.name ?: "",
            )
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
                    periodLabel = currentPeriod.label,
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
                selectedAnswer = uiState.answers[question.id].toMilestoneAnswer(),
                onAnswerChange = { answer ->
                    viewModel.onAnswerSelected(question.id, answer.value)
                    // check if all answered after this change
                    val updatedCompleted =
                        summary.completed + (if (uiState.answers[question.id] == null) 1 else 0)
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
        
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.completePeriod() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Завершити період")
            }
            Spacer(Modifier.height(8.dp))
            AddMeasurementsButton(onClick = onAddMeasurements)
        }
    }

    if (uiState.showEarlyTransitionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissEarlyTransitionDialog() },
            title = { Text("Ранній перехід") },
            text = { Text(uiState.error ?: "Ви ще не досягли необхідного віку або не відповіли на всі питання. Бажаєте продовжити?") },
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

    if (uiState.completionResult != null) {
        AlertDialog(
            onDismissRequest = { /* Handle completion */ },
            title = { Text("Період завершено!") },
            text = { 
                Text("Вітаємо! Період успішно завершено.") 
            },
            confirmButton = {
                TextButton(onClick = { /* Handle completion */ }) {
                    Text("OK")
                }
            }
        )
    }
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
                style = CurakidTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                )
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
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium),
        horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
    ) {
        items(periods) { period ->
            PeriodItem(
                label = period.label,
                isSelected = period.id == selectedId,
                onClick = { onSelect(period.code) }
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
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
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
    selectedAnswer: MilestoneAnswer,
    onAnswerChange: (MilestoneAnswer) -> Unit,
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
            if (selectedAnswer != MilestoneAnswer.UNANSWERED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnswerBadge(answer = selectedAnswer)
                    Text(
                        text = "Змінити",
                        style = CurakidTheme.typography.labelMedium.copy(color = CuraKidColors.Neutral40),
                        modifier = Modifier.clickable { onAnswerChange(MilestoneAnswer.UNANSWERED) }
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
                        onClick = { onAnswerChange(MilestoneAnswer.YES) }
                    )
                    AnswerOption(
                        label = "Частково",
                        icon = Icons.Default.Remove,
                        activeColor = CuraKidColors.Amber30,
                        activeBg = CuraKidColors.Amber90,
                        activeBorder = CuraKidColors.Amber30,
                        isSelected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(MilestoneAnswer.PARTIAL) }
                    )
                    AnswerOption(
                        label = "Ні",
                        icon = Icons.Default.Close,
                        activeColor = CuraKidColors.Red30,
                        activeBg = CuraKidColors.Red90,
                        activeBorder = CuraKidColors.Red30,
                        isSelected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(MilestoneAnswer.NO) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun AnswerBadge(answer: MilestoneAnswer) {
    val (bg, color, icon, text) = when (answer) {
        MilestoneAnswer.YES -> Quadruple(
            CuraKidColors.Teal90,
            CuraKidColors.Teal40,
            Icons.Default.Check,
            "Виконує"
        )

        MilestoneAnswer.PARTIAL -> Quadruple(
            CuraKidColors.Amber90,
            CuraKidColors.Amber30,
            Icons.Default.Remove,
            "Частково"
        )

        MilestoneAnswer.NO -> Quadruple(
            CuraKidColors.Red90,
            CuraKidColors.Red30,
            Icons.Default.Close,
            "Не виконує"
        )

        else -> Quadruple(CuraKidColors.Blue95, CuraKidColors.Blue40,
            Icons.AutoMirrored.Filled.HelpOutline, "")
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
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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

private fun List<Question>.toSummary(answers: Map<String, String>) = MilestoneSummary(
    totalQuestions = size,
    answeredYes = count { answers[it.id] == "YES" },
    answeredPartial = count { answers[it.id] == "PARTIAL" },
    answeredNo = count { answers[it.id] == "NO" }
)

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
