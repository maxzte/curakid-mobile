package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AssessmentRepository
import java.util.UUID
import javax.inject.Inject

class SyncChildAssessmentsUseCase @Inject constructor(
    private val repository: AssessmentRepository,
) {
    suspend operator fun invoke(childId: UUID): Result<Unit> {
        return repository.syncChildAssessments(childId)
    }
}
