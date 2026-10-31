package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.SecurityRepository
import javax.inject.Inject

class VerifyPasscodeUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    suspend operator fun invoke(entered: String): Boolean = repository.verifyPasscode(entered)
}
