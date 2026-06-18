package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetMeasurementsUseCase @Inject constructor(
    private val repository: MeasurementRepository
) {
    operator fun invoke(childId: UUID): Flow<List<Measurement>> {
        return repository.getMeasurementsForChild(childId)
    }
}
