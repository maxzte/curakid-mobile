package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.ChildRepository
import javax.inject.Inject

class SyncChildrenUseCase @Inject constructor(
    private val repository: ChildRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncChildren()
    }
}
