package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<String> {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            return Result.failure(Exception("All fields are required"))
        }
        if (!email.matches(EMAIL_REGEX)) {
            return Result.failure(Exception("Невірний формат email"))
        }
        return repository.register(email, password, name)
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
    }
}
