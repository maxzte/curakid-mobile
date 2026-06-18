package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.PeriodReadiness
import com.misterio.core.domain.repository.AssessmentRepository
import java.util.UUID
import javax.inject.Inject

class GetPeriodReadinessUseCase @Inject constructor(
    private val repository: AssessmentRepository
) {
    suspend operator fun invoke(
        childId: UUID,
        periodCode: String
    ): Result<PeriodReadiness> {
        return repository.getPeriodReadiness(childId, periodCode)
    }
}
