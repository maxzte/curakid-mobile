package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.NotificationRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.registerFcmToken()
    }
}
