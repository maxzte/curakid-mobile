@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.usecase.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val childRepository: ChildRepository,
    private val connectivityRepository: ConnectivityRepository,
) : ViewModel() {
    private val _startDestination = MutableStateFlow<Route?>(null)
    val startDestination = _startDestination.asStateFlow()

    val isConnected = connectivityRepository.isConnected

    init {
        Log.i(TAG, "INIT: Starting reactive navigation")

        // 1. Reactive Start Destination
        isLoggedInUseCase()
            .flatMapLatest { loggedIn ->
                if (!loggedIn) {
                    flowOf(Route.Login)
                } else {
                    childRepository.getChildren().map { kids ->
                        if (kids.isEmpty()) Route.RegisterChild else Route.HomeRoot
                    }
                }
            }
            .distinctUntilChanged()
            .onEach { route ->
                Log.i(TAG, "Start destination updated: $route")
                _startDestination.value = route
            }
            .launchIn(viewModelScope)

        // 2. Initial Sync Side-Effect (Triggered on Login)
        isLoggedInUseCase()
            .distinctUntilChanged()
            .onEach { loggedIn ->
                if (loggedIn) {
                    Log.i(TAG, "Sync trigger: User is logged in, performing sync")
                    viewModelScope.launch {
                        try {
                            withTimeout(5000) {
                                childRepository.syncChildren()
                            }
                            Log.i(TAG, "Sync trigger: Finished")
                        } catch (e: Exception) {
                            Log.e(TAG, "Sync trigger: Failed or Timed out", e)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }


    private companion object {
        private const val TAG = "MainVM"
    }

}
