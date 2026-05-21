package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String, newPassword: String): Result<String> {
        return repository.resetPassword(email, code, newPassword)
    }
}
