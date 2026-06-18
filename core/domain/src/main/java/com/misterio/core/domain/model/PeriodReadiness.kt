package com.misterio.core.domain.model

data class PeriodReadiness(
    val isReady: Boolean,
    val completionPercent: Int,
    val requiredPercent: Int,
    val domainStatus: List<DomainReadiness>,
    val reasons: List<String>,
    val canTransitionEarly: Boolean
)

data class DomainReadiness(
    val domain: String,
    val completionPercent: Int,
    val isReady: Boolean,
    val requiredPercent: Int
)
