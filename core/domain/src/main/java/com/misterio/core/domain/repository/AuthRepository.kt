package com.misterio.core.domain.repository

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun register(email: String, password: String, name: String): Result<Unit>

    suspend fun refresh()

    suspend fun logout(): Result<Unit>

}
