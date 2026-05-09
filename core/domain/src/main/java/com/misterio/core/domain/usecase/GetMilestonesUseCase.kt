package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Milestone
import com.misterio.core.domain.repository.AssessmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMilestonesUseCase @Inject constructor(
    private val repository: AssessmentRepository
) {
    operator fun invoke(childId: String): Flow<List<Milestone>> {
        return repository.getMilestones(childId)
    }
}
