package com.misterio.curakid.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.model.LegalDocument
import com.misterio.core.domain.usecase.DownloadLegalDocumentUseCase
import com.misterio.core.domain.usecase.GetLegalDocumentsUseCase
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
    private val getLegalDocumentsUseCase: GetLegalDocumentsUseCase,
    private val downloadLegalDocumentUseCase: DownloadLegalDocumentUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<String>> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private val _legalDocuments = MutableStateFlow< UiState<List<LegalDocument>>>(UiState.Initial)
    val legalDocuments = _legalDocuments.asStateFlow()

    private val _documentContent = MutableStateFlow<UiState<String>>(UiState.Initial)
    val documentContent = _documentContent.asStateFlow()

    init {
        loadLegalDocuments()
    }

    private fun loadLegalDocuments() {
        viewModelScope.launch {
            getLegalDocumentsUseCase()
                .onSuccess { docs -> _legalDocuments.value = UiState.Success(docs) }
                .onFailure { error -> 
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Unknown error", error)
                    _legalDocuments.value = UiState.Error(domainError)
                }
        }
    }

    fun signUp(email: String, password: String, name: String, isAccepted: Boolean) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            registerUseCase(email, password, name, isAccepted)
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

    fun downloadDocument(url: String) {
        viewModelScope.launch {
            _documentContent.value = UiState.Loading
            downloadLegalDocumentUseCase(url)
                .onSuccess { content ->
                    _documentContent.value = UiState.Success(content)
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Unknown error", error)
                    _documentContent.value = UiState.Error(domainError)
                }
        }
    }

    fun clearDocumentContent() {
        _documentContent.value = UiState.Initial
    }
}
