package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<String> {
        return repository.forgotPassword(email)
    }
}
