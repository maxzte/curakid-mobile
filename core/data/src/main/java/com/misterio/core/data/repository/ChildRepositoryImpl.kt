@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.util.SyncManager
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.network.api.ChildApi
import com.misterio.curakid.core.common.AppDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildRepositoryImpl @Inject constructor(
    private val childApi: ChildApi,
    private val childDao: ChildDao,
    private val appDataStore: AppDataStore,
    private val syncManager: SyncManager,
) : ChildRepository {

    override fun getChildren(): Flow<List<Child>> {
        return childDao.getAllChildren().map { entities ->
            Log.d(TAG, "getChildren from DAO: ${entities.size}")
            entities.map { it.toDomain() }
        }
    }

    override fun getChild(childId: UUID): Flow<Child?> {
        return childDao.getChildByIdFlow(childId).map {
            Log.d(TAG, "getChild from DAO for $childId: ${it?.id}")
            it?.toDomain()
        }
    }

    override suspend fun syncChildren(force: Boolean): Result<Unit> {
        Log.d(TAG, "SYNC children")
        if (!force) {
            val lastSync = appDataStore.lastChildrenSync.first()
            val elapsed = System.currentTimeMillis() - lastSync
            if (lastSync > 0 && elapsed < SYNC_INTERVAL_MS) {
                Log.d(TAG, "syncChildren: Skipping, last sync was ${elapsed / 1000}s ago")
                return Result.success(Unit)
            }
        }

        return safeApiCall { childApi.getChildren() }.map { body ->
            val remoteEntities = body.map { it.toEntity() }
            childDao.syncWithServer(remoteEntities)
            appDataStore.setLastChildrenSync(System.currentTimeMillis())

            // Also check for any locally added unsynced children to push to backend
            val unsynced = childDao.getUnsyncedChildren()
            if (unsynced.isNotEmpty()) {
                syncManager.scheduleSync()
            }
            Unit
        }
    }

    override suspend fun addChild(
        name: String,
        birthDate: Long,
        gender: Gender,
        isPremature: Boolean,
        gestationalWeeks: Int?,
        allergies: List<String>,
        chronicDiseases: List<String>,
        pastDiseases: List<String>,
        specialFeatures: List<String>
    ): Result<UUID> {
        val id = UUID.randomUUID()
        val birthDateLocal = Instant.ofEpochMilli(birthDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val tempEntity = ChildEntity(
            id = id,
            name = name,
            birthDate = birthDateLocal,
            gender = gender.name,
            isPremature = isPremature,
            gestationalWeeks = gestationalWeeks,
            allergies = allergies,
            chronicDiseases = chronicDiseases,
            pastDiseases = pastDiseases,
            specialFeatures = specialFeatures,
            isSynced = false,
        )

        // 1. Save locally first
        childDao.insertChild(tempEntity)
        appDataStore.setOnboardingCompleted(true)
        syncManager.scheduleSync()

        return Result.success(id)
    }

    override suspend fun updateChild(child: Child): Result<Unit> {
        // 1. Optimistic update
        childDao.insertChild(child.toEntity().copy(isSynced = false))
        syncManager.scheduleSync()

        return Result.success(Unit)
    }

    override fun getSelectedChild(): Flow<Child?> {
        return appDataStore.selectedChildId.flatMapLatest { id ->
            id?.let {
                childDao.getChildByIdFlow(UUID.fromString(it)).map { entity -> entity?.toDomain() }
            } ?: childDao.getAllChildren().map { entities -> entities.firstOrNull()?.toDomain() }
        }
    }

    override suspend fun setSelectedChildId(id: UUID) {
        appDataStore.saveSelectedChildId(id.toString())
    }

    private companion object {
        private const val TAG = "ChildRepository"
        private const val SYNC_INTERVAL_MS = 30 * 60 * 1000L
    }
}
