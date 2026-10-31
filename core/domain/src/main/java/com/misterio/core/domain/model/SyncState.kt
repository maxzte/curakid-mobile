package com.misterio.core.domain.model

sealed interface SyncState {
    object Synced : SyncState
    object PendingCreate : SyncState
    object PendingUpdate : SyncState
    object PendingDelete : SyncState
}
