package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.repository.DevelopmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLifePeriodsUseCase @Inject constructor(
    private val repository: DevelopmentRepository
) {
    operator fun invoke(): Flow<List<LifePeriod>> {
        return repository.getLifePeriods()
    }
}
