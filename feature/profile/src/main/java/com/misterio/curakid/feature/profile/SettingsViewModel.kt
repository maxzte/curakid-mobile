package com.misterio.curakid.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.DeleteProfileUseCase
import com.misterio.core.domain.usecase.GetIsNotificationsEnabledUseCase
import com.misterio.core.domain.usecase.GetIsPasscodeEnabledUseCase
import com.misterio.core.domain.usecase.ToggleNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getIsNotificationsEnabledUseCase: GetIsNotificationsEnabledUseCase,
    private val toggleNotificationsUseCase: ToggleNotificationsUseCase,
    getIsPasscodeEnabledUseCase: GetIsPasscodeEnabledUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
) : ViewModel() {

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Initial)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    val isNotificationsEnabled: StateFlow<Boolean> = getIsNotificationsEnabledUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isPasscodeEnabled: StateFlow<Boolean> = getIsPasscodeEnabledUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            toggleNotificationsUseCase(enabled)
        }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _showDeleteDialog.value = show
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            deleteProfileUseCase()
                .onSuccess {
                    _deleteState.value = UiState.Success(Unit)
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Failed to delete account", error)
                    _deleteState.value = UiState.Error(domainError)
                }
        }
    }
}
