package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import javax.inject.Inject

class DeleteProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.deleteAccount()
    }
}
