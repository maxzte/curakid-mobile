package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class CreateChildRequest(
    val id: UUID,
    val name: String,
    val birthDate: String,//LocalDate
    val gender: String,
    val isPremature: Boolean,
    val gestationalWeeks: Int?,
    val allergies: List<String>?,
    val chronicDiseases: List<String>?,
    val pastDiseases: List<String>?,
    val specialFeatures: List<String>?,
)

data class UpdateChildRequest(
    val name: String?,
    val birthDate: String?,
    val gender: String?,
    val gestationalWeeks: Int?,
    val allergies: List<String>?,
    val chronicDiseases: List<String>?,
    val pastDiseases: List<String>?,
    val specialFeatures: List<String>?,
)

data class ChildResponse(
    @SerializedName("id") val id: UUID,
    @SerializedName("name") val name: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("isPremature") val isPremature: Boolean,
    val gestationalWeeks: Int?,
    @SerializedName("allergies") val allergies: List<String>,
    @SerializedName("chronicDiseases") val chronicDiseases: List<String>,
    @SerializedName("pastDiseases") val pastDiseases: List<String>,
    @SerializedName("specialFeatures") val specialFeatures: List<String>,
)
