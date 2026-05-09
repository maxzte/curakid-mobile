package com.misterio.core.domain.model

data class Milestone(
    val id: String,
    val text: String,
    val domain: MilestoneDomain,
    val ageInMonths: Int,
    val isAchieved: Boolean
)

enum class MilestoneDomain {
    GROSS_MOTOR,
    FINE_MOTOR,
    LANGUAGE,
    COGNITIVE,
    SOCIAL
}
