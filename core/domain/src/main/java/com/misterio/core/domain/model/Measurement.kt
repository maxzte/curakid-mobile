package com.misterio.core.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Measurement(
    val id: UUID,
    val childId: UUID,
    val date: LocalDateTime,
    val aiReportId: UUID?,

    val weight: Double?,
    val height: Double?,
    val headCirc: Double?,

    val wfaZscore: Double?,
    val hfaZscore: Double?,
    val wfhZscore: Double?,
    val bmiZscore: Double?,
    val hcfaZscore: Double?,

    val wfaPercentile: Double?,
    val hfaPercentile: Double?,
    val wfhPercentile: Double?,
    val bmiPercentile: Double?,
    val hcfaPercentile: Double?,

    val isSynced: Boolean = true,
)
