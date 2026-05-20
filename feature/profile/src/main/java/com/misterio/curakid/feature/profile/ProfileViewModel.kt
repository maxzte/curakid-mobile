package com.misterio.curakid.feature.profile

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.usecase.AddChildUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetChildrenUseCase
import com.misterio.core.domain.usecase.LogoutUseCase
import com.misterio.core.domain.usecase.SelectChildUseCase
import com.misterio.core.domain.usecase.SyncChildrenUseCase
import com.misterio.core.domain.usecase.UpdateChildUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val syncChildrenUseCase: SyncChildrenUseCase,
    getChildrenUseCase: GetChildrenUseCase,
    private val selectChildUseCase: SelectChildUseCase,
    private val addChildUseCase: AddChildUseCase,
    private val updateChildUseCase: UpdateChildUseCase,
    getChildUseCase: GetChildUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    init {
        Log.i("ProfileVM", "INIT")
        viewModelScope.launch {
            syncChildrenUseCase(false)
        }
    }

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Initial)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

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

    fun onChildSelected(id: String) {
        viewModelScope.launch {
            selectChildUseCase(id)
        }
    }

    fun addChild(
        name: String,
        birthDate: Long,
        gender: Gender,
        isPremature: Boolean = false,
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
                allergies,
                chronicDiseases,
                pastDiseases,
                specialFeatures
            )
                .onSuccess {
                    _uiState.value = UiState.Success("Child added")
                    _uiState.value = UiState.Initial
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to add child")
                }
        }
    }

    fun updateChild(child: Child) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            updateChildUseCase(child)
                .onSuccess {
                    _uiState.value = UiState.Success("Child updated")
                    delay(500)
                    _uiState.value = UiState.Initial
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to update child")
                }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            logoutUseCase()

            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent?.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)

            Runtime.getRuntime().exit(0)
        }
    }
}
