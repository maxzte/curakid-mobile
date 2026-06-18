package com.misterio.curakid.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.usecase.GetVaccineEventByIdUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

@HiltViewModel(assistedFactory = VaccineDetailsViewModel.Factory::class)
class VaccineDetailsViewModel @AssistedInject constructor(
    @Assisted("eventId") private val eventId: UUID,
    getVaccineEventByIdUseCase: GetVaccineEventByIdUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("eventId") eventId: UUID): VaccineDetailsViewModel
    }

    val vaccineDetails = getVaccineEventByIdUseCase(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        Log.d(TAG, "INIT")
    }

    private companion object {
        private const val TAG = "VaccineDetailsVM"
    }
}
