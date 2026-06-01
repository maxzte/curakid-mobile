@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.misterio.core.ui.components.CuraKidAlertDialog
import com.misterio.core.ui.components.ErrorView
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme


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
    onAddMeasurements: (List<String>) -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    val viewModel: DevelopmentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()

    val scaffoldController = com.misterio.core.ui.LocalScaffoldController.current
    val snackbarHostState = scaffoldController.snackbarHostState

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Don't show snackbar if we are showing the early transition dialog
            if (uiState.showEarlyTransitionDialog) return@let

            // Only show snackbar if we already have some data, otherwise ErrorView is better
            if (uiState.lifePeriods.isNotEmpty()) {
                val result = snackbarHostState.showSnackbar(
                    message = error.message ?: "Виникла помилка",
                    actionLabel = "Повторити",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    // Logic to retry could go here, e.g. viewModel.retry()
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            TrackFab(
                show = true,
                icon = Icons.Default.Add,
                onClick = { onAddMeasurements(uiState.lifePeriods.map { it.label }) })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding()) {
            if (uiState.isLoading && uiState.lifePeriods.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null && uiState.lifePeriods.isEmpty()) {
                ErrorView(
                    error = uiState.error!!,
                    onRetry = { /* viewModel.refresh() */ }
                )
            } else {
                DevelopmentContent(
                    uiState = uiState,
                    selectedChild = selectedChild,
                    viewModel = viewModel,
                    onAddMeasurements = onAddMeasurements,
                    onChatClick = onChatClick
                )
            }
        }
    }

    if (uiState.showEarlyTransitionDialog) {
        CuraKidAlertDialog(
            onDismissRequest = { viewModel.dismissEarlyTransitionDialog() },
            onConfirm = { viewModel.completePeriod(allowEarlyTransition = true) },
            title = "Ранній перехід",
            text = uiState.error?.message
                ?: "Ви ще не досягли необхідного віку або не відповіли на всі питання. Бажаєте продовжити?",
            confirmButtonText = "Так, продовжити",
            dismissButtonText = "Скасувати",
            confirmButtonColor = CuraKidColors.Blue40
        )
    }
}

@Composable
private fun DevelopmentContent(
    uiState: DevelopmentUiState,
    selectedChild: com.misterio.core.domain.model.Child?,
    viewModel: DevelopmentViewModel,
    onAddMeasurements: (List<String>) -> Unit,
    onChatClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val currentPeriod = uiState.currentPeriod
    val summary =
        currentPeriod?.questions?.toSummary(uiState.answers) ?: MilestoneSummary(0, 0, 0, 0)
    var showSummary by remember(currentPeriod?.id) {
        mutableStateOf(summary.completed == summary.totalQuestions && summary.totalQuestions > 0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = rememberListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        if (uiState.isLoading) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = CurakidTheme.colors.primary,
                    trackColor = CurakidTheme.colors.surfaceVariant
                )
            }
        }
        // ... (rest of the items from previous LazyColumn)

        itemWrapper {
            ScreenHeader(
                title = "Розвиток",
                childName = selectedChild?.name ?: "",
                onChatClick = onChatClick
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
                selectedOptionValue = uiState.answers.find { it.questionId == question.id }?.option?.value,
                onAnswerChange = { value ->
                    if (value == null) {
                        viewModel.onAnswerCleared(question.id)
                        showSummary = false
                        return@QuizCard
                    }
                    val optionId = question.options.find { it.value == value }?.id
                        ?: run {
                            Log.e(
                                "DevelopmentScreen",
                                "onAnswerChange: targetValue $value not found in question ${question.id}"
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
            val report = uiState.currentAiReport
            val reportText = report?.reportText ?: ""
            val recommendations = report?.recommendations ?: ""

            AnimatedVisibility(
                visible = uiState.isPollingReport || reportText.isNotBlank() || recommendations.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                AiReportCard(
                    title = "Аналіз розвитку від ШІ",
                    reportText = reportText,
                    recommendations = recommendations,
                    isLoading = uiState.isPollingReport
                )
            }
        }

        if (!uiState.isCompleted) {
            itemWrapper {
                Button(
                    onClick = { viewModel.completePeriod() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isMinAnswersRequirementMet) CurakidTheme.colors.primary else CuraKidColors.Neutral80
                    )
                ) {
                    Text("Завершити період")
                }
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }

    if (uiState.showEarlyTransitionDialog) {
        CuraKidAlertDialog(
            onDismissRequest = { viewModel.dismissEarlyTransitionDialog() },
            onConfirm = { viewModel.completePeriod(allowEarlyTransition = true) },
            title = "Ранній перехід",
            text = uiState.error?.message
                ?: "Ви ще не досягли необхідного віку або не відповіли на всі питання. Бажаєте продовжити?",
            confirmButtonText = "Так, продовжити",
            dismissButtonText = "Скасувати",
            confirmButtonColor = CuraKidColors.Blue40
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
    selectedOptionValue: Double?,
    onAnswerChange: (Double?) -> Unit,
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
                        isSelected = selectedOptionValue == 1.0,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(1.0) }
                    )
                    AnswerOption(
                        label = "Частково",
                        icon = Icons.Default.Remove,
                        activeColor = CuraKidColors.Amber30,
                        activeBg = CuraKidColors.Amber90,
                        activeBorder = CuraKidColors.Amber30,
                        isSelected = selectedOptionValue == 0.5,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(0.5) }
                    )
                    AnswerOption(
                        label = "Ні",
                        icon = Icons.Default.Close,
                        activeColor = CuraKidColors.Red30,
                        activeBg = CuraKidColors.Red90,
                        activeBorder = CuraKidColors.Red30,
                        isSelected = selectedOptionValue == 0.0,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(0.0) }
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
private fun AnswerBadge(optionValue: Double) {
    val (bg, color, icon, text) = when (optionValue) {
        1.0 -> Quadruple(
            CuraKidColors.Teal90,
            CuraKidColors.Teal40,
            Icons.Default.Check,
            "Виконує"
        )

        0.5 -> Quadruple(
            CuraKidColors.Amber90,
            CuraKidColors.Amber30,
            Icons.Default.Remove,
            "Частково"
        )

        0.0 -> Quadruple(
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
    answeredYes = count { q -> answers.any { it.questionId == q.id && (it.option?.value == 1.0) } },
    answeredPartial = count { q -> answers.any { it.questionId == q.id && (it.option?.value == 0.5) } },
    answeredNo = count { q -> answers.any { it.questionId == q.id && (it.option?.value == 0.0) } }
)

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
