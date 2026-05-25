package com.misterio.curakid.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.VerifyEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val verifyEmailUseCase: VerifyEmailUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<Unit>> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    fun verifyEmail(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            verifyEmailUseCase(email, code)
                .onSuccess {
                    _uiState.value = UiState.Success(Unit)
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Unknown error", error)
                    _uiState.value = UiState.Error(domainError)
                }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Initial
    }
}
