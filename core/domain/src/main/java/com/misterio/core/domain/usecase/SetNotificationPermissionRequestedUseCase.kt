package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.NotificationRepository
import javax.inject.Inject

class SetNotificationPermissionRequestedUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(requested: Boolean = true) {
        repository.setPermissionRequested(requested)
    }
}
