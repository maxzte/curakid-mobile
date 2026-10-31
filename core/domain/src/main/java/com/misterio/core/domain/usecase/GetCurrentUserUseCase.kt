package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.User
import com.misterio.core.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return userRepository.getCurrentUser()
    }
}
