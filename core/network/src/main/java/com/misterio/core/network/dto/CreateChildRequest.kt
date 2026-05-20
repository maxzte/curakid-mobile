package com.misterio.core.network.dto


data class CreateChildRequest(
    val name: String,
    val birthDate: String,//LocalDate
    val gender: String,
    val isPremature: Boolean,
    val allergies: List<String>?,
    val chronicDiseases: List<String>?,
    val pastDiseases: List<String>?,
    val specialFeatures: List<String>?,
)
