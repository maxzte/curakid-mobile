package com.misterio.curakid.feature.profile

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.usecase.DeleteProfileUseCase
import com.misterio.curakid.core.common.SecurityManager
import com.misterio.curakid.core.common.SettingsManager
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
    private val settingsManager: SettingsManager,
    securityManager: SecurityManager,
    private val deleteProfileUseCase: DeleteProfileUseCase
) : ViewModel() {

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Initial)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    val isAppEnabled: StateFlow<Boolean> = settingsManager.isAppEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isNotificationsEnabled: StateFlow<Boolean> = settingsManager.isNotificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isPasscodeEnabled: StateFlow<Boolean> = securityManager.isPasscodeEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setAppEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.saveAppEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.saveNotificationsEnabled(enabled)
        }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _showDeleteDialog.value = show
    }

    fun deleteAccount(context: Context) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            deleteProfileUseCase()
                .onSuccess {
                    _deleteState.value = UiState.Success(Unit)
                    restartApp(context)
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Failed to delete account", error)
                    _deleteState.value = UiState.Error(domainError)
                }
        }
    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)

        Runtime.getRuntime().exit(0)
    }
}
