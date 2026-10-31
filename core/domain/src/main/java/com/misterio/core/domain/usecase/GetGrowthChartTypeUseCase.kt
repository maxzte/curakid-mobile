package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.ChartType
import javax.inject.Inject

enum class GrowthChartCategory {
    WEIGHT_FOR_AGE,
    HEIGHT_FOR_AGE,
    WEIGHT_FOR_HEIGHT,
    BMI,
    HEAD_CIRCUMFERENCE
}

class GetGrowthChartTypeUseCase @Inject constructor() {
    operator fun invoke(category: GrowthChartCategory, ageInMonths: Double = 0.0): ChartType {
        return when (category) {
            GrowthChartCategory.WEIGHT_FOR_AGE -> ChartType.WFA
            GrowthChartCategory.HEIGHT_FOR_AGE -> ChartType.HFA
            GrowthChartCategory.WEIGHT_FOR_HEIGHT -> if (ageInMonths < 24.0) ChartType.WFL else ChartType.WFH
            GrowthChartCategory.BMI -> ChartType.BMI
            GrowthChartCategory.HEAD_CIRCUMFERENCE -> ChartType.HCFA
        }
    }
}
