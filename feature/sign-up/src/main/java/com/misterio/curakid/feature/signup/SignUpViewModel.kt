package com.misterio.curakid.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.OAuthLoginUseCase
import com.misterio.core.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val oAuthLoginUseCase: OAuthLoginUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<String>> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            registerUseCase(email, password, name)
                .onSuccess { message ->
                    _uiState.value = UiState.Success(message)
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Unknown error", error)
                    _uiState.value = UiState.Error(domainError)
                }
        }
    }

    fun oAuthLogin(idToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            oAuthLoginUseCase(idToken)
                .onSuccess {
                    _uiState.value = UiState.Success("Login successful")
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
