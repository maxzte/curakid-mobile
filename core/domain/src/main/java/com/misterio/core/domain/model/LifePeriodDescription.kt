package com.misterio.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LifePeriodDescription(
    val title: String,
    val subtitle: String,
    val milestones: Milestones,
    val tips: List<String>
)

@Serializable
data class Milestones(
    val movement: String,
    val speech: String,
    val cognition: String
)
