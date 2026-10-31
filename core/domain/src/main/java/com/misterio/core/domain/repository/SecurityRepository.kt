package com.misterio.core.domain.repository

interface SecurityRepository {
    suspend fun savePasscode(passcode: String)
    suspend fun verifyPasscode(entered: String): Boolean
    suspend fun clearPasscode()
}
