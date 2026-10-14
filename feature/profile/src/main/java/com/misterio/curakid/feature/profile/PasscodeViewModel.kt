package com.misterio.curakid.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.curakid.core.common.PasscodeMode
import com.misterio.curakid.core.common.SecurityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PasscodeUiState(
    val passcode: String = "",
    val confirmPasscode: String = "",
    val isConfirming: Boolean = false,
    val errorMessage: String? = null,
    val isLockedOut: Boolean = false,
    val lockoutSecondsRemaining: Int = 0,
    val failedAttempts: Int = 0
)

sealed interface PasscodeEvent {
    data object Success : PasscodeEvent
}

@HiltViewModel(assistedFactory = PasscodeViewModel.Factory::class)
class PasscodeViewModel(
    private val mode: PasscodeMode,
    private val securityManager: SecurityManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    @AssistedInject
    constructor(
        @Assisted mode: PasscodeMode,
        securityManager: SecurityManager
    ) : this(mode, securityManager, Dispatchers.IO)

    @AssistedFactory
    interface Factory {
        fun create(mode: PasscodeMode): PasscodeViewModel
    }

    private val _uiState = MutableStateFlow(PasscodeUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<PasscodeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onDigitClick(digit: String) {
        if (_uiState.value.isLockedOut) return
        if (_uiState.value.passcode.length >= 4) return

        val newPasscode = _uiState.value.passcode + digit
        _uiState.update { it.copy(passcode = newPasscode, errorMessage = null) }

        if (newPasscode.length == 4) {
            handleCompletePasscode(newPasscode)
        }
    }

    fun onDeleteClick() {
        if (_uiState.value.isLockedOut) return
        _uiState.update {
            if (it.passcode.isNotEmpty()) it.copy(passcode = it.passcode.dropLast(1), errorMessage = null) else it
        }
    }

    private fun handleCompletePasscode(entered: String) {
        viewModelScope.launch {
            when (mode) {
                PasscodeMode.SETUP -> {
                    if (!_uiState.value.isConfirming) {
                        _uiState.update { it.copy(confirmPasscode = entered, passcode = "", isConfirming = true) }
                    } else {
                        if (entered == _uiState.value.confirmPasscode) {
                            withContext(ioDispatcher) {
                                securityManager.savePasscode(entered)
                            }
                            _events.send(PasscodeEvent.Success)
                        } else {
                            _uiState.update { it.copy(errorMessage = "Коди не збігаються") }
                            delay(1000)
                            _uiState.update { it.copy(passcode = "", confirmPasscode = "", isConfirming = false, errorMessage = null) }
                        }
                    }
                }
                PasscodeMode.UNLOCK -> {
                    val isValid = withContext(ioDispatcher) {
                        securityManager.verifyPasscode(entered)
                    }
                    if (isValid) {
                        _uiState.update { it.copy(failedAttempts = 0) }
                        _events.send(PasscodeEvent.Success)
                    } else {
                        val newAttempts = _uiState.value.failedAttempts + 1
                        if (newAttempts >= 5) {
                            triggerLockout(30)
                        } else {
                            _uiState.update { it.copy(failedAttempts = newAttempts, errorMessage = "Неправильний ПІН-код") }
                            delay(1000)
                            _uiState.update { it.copy(passcode = "", errorMessage = null) }
                        }
                    }
                }
                PasscodeMode.DISABLE -> {
                    val isValid = withContext(ioDispatcher) {
                        securityManager.verifyPasscode(entered)
                    }
                    if (isValid) {
                        withContext(ioDispatcher) {
                            securityManager.clearPasscode()
                        }
                        _events.send(PasscodeEvent.Success)
                    } else {
                        _uiState.update { it.copy(errorMessage = "Неправильний ПІН-код") }
                        delay(1000)
                        _uiState.update { it.copy(passcode = "", errorMessage = null) }
                    }
                }
            }
        }
    }

    private fun triggerLockout(seconds: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLockedOut = true,
                    lockoutSecondsRemaining = seconds,
                    passcode = "",
                    errorMessage = "Забагато спроб. Спробуйте через $seconds с"
                )
            }
            for (sec in seconds downTo 1) {
                delay(1000)
                _uiState.update { it.copy(lockoutSecondsRemaining = sec - 1) }
            }
            _uiState.update {
                it.copy(
                    isLockedOut = false,
                    failedAttempts = 0,
                    errorMessage = null
                )
            }
        }
    }
}
