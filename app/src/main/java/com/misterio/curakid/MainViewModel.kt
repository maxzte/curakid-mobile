package com.misterio.curakid

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.AuthState
import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.sync.SyncController
import com.misterio.core.domain.usecase.GetAuthStateUseCase
import com.misterio.core.domain.usecase.GetIsPasscodeEnabledUseCase
import com.misterio.core.domain.usecase.LogoutUseCase
import com.misterio.core.domain.usecase.RegisterFcmTokenUseCase
import com.misterio.curakid.core.common.PasscodeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase,
    getIsPasscodeEnabledUseCase: GetIsPasscodeEnabledUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    connectivityRepository: ConnectivityRepository,
    private val logoutUseCase: LogoutUseCase,
    syncController: SyncController,
) : ViewModel() {

    val isConnected = connectivityRepository.isConnected
    val isSyncing = syncController.isSyncing

    private val passcodeUnlocked = MutableStateFlow(false)

    private val _pendingEventId = MutableStateFlow<UUID?>(null)
    val pendingEventId = _pendingEventId.asStateFlow()

    private val _pendingReportId = MutableStateFlow<UUID?>(null)
    val pendingReportId = _pendingReportId.asStateFlow()

    fun setPendingEventId(eventId: String?) {
        eventId?.let {
            try {
                _pendingEventId.value = UUID.fromString(it)
            } catch (e: Exception) {
                Log.e(TAG, "Invalid eventId: $it", e)
            }
        }
    }

    fun clearPendingEventId() {
        _pendingEventId.value = null
    }

    fun setPendingReportId(reportId: String?) {
        reportId?.let {
            try {
                _pendingReportId.value = UUID.fromString(it)
            } catch (e: Exception) {
                Log.e(TAG, "Invalid reportId: $it", e)
            }
        }
    }

    fun clearPendingReportId() {
        _pendingReportId.value = null
    }

    val startDestination: StateFlow<Route?> =
        combine(
            getAuthStateUseCase(),
            getIsPasscodeEnabledUseCase(),
            passcodeUnlocked
        ) { authState, isPasscodeEnabled, unlocked ->
            if (isPasscodeEnabled && !unlocked) {
                return@combine Route.Passcode(PasscodeMode.UNLOCK)
            }

            when (authState) {
                AuthState.LOADING -> null
                AuthState.UNAUTHENTICATED -> Route.Login
                AuthState.NEEDS_ONBOARDING -> Route.RegisterChild
                AuthState.AUTHENTICATED -> Route.HomeRoot
            }
        }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

    fun onPasscodeSuccess() {
        passcodeUnlocked.value = true
    }

    fun resetApp() {
        viewModelScope.launch {
            logoutUseCase()
            passcodeUnlocked.value = false
        }
    }

    init {
        Log.d(TAG, "INIT")
        syncFcmToken()
    }

    private fun syncFcmToken() {
        viewModelScope.launch {
            registerFcmTokenUseCase()
        }
    }

    private companion object {
        private const val TAG = "MainVM"
    }
}
