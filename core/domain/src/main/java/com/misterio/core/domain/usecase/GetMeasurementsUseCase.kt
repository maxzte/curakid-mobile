package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMeasurementsUseCase @Inject constructor(
    private val repository: MeasurementRepository
) {
    operator fun invoke(childId: String): Flow<List<Measurement>> {
        return repository.getMeasurementsForChild(childId)
    }
}
