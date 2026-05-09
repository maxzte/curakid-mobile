package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.repository.AssessmentRepository
import javax.inject.Inject

class GetLifePeriodUseCase @Inject constructor(
    private val repository: AssessmentRepository
) {
    suspend operator fun invoke(code: String): Result<LifePeriod> {
        return repository.getLifePeriod(code)
    }
}
