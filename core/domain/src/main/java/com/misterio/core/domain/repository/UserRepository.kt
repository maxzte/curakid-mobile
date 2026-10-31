package com.misterio.core.domain.repository

import com.misterio.core.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateCurrentUser(name: String): Result<User>
    suspend fun deleteCurrentUser(): Result<Unit>
}
