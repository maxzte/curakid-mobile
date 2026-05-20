package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Child
import com.misterio.core.domain.repository.ChildRepository
import javax.inject.Inject

class UpdateChildUseCase @Inject constructor(
    private val repository: ChildRepository,
) {
    suspend operator fun invoke(child: Child) = repository.updateChild(child)
}
