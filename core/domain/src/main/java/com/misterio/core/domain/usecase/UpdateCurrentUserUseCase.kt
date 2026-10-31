package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.User
import com.misterio.core.domain.repository.UserRepository
import javax.inject.Inject

class UpdateCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): Result<User> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be blank"))
        }
        return userRepository.updateCurrentUser(name)
    }
}
