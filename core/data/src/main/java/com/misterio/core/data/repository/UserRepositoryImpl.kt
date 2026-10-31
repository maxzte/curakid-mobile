package com.misterio.core.data.repository

import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.domain.model.User
import com.misterio.core.domain.repository.UserRepository
import com.misterio.core.network.api.UserApi
import com.misterio.core.network.dto.UpdateUserRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getCurrentUser(): Result<User> {
        return safeApiCall { userApi.getCurrentUser() }.map { it.toDomain() }
    }

    override suspend fun updateCurrentUser(name: String): Result<User> {
        return safeApiCall { userApi.updateCurrentUser(UpdateUserRequest(name)) }.map { it.toDomain() }
    }

    override suspend fun deleteCurrentUser(): Result<Unit> {
        return safeApiCall { userApi.deleteCurrentUser() }.map { }
    }
}
