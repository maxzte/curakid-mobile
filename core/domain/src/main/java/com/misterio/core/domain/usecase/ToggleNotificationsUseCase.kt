package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.NotificationRepository
import javax.inject.Inject

class ToggleNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> {
        return repository.setNotificationsEnabled(enabled)
    }
}
