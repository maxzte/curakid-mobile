package com.misterio.core.domain.util

interface FcmRegistrar {
    suspend fun register(): Result<Unit>
}
