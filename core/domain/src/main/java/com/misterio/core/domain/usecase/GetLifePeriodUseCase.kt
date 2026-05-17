package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.repository.DevelopmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLifePeriodUseCase @Inject constructor(
    private val repository: DevelopmentRepository
) {
    operator fun invoke(code: String): Flow<LifePeriod?> {
        return repository.getLifePeriod(code)
    }
}
