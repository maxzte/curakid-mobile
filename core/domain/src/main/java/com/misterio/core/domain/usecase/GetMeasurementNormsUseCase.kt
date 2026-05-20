package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.MeasurementRepository
import javax.inject.Inject

class GetMeasurementNormsUseCase @Inject constructor(
    private val repository: MeasurementRepository
) {
    suspend operator fun invoke(
        chartType: ChartType,
        gender: Gender,
    ): Result<List<ChartNormPoint>> {
        return repository.getMeasurementNorms(chartType, gender)
    }
}
