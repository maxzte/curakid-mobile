package com.misterio.core.data.repository

import com.misterio.core.domain.repository.SecurityRepository
import com.misterio.curakid.core.common.SecurityManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    private val securityManager: SecurityManager
) : SecurityRepository {
    override suspend fun savePasscode(passcode: String) = securityManager.savePasscode(passcode)
    override suspend fun verifyPasscode(entered: String): Boolean = securityManager.verifyPasscode(entered)
    override suspend fun clearPasscode() = securityManager.clearPasscode()
}
