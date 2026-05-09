package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

data class ChildDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("isPremature") val isPremature: Boolean = false,
    @SerializedName("allergies") val allergies: List<String> = emptyList(),
    @SerializedName("chronicDiseases") val chronicDiseases: List<String> = emptyList(),
    @SerializedName("pastDiseases") val pastDiseases: List<String> = emptyList(),
    @SerializedName("specialFeatures") val specialFeatures: List<String> = emptyList(),
)
