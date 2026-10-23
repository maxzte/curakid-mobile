package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.MeasurementRepository
import java.util.UUID
import javax.inject.Inject

class SyncMeasurementsUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository
) {
    suspend operator fun invoke(childId: UUID): Result<Unit> {
        return measurementRepository.syncMeasurementsForChild(childId)
    }
}
