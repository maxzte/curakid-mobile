package com.misterio.core.data.mapper

import com.misterio.core.database.entity.LifePeriodEntity
import com.misterio.core.domain.model.LifePeriod

fun LifePeriodEntity.toDomain() = LifePeriod(
    id = id,
    code = code,
    label = label,
    about = about,
    minMonths = minMonths,
    maxMonths = maxMonths,
    questions = questions
)
