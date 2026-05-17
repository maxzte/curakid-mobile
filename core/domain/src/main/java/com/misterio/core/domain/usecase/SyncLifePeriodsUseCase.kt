package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.DevelopmentRepository
import javax.inject.Inject

class SyncLifePeriodsUseCase @Inject constructor(
    private val repository: DevelopmentRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncLifePeriods()
    }
}
