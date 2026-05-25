package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

data class QuestionResponse(
    @SerializedName("questionId") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("options") val options: List<OptionResponse>,
)

data class OptionResponse(
    @SerializedName("optionId") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Double,
    @SerializedName("displayOrder") val displayOrder: Int,
)
