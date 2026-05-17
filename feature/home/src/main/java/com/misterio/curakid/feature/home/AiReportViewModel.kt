@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.asUiState
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AiReportViewModel @Inject constructor(
    getChildUseCase: GetChildUseCase,
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase,
) : ViewModel() {

    val uiState = getChildUseCase().flatMapLatest { child ->
        if (child != null) {
            getChildAiReportsUseCase(childId = child.id).asUiState()
        } else {
            flowOf(UiState.Loading)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

}
