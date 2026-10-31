package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.AuthState
import com.misterio.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> = authRepository.getAuthState()
}
