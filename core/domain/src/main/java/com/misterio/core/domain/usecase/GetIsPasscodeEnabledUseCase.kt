package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIsPasscodeEnabledUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = authRepository.isPasscodeEnabled()
}
