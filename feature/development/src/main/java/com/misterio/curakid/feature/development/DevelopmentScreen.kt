package com.misterio.curakid.feature.development

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.ui.LocalScaffoldController
import com.misterio.core.ui.TrackFab
import com.misterio.core.ui.components.AiReportCard
import com.misterio.core.ui.components.CuraKidButton
import com.misterio.core.ui.components.CuraKidPullToRefreshBox
import com.misterio.core.ui.components.ErrorView
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun DevelopmentScreen(
    onAddMeasurements: (List<String>) -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    val viewModel: DevelopmentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()

    val scaffoldController = LocalScaffoldController.current
    val snackbarHostState = scaffoldController.snackbarHostState

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            if (uiState.showEarlyTransitionDialog) return@let

            if (uiState.lifePeriods.isNotEmpty()) {
                val result = snackbarHostState.showSnackbar(
                    message = error.message ?: "Виникла помилка",
                    actionLabel = "Повторити",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.refresh()
                }
            }
        }
    }

    TrackFab(
        show = true,
        icon = Icons.Default.Add,
        onClick = { onAddMeasurements(uiState.lifePeriods.map { it.label }) }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading && uiState.lifePeriods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && uiState.lifePeriods.isEmpty()) {
            ErrorView(
                error = uiState.error!!,
                onRetry = { viewModel.refresh() }
            )
        } else {
            CuraKidPullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() }
            ) {
                DevelopmentContent(
                    uiState = uiState,
                    selectedChild = selectedChild,
                    onPeriodSelect = { viewModel.selectPeriod(it) },
                    onAnswerChange = { qId, value -> viewModel.onAnswerChange(qId, value) },
                    onCompletePeriod = { viewModel.completePeriod() },
                    onChatClick = onChatClick
                )
            }
        }
    }

    if (uiState.showAgeTransitionPrompt) {
        AgeTransitionDialog(
            readiness = uiState.readiness,
            onConfirm = { viewModel.completePeriod() },
            onDismiss = { viewModel.dismissAgeTransitionPrompt() }
        )
    }

    if (uiState.showEarlyTransitionDialog) {
        EarlyTransitionDialog(
            readiness = uiState.readiness,
            onConfirm = { viewModel.completePeriod(allowEarlyTransition = true) },
            onDismiss = { viewModel.dismissEarlyTransitionDialog() }
        )
    }
}

@Composable
private fun DevelopmentContent(
    uiState: DevelopmentUiState,
    selectedChild: Child?,
    onPeriodSelect: (LifePeriod) -> Unit,
    onAnswerChange: (UUID, Double?) -> Unit,
    onCompletePeriod: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val currentPeriod = uiState.currentPeriod

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = rememberListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        itemWrapper {
            ScreenHeader(
                title = stringResource(R.string.development),
                childName = selectedChild?.name.orEmpty(),
                onChatClick = onChatClick
            )
        }

        item {
            PeriodTimeline(
                periods = uiState.lifePeriods,
                selectedId = currentPeriod?.id ?: UUID.randomUUID(),
                onSelect = onPeriodSelect
            )
        }

        itemWrapper {
            PeriodDescriptionCard(description = currentPeriod?.about)
        }

        itemsIndexed(
            items = currentPeriod?.questions.orEmpty(),
            key = { _, it -> it.id },
            contentType = { _, _ -> "question" }
        ) { _, question ->
            QuizCard(
                question = question,
                selectedOptionValue = uiState.answers.find { it.questionId == question.id }?.option?.value,
                onAnswerChange = { value -> onAnswerChange(question.id, value) }
            )
        }

        itemWrapper {
            AnimatedVisibility(
                visible = uiState.showSummary,
                enter = expandVertically() + fadeIn()
            ) {
                PeriodSummaryCard(summary = uiState.summary)
            }
        }

        itemWrapper {
            val report = uiState.currentAiReport
            val isPolling = uiState.isPollingReport

            if (report != null || isPolling) {
                AiReportCard(
                    title = "Аналіз періоду розвитку",
                    reportText = report?.reportText ?: "ШІ аналізує відповіді та готує звіт...",
                    recommendations = report?.recommendations ?: "",
                    isLoading = isPolling
                )
            }
        }

        itemWrapper {
            if (!uiState.isCompleted && currentPeriod != null) {
                val readiness = uiState.readiness
                val isMinMet = readiness?.isReady ?: uiState.isMinAnswersRequirementMet

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CuraKidButton(
                        onClick = onCompletePeriod,
                        modifier = Modifier.fillMaxWidth(),
                        text = if (readiness != null) "Завершити (${readiness.completionPercent}%)" else "Завершити період",
                        loading = uiState.isLoading,
                        containerColor = if (isMinMet) CurakidTheme.colors.primary else CurakidTheme.colors.surfaceContainerHigh
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DevelopmentScreenPreview() {
    CurakidTheme {
        DevelopmentContent(
            uiState = DevelopmentUiState(),
            selectedChild = null,
            onPeriodSelect = {},
            onAnswerChange = { _, _ -> },
            onCompletePeriod = {},
            onChatClick = {}
        )
    }
}
