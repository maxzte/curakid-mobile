package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<Unit> {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            return Result.failure(Exception("All fields are required"))
        }
        return repository.register(email, password, name)
    }
}
