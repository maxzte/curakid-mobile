package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.SecurityRepository
import javax.inject.Inject

class SavePasscodeUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    suspend operator fun invoke(passcode: String) = repository.savePasscode(passcode)
}
