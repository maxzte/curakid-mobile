package com.misterio.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val text: String,
    val domain: String,
    val weight: Double,
    val options: List<Option>,
)

@Serializable
data class Option(
    val id: String,
    val text: String,
    val value: String,
    val displayOrder: Int,
)
