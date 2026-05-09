package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Child
import com.misterio.core.domain.repository.ChildRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetChildUseCase @Inject constructor(
    private val repository: ChildRepository,
) {
    operator fun invoke(): Flow<Child?> {
        return combine(
            repository.getSelectedChildId(),
            repository.getChildren()
        ) { childId, children ->
            children.find { it.id == childId } ?: children.firstOrNull()
        }
    }
}
