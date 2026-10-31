package com.misterio.curakid.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.User
import com.misterio.core.domain.usecase.AddChildUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetChildrenUseCase
import com.misterio.core.domain.usecase.GetCurrentUserUseCase
import com.misterio.core.domain.usecase.LogoutUseCase
import com.misterio.core.domain.usecase.SelectChildUseCase
import com.misterio.core.domain.usecase.SyncChildrenUseCase
import com.misterio.core.domain.usecase.UpdateChildUseCase
import com.misterio.core.domain.usecase.UpdateCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val syncChildrenUseCase: SyncChildrenUseCase,
    getChildrenUseCase: GetChildrenUseCase,
    private val selectChildUseCase: SelectChildUseCase,
    private val addChildUseCase: AddChildUseCase,
    private val updateChildUseCase: UpdateChildUseCase,
    getChildUseCase: GetChildUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateCurrentUserUseCase: UpdateCurrentUserUseCase,
) : ViewModel() {

    init {
        Log.i("ProfileVM", "INIT")
        viewModelScope.launch {
            syncChildrenUseCase(false)
            loadUser()
        }
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Initial)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    sealed interface UiEvent {
        object NavigateBack : UiEvent
    }

    val children: StateFlow<List<Child>> = getChildrenUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedChild = getChildUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private fun loadUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().onSuccess { user ->
                _currentUser.value = user
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            updateCurrentUserUseCase(name)
                .onSuccess { updatedUser ->
                    _currentUser.value = updatedUser
                    _uiState.value = UiState.Success("Profile updated")
                    delay(500.milliseconds)
                    _uiState.value = UiState.Initial
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Failed to update profile", error)
                    _uiState.value = UiState.Error(domainError)
                }
        }
    }

    fun onChildSelected(id: UUID) {
        viewModelScope.launch {
            selectChildUseCase(id)
        }
    }

    fun addChild(
        name: String,
        birthDate: Long,
        gender: Gender,
        isPremature: Boolean = false,
        gestationalWeeks: Int? = null,
        allergies: List<String> = emptyList(),
        chronicDiseases: List<String> = emptyList(),
        pastDiseases: List<String> = emptyList(),
        specialFeatures: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            addChildUseCase(
                name,
                birthDate,
                gender,
                isPremature,
                gestationalWeeks,
                allergies,
                chronicDiseases,
                pastDiseases,
                specialFeatures
            )
                .onSuccess {
                    _uiEvent.send(UiEvent.NavigateBack)
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Failed to add child", error)
                    _uiState.value = UiState.Error(domainError)
                }
        }
    }

    fun updateChild(child: Child) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            updateChildUseCase(child)
                .onSuccess {
                    _uiState.value = UiState.Success("Child updated")
                    delay(500.milliseconds)
                    _uiState.value = UiState.Initial
                }
                .onFailure { error ->
                    val domainError = (error as? DomainError) ?: DomainError.UnknownError(error.message ?: "Failed to update child", error)
                    _uiState.value = UiState.Error(domainError)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
