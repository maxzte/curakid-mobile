@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.misterio.curakid.feature.development

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.domain.model.Option
import com.misterio.core.domain.usecase.CompleteLifePeriodUseCase
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildAssessmentsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetLifePeriodsUseCase
import com.misterio.core.domain.usecase.PollReportStatusUseCase
import com.misterio.core.domain.usecase.SaveAssessmentDraftUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import com.misterio.core.domain.usecase.SyncChildAssessmentsUseCase
import com.misterio.core.domain.usecase.SyncLifePeriodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class AnswerUI(
    val assessmentId: String,
    val questionId: String,
    val optionId: Option
)

data class DevelopmentUiState(
    val currentPeriod: LifePeriod? = null,
    val currentAiReport: LifePeriodAiReport? = null,
    val answers: List<AnswerUI> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val completionResult: CompletePeriodResult? = null,
    val showEarlyTransitionDialog: Boolean = false,
    val lifePeriods: List<LifePeriod> = emptyList(),
    val aiReports: List<LifePeriodAiReport> = emptyList(),
    val pendingAnswer: Answer? = null
)

@HiltViewModel
class DevelopmentViewModel @Inject constructor(
    private val getLifePeriodsUseCase: GetLifePeriodsUseCase,
    private val getChildAssessmentsUseCase: GetChildAssessmentsUseCase,
    private val syncLifePeriodsUseCase: SyncLifePeriodsUseCase,
    private val syncChildAssessmentsUseCase: SyncChildAssessmentsUseCase,
    private val completeLifePeriodUseCase: CompleteLifePeriodUseCase,
    getChildUseCase: GetChildUseCase,
    private val saveAssessmentDraftUseCase: SaveAssessmentDraftUseCase,
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase,
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase,
    private val pollReportStatusUseCase: PollReportStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevelopmentUiState())
    val uiState: StateFlow<DevelopmentUiState> = _uiState.asStateFlow()

    val selectedChild: StateFlow<Child?> = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _saveTrigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        Log.i(TAG, "INIT")
        observeData()
        observeAutoSave()
    }

    private fun observeData() {
        viewModelScope.launch {
            selectedChild.collectLatest { child ->
                if (child == null) {
                    Log.d(TAG, "observeData: No child selected")
                    return@collectLatest
                }

                Log.d(TAG, "observeData: Starting for child ${child.id}")
                _uiState.update { it.copy(isLoading = true) }

                // Start sync in background
                launch {
                    try {
                        Log.d(TAG, "observeData: Triggering sync...")
                        syncLifePeriodsUseCase()
                        syncChildAssessmentsUseCase(childId = child.id)
                        syncChildAiReportsUseCase(childId = child.id)
                        Log.d(TAG, "observeData: Sync finished")
                    } catch (e: Exception) {
                        Log.e(TAG, "observeData: Sync error", e)
                    }
                }

                // Combine periods and assessments for a single update
                combine(
                    getLifePeriodsUseCase(),
                    getChildAssessmentsUseCase(childId = child.id),
                    getChildAiReportsUseCase(childId = child.id),
                ) { periods, assessments, reports ->
                    Log.d(
                        TAG,
                        "observeData: Flow emitted ${periods.size} periods and ${assessments.size} assessments"
                    )
                    val initialAnswersMap = mutableListOf<AnswerUI>()

                    assessments.forEach { assessment ->
                        assessment.answers.forEach { ans ->
                            val qId = ans.question?.id ?: return@forEach
                            val opt = ans.selectedOption ?: return@forEach
                            initialAnswersMap.add(
                                AnswerUI(
                                    assessmentId = assessment.id,
                                    questionId = qId,
                                    optionId = opt
                                )
                            )
                        }
                    }

                    _uiState.update { state ->
                        val currentPeriod = state.currentPeriod ?: run {
                            val ageInMonths = Period.between(child.birthDate, LocalDate.now())
                                .toTotalMonths()
                                .toInt()

                            periods.find { ageInMonths >= it.minMonths && ageInMonths <= it.maxMonths }
                                ?: periods.firstOrNull()
                        }
                        val currentAiReport = reports.find { it.lifePeriodId == currentPeriod?.id }

                        state.copy(
                            lifePeriods = periods,
                            aiReports = reports,
                            currentPeriod = currentPeriod,
                            currentAiReport = currentAiReport,
                            answers = initialAnswersMap,
                            isLoading = periods.isEmpty() && state.lifePeriods.isEmpty()
                        )
                    }
                }.collectLatest { }
            }
        }
    }

    private fun observeAutoSave() {
        viewModelScope.launch {
            _saveTrigger
                .debounce(1000L)
                .collectLatest {
                    val childId = selectedChild.value?.id ?: return@collectLatest
                    val state = _uiState.value
                    val answerToSave = state.pendingAnswer ?: return@collectLatest

                    _uiState.update { it.copy(isSaving = true) }
                    val result = saveAssessmentDraftUseCase(
                        periodId = state.currentPeriod?.id!!,
                        childId = childId,
                        answer = answerToSave,
                        skillAssessmentId = state.answers.firstOrNull { it.questionId == answerToSave.questionId }?.assessmentId
                    )
                    _uiState.update { it.copy(isSaving = false) }

                    result.onSuccess { _ ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                pendingAnswer = if (currentState.pendingAnswer == answerToSave) null else currentState.pendingAnswer
                            )
                        }
                    }
                }
        }
    }

    fun selectPeriod(period: LifePeriod) {
        _uiState.update { state ->
            state.copy(
                currentPeriod = period,
                currentAiReport = state.aiReports.find { it.lifePeriodId == period.id }
            )
        }
    }

    fun onAnswerSelected(questionId: String, optionId: String) {
        val currentState = _uiState.value
        val existingAnswer = currentState.answers.find { it.questionId == questionId }

        if (existingAnswer == null || existingAnswer.optionId.id == optionId) {
            return
        }

        val period = currentState.currentPeriod ?: return
        val question = period.questions.find { it.id == questionId } ?: return
        val option = question.options.find { it.id == optionId } ?: return

        _uiState.update { state ->
            val newAnswerUI = AnswerUI(
                assessmentId = existingAnswer.assessmentId,
                questionId = questionId,
                optionId = option
            )
            val newAnswers = state.answers.filterNot { it.questionId == questionId } + newAnswerUI
            state.copy(
                answers = newAnswers,
                pendingAnswer = Answer(questionId, optionId)
            )
        }
        _saveTrigger.tryEmit(Unit)
    }

    fun onAnswerCleared(questionId: String) {
        val currentState = _uiState.value
        if (currentState.answers.none { it.questionId == questionId }) {
            return
        }
        _uiState.update { state ->
            val newAnswers = state.answers.filterNot { it.questionId == questionId }
            state.copy(answers = newAnswers)
        }
        // Since backend doesn't support clearing, we don't trigger save here
        // or we might need a different approach if clearing is required.
    }

    fun completePeriod(allowEarlyTransition: Boolean = false) {
        val childId = selectedChild.value?.id ?: return
        val period = _uiState.value.currentPeriod ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    showEarlyTransitionDialog = false
                )
            }

            // Final save if pending
            val state = _uiState.value
            state.pendingAnswer?.let { pending ->
                saveAssessmentDraftUseCase(
                    periodId = state.currentPeriod?.id!!,
                    childId = childId,
                    skillAssessmentId = state.answers.first { it.questionId == pending.questionId }.assessmentId,
                    answer = pending
                )
            }

            val result = completeLifePeriodUseCase(childId, period.code, allowEarlyTransition)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        completionResult = result.getOrThrow()
                    )
                }
                waitForReport(
                    reportId = result.getOrThrow().reportId!!,
                    childId = childId
                )
            } else {
                val error = result.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error?.message,
//                        showEarlyTransitionDialog = error?.message?.contains(
//                            "early",
//                            ignoreCase = true
//                        ) == true
                    )
                }
            }
        }
    }

    private fun waitForReport(reportId: String, childId: String) {
        viewModelScope.launch {
            pollReportStatusUseCase(reportId).collect { result ->
                result.onSuccess { isReady ->
                    if (isReady) {
                        _uiState.update { it.copy(isLoading = false) }
                        syncChildAiReportsUseCase(childId = childId)
                    } else {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            }
        }
    }

    fun dismissEarlyTransitionDialog() {
        _uiState.update { it.copy(showEarlyTransitionDialog = false) }
    }

    private companion object {
        private const val TAG = "DevelopmentVM"
    }
}
