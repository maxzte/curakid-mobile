@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.GetAiReportByIdUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel(assistedFactory = AiReportViewModel.Factory::class)
class AiReportViewModel @AssistedInject constructor(
    @Assisted private val reportId: UUID,
    private val getAiReportByIdUseCase: GetAiReportByIdUseCase,
    private val getChildUseCase: GetChildUseCase,
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(reportId: UUID): AiReportViewModel
    }

    private val _isSyncing = MutableStateFlow(false)
    private val _syncError = MutableStateFlow<DomainError?>(null)

    init {
        fetchReportIfMissing()
    }

    fun retry() {
        fetchReportIfMissing()
    }

    private fun fetchReportIfMissing() {
        viewModelScope.launch {
            val child = getChildUseCase().first() ?: return@launch
            _isSyncing.value = true
            _syncError.value = null
            syncChildAiReportsUseCase(child.id).onFailure { error ->
                _syncError.value = DomainError.fromThrowable(error)
            }
            _isSyncing.value = false
        }
    }

    val uiState: StateFlow<UiState<Pair<String, String>>> = combine(
        getAiReportByIdUseCase(reportId),
        _isSyncing,
        _syncError
    ) { report, isSyncing, syncError ->
        when {
            report.text.isNotEmpty() || report.recommendations.isNotEmpty() -> {
                UiState.Success(report.text to report.recommendations)
            }
            isSyncing -> UiState.Loading
            syncError != null -> UiState.Error(syncError)
            else -> UiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

}
