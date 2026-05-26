@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.curakid.core.common.PasscodeMode
import com.misterio.curakid.core.common.SecurityManager
import com.misterio.curakid.core.common.TokenManager
import com.misterio.curakid.core.common.TokenState
import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.usecase.RefreshTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val tokenManager: TokenManager,
    connectivityRepository: ConnectivityRepository,
    val securityManager: SecurityManager,
) : ViewModel() {

    val isConnected = connectivityRepository.isConnected

    private val passcodeUnlocked = MutableStateFlow(false)

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
            securityManager.clearPasscode()
            tokenManager.clearTokens()
            appDataStore.clear()
            passcodeUnlocked.value = false
        }
    }

    init {
        Log.d(TAG, "INIT")
    }

    private companion object {
        private const val TAG = "MainVM"
    }
}
