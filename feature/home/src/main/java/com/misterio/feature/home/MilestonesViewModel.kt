package com.misterio.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Milestone
import com.misterio.core.domain.usecase.GetMilestonesUseCase
import com.misterio.core.domain.usecase.SaveAssessmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MilestonesViewModel @Inject constructor(
    private val getMilestonesUseCase: GetMilestonesUseCase,
    private val saveAssessmentUseCase: SaveAssessmentUseCase
) : ViewModel() {

    fun milestones(childId: String): StateFlow<List<Milestone>> =
        getMilestonesUseCase(childId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleMilestone(childId: String, milestone: Milestone) {
        viewModelScope.launch {
            val updatedMilestone = milestone.copy(isAchieved = !milestone.isAchieved)
            // In a real app, we might want to update the whole list or a specific item
            // For now, we just call save with the updated one
            saveAssessmentUseCase(childId, listOf(updatedMilestone))
        }
    }
}
