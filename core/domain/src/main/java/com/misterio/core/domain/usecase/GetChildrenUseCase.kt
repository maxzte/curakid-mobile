package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Child
import com.misterio.core.domain.repository.ChildRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChildrenUseCase @Inject constructor(
    private val repository: ChildRepository
) {
    operator fun invoke(): Flow<List<Child>> {
        return repository.getChildren()
    }
}
