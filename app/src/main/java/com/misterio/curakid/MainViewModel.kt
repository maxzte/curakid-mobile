package com.misterio.curakid

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.usecase.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val childRepository: ChildRepository,
) : ViewModel() {
    private val _startDestination = MutableStateFlow<NavKey?>(null)
    val startDestination: StateFlow<NavKey?> = _startDestination.asStateFlow()

    init {
        Log.i(TAG, "INIT")
        viewModelScope.launch {
            val initialIsLoggedIn = isLoggedInUseCase().first()
            if (initialIsLoggedIn) {
                childRepository.syncChildren()
            }
            val initialChildren = childRepository.getChildren().first()
            Log.i(TAG, "initialIsLoggedIn:$initialIsLoggedIn")
            Log.i(TAG, "initialChildren:$initialChildren")

            _startDestination.value = when {
                !initialIsLoggedIn -> Route.Login
                initialChildren.isEmpty() -> Route.RegisterChild
                else -> Route.HomeRoot
            }

            combine(
                isLoggedInUseCase(),
                childRepository.getChildren()
            ) { loggedIn, children ->
                when {
                    !loggedIn -> Route.Login
                    children.isEmpty() -> Route.RegisterChild
                    else -> Route.HomeRoot
                }
            }.distinctUntilChanged()
                .drop(1)
                .collect { destination ->
                    Log.i(TAG, "destination:$destination")
                    _startDestination.value = destination
                }
        }
    }

    private companion object {
        private const val TAG = "MainVM"
    }

}
