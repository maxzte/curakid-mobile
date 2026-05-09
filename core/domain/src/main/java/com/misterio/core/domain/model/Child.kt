package com.misterio.core.domain.model

import java.time.LocalDate

data class Child(
    val id: String,
    val name: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val isPremature: Boolean = false,
    val allergies: List<String> = emptyList(),
    val chronicDiseases: List<String> = emptyList(),
    val pastDiseases: List<String> = emptyList(),
    val specialFeatures: List<String> = emptyList(),
)
