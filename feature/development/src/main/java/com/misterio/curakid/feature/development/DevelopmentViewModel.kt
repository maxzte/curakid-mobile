@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.misterio.curakid.feature.development

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.ChildAssessment
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class AnswerUI(
    val assessmentId: String?,
    val questionId: String,
    val optionId: Option?,
    val originalOptionId: String? = null
)

data class DevelopmentUiState(
    val currentPeriod: LifePeriod? = null,
    val currentAiReport: LifePeriodAiReport? = null,
    val answers: List<AnswerUI> = emptyList(),
    val isLoading: Boolean = false,
    val isPollingReport: Boolean = false,
    val isCompleted: Boolean = false,
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
            Log.d(TAG, "observeData: Starting flow")
            selectedChild
                .flatMapLatest { child ->
                    if (child == null) {
                        Log.d(TAG, "observeData: Child is null")
                        flowOf(null)
                    } else {
                        Log.d(TAG, "observeData: Child selected ${child.id}, triggering sync")
                        // Trigger sync in background
                        launch {
                            try {
                                syncLifePeriodsUseCase()
                                syncChildAssessmentsUseCase(childId = child.id)
                                syncChildAiReportsUseCase(childId = child.id)
                            } catch (e: Exception) {
                                Log.e(TAG, "observeData: Sync error", e)
                            }
                        }

                        combine(
                            getLifePeriodsUseCase(),
                            getChildAssessmentsUseCase(childId = child.id),
                            getChildAiReportsUseCase(childId = child.id),
                        ) { periods, assessments, reports ->
                            DataSnapshot(periods, assessments, reports, child)
                        }
                    }
                }
                .collectLatest { snapshot ->
                    if (snapshot == null) return@collectLatest

                    Log.d(
                        TAG,
                        "observeData: New snapshot - Periods: ${snapshot.periods.size}, Assessments: ${snapshot.assessments.size}, Reports: ${snapshot.reports.size}"
                    )

                    val initialAnswersMap = mutableListOf<AnswerUI>()
                    snapshot.assessments.forEach { assessment: ChildAssessment ->
                        assessment.answers.forEach { ans ->
                            val qId = ans.question?.id ?: return@forEach
                            val opt = ans.selectedOption ?: return@forEach
                            initialAnswersMap.add(
                                AnswerUI(
                                    assessmentId = assessment.id,
                                    questionId = qId,
                                    optionId = opt,
                                    originalOptionId = opt.id
                                )
                            )
                        }
                    }

                    _uiState.update { state ->
                        val currentPeriod = state.currentPeriod ?: run {
                            val ageInMonths =
                                Period.between(snapshot.child.birthDate, LocalDate.now())
                                    .toTotalMonths()
                                    .toInt()

                            snapshot.periods.find { ageInMonths >= it.minMonths && ageInMonths <= it.maxMonths }
                                ?: snapshot.periods.firstOrNull()
                        }

                        val currentAiReport =
                            snapshot.reports.find { it.lifePeriodId == currentPeriod?.id }

                        Log.d(TAG, "observeData: Mapping for period ${currentPeriod?.id}")
                        Log.d(TAG, "observeData: found report=${currentAiReport?.id}")
                        if (currentAiReport == null && snapshot.reports.isNotEmpty()) {
                            Log.d(
                                TAG,
                                "observeData: Available reports: ${snapshot.reports.map { "id=${it.id}, periodId=${it.lifePeriodId}" }}"
                            )
                        }

                        state.copy(
                            lifePeriods = snapshot.periods,
                            aiReports = snapshot.reports,
                            currentPeriod = currentPeriod,
                            currentAiReport = currentAiReport,
                            answers = initialAnswersMap,
                            isLoading = false,
                            isCompleted = currentAiReport != null
                        )
                    }
                }
        }
    }

    private data class DataSnapshot(
        val periods: List<LifePeriod>,
        val assessments: List<ChildAssessment>,
        val reports: List<LifePeriodAiReport>,
        val child: Child
    )

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

        if (existingAnswer?.optionId?.id == optionId) {
            return
        }

        val period = currentState.currentPeriod ?: return
        val question = period.questions.find { it.id == questionId } ?: return
        val option = question.options.find { it.id == optionId } ?: return

        if (existingAnswer?.originalOptionId == optionId) {
            _uiState.update { state ->
                val newAnswerUI = existingAnswer.copy(optionId = option)
                val newAnswers =
                    state.answers.filterNot { it.questionId == questionId } + newAnswerUI
                state.copy(answers = newAnswers)
            }
            return
        }

        _uiState.update { state ->
            val newAnswerUI = AnswerUI(
                assessmentId = existingAnswer?.assessmentId,
                questionId = questionId,
                optionId = option,
                originalOptionId = existingAnswer?.originalOptionId
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
        _uiState.update { state ->
            val newAnswers = state.answers.map {
                if (it.questionId == questionId) it.copy(optionId = null) else it
            }
            state.copy(answers = newAnswers)
        }
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
                        isCompleted = true,
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
            _uiState.update { it.copy(isPollingReport = true) }
            pollReportStatusUseCase(reportId).collect { result ->
                result.onSuccess { isReady ->
                    if (isReady) {
                        syncChildAiReportsUseCase(childId = childId)
                        _uiState.update { it.copy(isPollingReport = false) }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isPollingReport = false, error = error.message) }
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
