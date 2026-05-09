package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.MeasurementRepository
import javax.inject.Inject

class AddMeasurementUseCase @Inject constructor(
    private val repository: MeasurementRepository
) {
    suspend operator fun invoke(
        childId: String,
        weight: Double?,
        height: Double?,
        headCirc: Double?,
    ): Result<Unit> {
        val hasValidMeasurement = listOf(weight, height, headCirc)
            .any { it != null && it > 0.0 }

        if (!hasValidMeasurement) {
            return Result.failure(IllegalArgumentException("At least one measurement must be greater than zero"))
        }

        return repository.addMeasurement(childId, weight, height, headCirc)
    }
}
