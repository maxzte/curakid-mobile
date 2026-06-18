package com.misterio.core.data.mapper

import com.misterio.core.data.util.toLocalDateTime
import com.misterio.core.database.entity.ChartNormEntity
import com.misterio.core.database.entity.MeasurementEntity
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.Measurement
import com.misterio.core.network.dto.MeasurementResponse

fun ChartNormEntity.toDomain() = ChartNormPoint(
    x = x,
    p01 = p01,
    p1 = p1,
    p3 = p3,
    p5 = p5,
    p10 = p10,
    p15 = p15,
    p25 = p25,
    p50 = p50,
    p75 = p75,
    p85 = p85,
    p90 = p90,
    p95 = p95,
    p97 = p97,
    p99 = p99,
    p999 = p999
)

fun ChartNormPoint.toEntity(type: ChartType, gender: Gender) = ChartNormEntity(
    chartType = type.name,
    gender = gender.name,
    x = x,
    p01 = p01,
    p1 = p1,
    p3 = p3,
    p5 = p5,
    p10 = p10,
    p15 = p15,
    p25 = p25,
    p50 = p50,
    p75 = p75,
    p85 = p85,
    p90 = p90,
    p95 = p95,
    p97 = p97,
    p99 = p99,
    p999 = p999
)

fun MeasurementEntity.toDomain() = Measurement(
    id = id,
    childId = childId,
    date = measurementDate,
    aiReportId = aiReportId,

    weight = weightKg,
    height = heightCm,
    headCirc = headCircCm,

    wfaZscore = wfaZscore,
    hfaZscore = hfaZscore,
    wfhZscore = wfhZscore,
    bmiZscore = bmiZscore,
    hcfaZscore = hcfaZscore,

    wfaPercentile = wfaPercentile,
    hfaPercentile = hfaPercentile,
    wfhPercentile = wfhPercentile,
    bmiPercentile = bmiPercentile,
    hcfaPercentile = hcfaPercentile,

    isSynced = isSynced,
)

fun MeasurementResponse.toEntity() = MeasurementEntity(
    id = id,
    childId = childId,
    measurementDate = measurementDate.toLocalDateTime(),
    aiReportId = aiReportId,

    weightKg = weightKg,
    heightCm = heightCm,
    headCircCm = headCircCm,

    wfaZscore = wfaZscore,
    hfaZscore = hfaZscore,
    wfhZscore = wfhZscore,
    bmiZscore = bmiZscore,
    hcfaZscore = hcfaZscore,

    wfaPercentile = wfaPercentile,
    hfaPercentile = hfaPercentile,
    wfhPercentile = wfhPercentile,
    bmiPercentile = bmiPercentile,
    hcfaPercentile = hcfaPercentile,

    isSynced = true,
)
