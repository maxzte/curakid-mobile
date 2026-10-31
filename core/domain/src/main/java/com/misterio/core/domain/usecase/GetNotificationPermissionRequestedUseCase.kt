package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationPermissionRequestedUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isPermissionRequested()
    }
}
