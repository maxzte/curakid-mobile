package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.SecurityRepository
import javax.inject.Inject

class ClearPasscodeUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    suspend operator fun invoke() = repository.clearPasscode()
}
