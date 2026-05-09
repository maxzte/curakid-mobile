package com.misterio.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetVaccinesUseCase
import com.misterio.core.domain.usecase.SyncVaccinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    getChildUseCase: GetChildUseCase,
    getVaccinesUseCase: GetVaccinesUseCase,
    private val syncVaccinesUseCase: SyncVaccinesUseCase,
) : ViewModel() {

    val selectedChild = getChildUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val vaccines = selectedChild.flatMapLatest { child ->
        if (child != null) {
            viewModelScope.launch {
                syncVaccinesUseCase(child.id)
            }
            getVaccinesUseCase(child.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        Log.i("HomeVM", "INIT")
    }

}
