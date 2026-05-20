@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.common.AppDataStore
import com.misterio.core.common.TokenManager
import com.misterio.core.common.TokenState
import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.usecase.RefreshTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val refreshTokenUseCase: RefreshTokenUseCase,
    appDataStore: AppDataStore,
    tokenManager: TokenManager,
    connectivityRepository: ConnectivityRepository,
) : ViewModel() {

    val isConnected = connectivityRepository.isConnected

    val startDestination: StateFlow<Route?> =
        combine(
            tokenManager.tokenState,
            appDataStore.onboardingCompleted
        ) { tokenState, onboardingCompleted ->
            when (tokenState) {
                is TokenState.Loading -> null
                is TokenState.Empty -> Route.Login
                is TokenState.Available -> if (onboardingCompleted) Route.HomeRoot else Route.RegisterChild
            }
        }
            .onStart { refreshTokenUseCase() }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    init {
        Log.d(TAG, "INIT")
    }

    private companion object {
        private const val TAG = "MainVM"
    }
}
