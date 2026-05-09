package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Milestone
import com.misterio.core.domain.repository.AssessmentRepository
import javax.inject.Inject

class SaveAssessmentUseCase @Inject constructor(
    private val repository: AssessmentRepository
) {
    suspend operator fun invoke(childId: String, milestones: List<Milestone>) {
        repository.saveAssessment(childId, milestones)
    }
}
