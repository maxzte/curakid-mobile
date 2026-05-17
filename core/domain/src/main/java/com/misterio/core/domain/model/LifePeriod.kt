package com.misterio.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LifePeriod(
    val id: String,
    val code: String,
    val label: String,
    val about: String,
    val minMonths: Int,
    val maxMonths: Int,
    val questions: List<Question>
)
