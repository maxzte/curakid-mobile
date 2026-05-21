package com.misterio.curakid.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.usecase.ForgotPasswordUseCase
import com.misterio.core.domain.usecase.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ForgotPasswordStep {
    data object RequestEmail : ForgotPasswordStep
    data object ResetPassword : ForgotPasswordStep
    data object Success : ForgotPasswordStep
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Initial)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _step = MutableStateFlow<ForgotPasswordStep>(ForgotPasswordStep.RequestEmail)
    val step: StateFlow<ForgotPasswordStep> = _step.asStateFlow()

    fun requestReset(email: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            forgotPasswordUseCase(email)
                .onSuccess {
                    _uiState.value = UiState.Initial
                    _step.value = ForgotPasswordStep.ResetPassword
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Помилка при запиті на скидання паролю")
                }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            resetPasswordUseCase(email, code, newPassword)
                .onSuccess {
                    _uiState.value = UiState.Success(Unit)
                    _step.value = ForgotPasswordStep.Success
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Помилка при зміні паролю")
                }
        }
    }
}
