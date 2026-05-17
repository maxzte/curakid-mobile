package com.misterio.core.domain.model

import java.time.LocalDateTime

data class Measurement(
    val id: String,
    val childId: String,
    val weight: Double?,
    val height: Double?,
    val headCirc: Double?,
    val wfaZscore: Double?,
    val hfaZscore: Double?,
    val bmiZscore: Double?,
    val hcfaZscore: Double?,
    val date: LocalDateTime,
    val isSynced: Boolean = true,
    val aiReportId: String?
)
