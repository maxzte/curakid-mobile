package com.misterio.core.data.mapper

import com.misterio.core.domain.model.User
import com.misterio.core.network.dto.UserResponse

internal fun UserResponse.toDomain() = User(
    id = id,
    email = email,
    name = name,
    role = role,
    onboardingCompleted = onboardingCompleted,
    active = active
)
