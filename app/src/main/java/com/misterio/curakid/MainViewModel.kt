@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.repository.NotificationRepository
import com.misterio.core.domain.usecase.LogoutUseCase
import com.misterio.core.domain.util.suspendRunCatching
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.curakid.core.common.PasscodeMode
import com.misterio.curakid.core.common.SecurityManager
import com.misterio.curakid.core.common.TokenManager
import com.misterio.curakid.core.common.TokenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val tokenManager: TokenManager,
    connectivityRepository: ConnectivityRepository,
    private val securityManager: SecurityManager,
    private val notificationRepository: NotificationRepository,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    val isConnected = connectivityRepository.isConnected

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
            tokenManager.tokenState,
            appDataStore.onboardingCompleted,
            securityManager.isPasscodeEnabled,
            passcodeUnlocked
        ) { tokenState, onboardingCompleted, isPasscodeEnabled, unlocked ->
            if (isPasscodeEnabled && !unlocked) {
                return@combine Route.Passcode(PasscodeMode.UNLOCK)
            }

            when (tokenState) {
                is TokenState.Loading -> null
                is TokenState.Empty -> Route.Login
                is TokenState.Available -> if (onboardingCompleted) Route.HomeRoot else Route.RegisterChild
            }
        }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    fun onPasscodeSuccess() {
        passcodeUnlocked.value = true
    }

    fun resetApp() {
        viewModelScope.launch {
            suspendRunCatching {
                val fcmToken = suspendCancellableCoroutine<String?> { cont ->
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                cont.resume(task.result)
                            } else {
                                cont.resume(null)
                            }
                        }
                }
                if (!fcmToken.isNullOrBlank()) {
                    notificationRepository.removeDeviceToken(fcmToken)
                }
            }.onFailure { e ->
                Log.w(TAG, "Failed to remove FCM token on logout", e)
            }
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
            tokenManager.tokenState.collect { state ->
                if (state is TokenState.Available && state.token.isNotEmpty()) {
                    suspendRunCatching {
                        val fcmToken = suspendCancellableCoroutine<String?> { cont ->
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        cont.resume(task.result)
                                    } else {
                                        cont.resume(null)
                                    }
                                }
                        }
                        if (!fcmToken.isNullOrBlank()) {
                            notificationRepository.registerDeviceToken(fcmToken)
                        }
                    }.onFailure { e ->
                        Log.w(TAG, "Failed to retrieve or sync FCM token", e)
                    }
                }
            }
        }
    }

    private companion object {
        private const val TAG = "MainVM"
    }
}
