package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.ChildRepository
import java.util.UUID
import javax.inject.Inject

class SelectChildUseCase @Inject constructor(
    private val repository: ChildRepository,
) {
    suspend operator fun invoke(childId: UUID) = repository.setSelectedChildId(childId)
}