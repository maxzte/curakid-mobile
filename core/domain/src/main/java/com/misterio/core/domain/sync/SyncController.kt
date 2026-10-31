package com.misterio.core.domain.sync

import kotlinx.coroutines.flow.Flow

interface SyncController {
    val isSyncing: Flow<Boolean>
    fun triggerSync()
}
