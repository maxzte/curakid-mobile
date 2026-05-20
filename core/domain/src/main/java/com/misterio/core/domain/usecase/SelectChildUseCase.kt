package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.ChildRepository
import javax.inject.Inject

class SelectChildUseCase @Inject constructor(
    private val repository: ChildRepository,
) {
    suspend operator fun invoke(childId: String) = repository.setSelectedChildId(childId)
}