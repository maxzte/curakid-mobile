package com.misterio.core.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.UUID
import javax.inject.Inject

class SyncHomeDataUseCase @Inject constructor(
    private val syncChildrenUseCase: SyncChildrenUseCase,
    private val syncEventsUseCase: SyncEventsUseCase,
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase,
) {
    suspend operator fun invoke(childId: UUID?, force: Boolean = false): Result<Unit> {
        val childrenResult = syncChildrenUseCase(force)
        if (childId != null) {
            return coroutineScope {
                val eventsDeferred = async { syncEventsUseCase(childId) }
                val reportsDeferred = async { syncChildAiReportsUseCase(childId) }

                val eventsResult = eventsDeferred.await()
                val reportsResult = reportsDeferred.await()

                when {
                    childrenResult.isFailure -> childrenResult
                    eventsResult.isFailure -> eventsResult
                    reportsResult.isFailure -> reportsResult
                    else -> Result.success(Unit)
                }
            }
        }
        return childrenResult
    }
}
