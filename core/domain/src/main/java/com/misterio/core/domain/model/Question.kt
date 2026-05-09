package com.misterio.core.domain.model

data class Question(
    val id: String,
    val text: String,
    val domain: String,
    val weight: Double,
    val options: List<Option>
)

data class Option(
    val id: String,
    val text: String,
    val value: String
)
