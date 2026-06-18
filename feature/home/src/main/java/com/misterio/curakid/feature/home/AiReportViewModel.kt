@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.asUiState
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.domain.usecase.GetAiReportByIdUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

@HiltViewModel(assistedFactory = AiReportViewModel.Factory::class)
class AiReportViewModel @AssistedInject constructor(
    @Assisted private val reportId: UUID,
    private val getAiReportByIdUseCase: GetAiReportByIdUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(reportId: UUID): AiReportViewModel
    }

    val uiState = getAiReportByIdUseCase(reportId)
        .map { report -> report.text to report.recommendations }
        .asUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

}
