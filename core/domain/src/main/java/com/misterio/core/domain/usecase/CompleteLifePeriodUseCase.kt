package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.repository.AssessmentRepository
import java.util.UUID
import javax.inject.Inject

class CompleteLifePeriodUseCase @Inject constructor(
    private val repository: AssessmentRepository
) {
    suspend operator fun invoke(
        childId: UUID,
        periodCode: String,
        allowEarlyTransition: Boolean
    ): Result<CompletePeriodResult> {
        return repository.completePeriod(childId, periodCode, allowEarlyTransition)
    }
}
