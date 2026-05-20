package com.misterio.core.domain.model

import java.time.LocalDateTime

data class Measurement(
    val id: String,
    val childId: String,
    val date: LocalDateTime,
    val aiReportId: String?,

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
