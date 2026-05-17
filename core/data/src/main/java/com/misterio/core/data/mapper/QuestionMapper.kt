package com.misterio.core.data.mapper

import com.misterio.core.domain.model.Option
import com.misterio.core.domain.model.Question
import com.misterio.core.network.dto.OptionResponse
import com.misterio.core.network.dto.QuestionResponse

internal fun QuestionResponse.toDomain() = Question(
    id = id,
    text = text,
    domain = domain,
    weight = weight,
    options = options.map { it.toDomain() }
)

internal fun OptionResponse.toDomain() = Option(
    id = id,
    text = text,
    value = value,
    displayOrder = displayOrder,
)
