package com.misterio.core.domain.model

import java.time.LocalDate
import java.util.UUID

data class Child(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val isPremature: Boolean,
    val gestationalWeeks: Int?,
    val allergies: List<String>,
    val chronicDiseases: List<String>,
    val pastDiseases: List<String>,
    val specialFeatures: List<String>,
)
