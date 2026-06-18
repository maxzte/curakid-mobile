package com.misterio.core.domain.model

import com.misterio.core.domain.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Question(
    @Serializable(UUIDSerializer::class) val id: UUID,
    val text: String,
    val domain: String,
    val weight: Double,
    val options: List<Option>,
)

@Serializable
data class Option(
    @Serializable(UUIDSerializer::class) val id: UUID,
    val text: String,
    val value: Double,
    val displayOrder: Int,
)
