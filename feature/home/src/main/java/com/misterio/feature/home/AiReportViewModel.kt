package com.misterio.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.usecase.RequestAIAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiReportViewModel @Inject constructor(
    private val requestAIAnalysisUseCase: RequestAIAnalysisUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun getReport(childId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            requestAIAnalysisUseCase(childId)
                .onSuccess { report ->
                    _uiState.value = UiState.Success(report)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to get report")
                }
        }
    }
}
