package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): Result<Unit> {
        if (email.isBlank() || code.isBlank()) {
            return Result.failure(Exception("Email and code are required"))
        }
        return repository.verifyEmail(email, code)
    }
}
