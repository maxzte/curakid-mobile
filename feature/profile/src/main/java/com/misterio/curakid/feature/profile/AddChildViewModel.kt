package com.misterio.curakid.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.usecase.AddChildUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddChildViewModel @Inject constructor(
    private val addChildUseCase: AddChildUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(AddChildFormState())
    val formState = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun onBirthDateChange(millis: Long) {
        _formState.update { it.copy(birthDateMillis = millis) }
    }

    fun onGenderChange(gender: Gender) {
        _formState.update { it.copy(gender = gender) }
    }

    fun onPrematureChange(isPremature: Boolean) {
        _formState.update { it.copy(isPremature = isPremature) }
    }

    fun onGestationalWeeksChange(weeks: Int) {
        _formState.update { it.copy(gestationalWeeks = weeks) }
    }

    fun onItemsChange(category: MedicalCategory, items: List<String>) {
        _formState.update { state ->
            when (category) {
                MedicalCategory.ALLERGIES -> state.copy(allergies = items)
                MedicalCategory.CHRONIC -> state.copy(chronicDiseases = items)
                MedicalCategory.PAST -> state.copy(pastDiseases = items)
                MedicalCategory.FEATURES -> state.copy(specialFeatures = items)
            }
        }
    }

    fun submit() {
        val state = _formState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            addChildUseCase(
                name = state.name,
                birthDate = state.birthDateMillis,
                gender = state.gender,
                isPremature = state.isPremature,
                gestationalWeeks = if (state.isPremature) state.gestationalWeeks else null,
                allergies = state.allergies,
                chronicDiseases = state.chronicDiseases,
                pastDiseases = state.pastDiseases,
                specialFeatures = state.specialFeatures
            ).onSuccess {
                _uiState.value = UiState.Success(Unit)
                _navigationEvent.send(Unit)
            }.onFailure { error ->
                _uiState.value = UiState.Error(com.misterio.core.domain.model.DomainError.fromThrowable(error))
            }
        }
    }
}
