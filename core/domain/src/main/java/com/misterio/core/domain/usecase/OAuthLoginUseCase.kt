package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AuthRepository
import javax.inject.Inject

class OAuthLoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String) = repository.oAuthLogin(idToken)
}
