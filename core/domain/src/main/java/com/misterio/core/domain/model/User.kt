package com.misterio.core.domain.model

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val name: String,
    val role: String?,
    val onboardingCompleted: Boolean,
    val active: Boolean
)
