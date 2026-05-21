package com.misterio.core.domain.repository

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun oAuthLogin(idToken: String): Result<Unit>

    suspend fun register(email: String, password: String, name: String): Result<String>

    suspend fun verifyEmail(email: String, code: String): Result<Unit>

    suspend fun forgotPassword(email: String): Result<String>

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String>

    suspend fun refresh()

    suspend fun logout(): Result<Unit>

}
