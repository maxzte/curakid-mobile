package com.misterio.core.domain.sync

sealed interface SyncResult {
    object Success : SyncResult
    data class TransientFailure(val error: Throwable) : SyncResult
    data class PermanentFailure(val error: Throwable) : SyncResult
}

interface Syncable {
    suspend fun syncPending(): SyncResult
}
