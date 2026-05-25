@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.util.safeApiCall
import androidx.room.withTransaction
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.core.database.AppDatabase
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.network.api.ChildApi
import com.misterio.core.network.dto.ChildResponse
import com.misterio.core.network.dto.CreateChildRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class ChildRepositoryImpl @Inject constructor(
    private val childApi: ChildApi,
    private val childDao: ChildDao,
    private val appDataStore: AppDataStore,
    private val database: AppDatabase,
) : ChildRepository {

    override fun getChildren(): Flow<List<Child>> {
        return childDao.getAllChildren().map { entities ->
            Log.d(TAG, "getChildren from DAO: ${entities.size}")
            entities.map { it.toDomain() }
        }
    }

    override fun getChild(childId: String): Flow<Child?> {
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
            childDao.insertChildren(body.map { it.toEntity() })
            appDataStore.setLastChildrenSync(System.currentTimeMillis())
            Unit
        }
    }

    override suspend fun addChild(
        name: String,
        birthDate: Long,
        gender: Gender,
        isPremature: Boolean,
        allergies: List<String>,
        chronicDiseases: List<String>,
        pastDiseases: List<String>,
        specialFeatures: List<String>
    ): Result<String> {
        val tempId = UUID.randomUUID().toString()
        val birthDateLocal = Instant.ofEpochMilli(birthDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val tempEntity = ChildEntity(
            id = tempId,
            name = name,
            birthDate = birthDateLocal,
            gender = gender.name,
            isPremature = isPremature,
            allergies = allergies,
            chronicDiseases = chronicDiseases,
            pastDiseases = pastDiseases,
            specialFeatures = specialFeatures,
        )

        // 1. Optimistic insert
        childDao.insertChild(tempEntity)

        val dto = CreateChildRequest(
            name = name,
            birthDate = birthDateLocal.toString(),
            gender = gender.name,
            isPremature = isPremature,
            allergies = allergies,
            chronicDiseases = chronicDiseases,
            pastDiseases = pastDiseases,
            specialFeatures = specialFeatures,
        )

        return safeApiCall { childApi.createChild(dto) }.map { createdChild ->
            appDataStore.setOnboardingCompleted(true)
            // 2. Replace with real ID and data atomically
            database.withTransaction {
                childDao.deleteChild(tempEntity)
                childDao.insertChild(createdChild.toEntity())
            }
            createdChild.id
        }
    }

    override suspend fun updateChild(child: Child): Result<Unit> {
        val originalEntity = childDao.getChildById(child.id)

        // 1. Optimistic update
        childDao.insertChild(child.toEntity())

        val dto = ChildResponse(
            id = child.id,
            name = child.name,
            birthDate = child.birthDate.toString(),
            gender = child.gender.name,
            isPremature = child.isPremature,
            allergies = child.allergies,
            chronicDiseases = child.chronicDiseases,
            pastDiseases = child.pastDiseases,
            specialFeatures = child.specialFeatures,
        )

        return safeApiCall { childApi.updateChild(child.id, dto) }
            .onSuccess { body ->
                childDao.insertChild(body.toEntity())
            }
            .onFailure {
                // Rollback on failure
                originalEntity?.let { childDao.insertChild(it) }
            }
            .map { Unit }
    }

    override fun getSelectedChild(): Flow<Child?> {
        return appDataStore.selectedChildId.flatMapLatest { id ->
            id?.let {
                childDao.getChildByIdFlow(it).map { entity -> entity?.toDomain() }
            } ?: childDao.getAllChildren().map { entities -> entities.firstOrNull()?.toDomain() }
        }
    }

    override suspend fun setSelectedChildId(id: String?) {
        appDataStore.saveSelectedChildId(id)
    }

    private fun ChildEntity.toDomain() = Child(
        id = id,
        name = name,
        birthDate = birthDate,
        gender = Gender.valueOf(gender),
        isPremature = isPremature,
        allergies = allergies,
        chronicDiseases = chronicDiseases,
        pastDiseases = pastDiseases,
        specialFeatures = specialFeatures,
    )

    private fun ChildResponse.toEntity() = ChildEntity(
        id = id,
        name = name,
        birthDate = LocalDate.parse(birthDate),
        gender = gender,
        isPremature = isPremature,
        allergies = allergies,
        chronicDiseases = chronicDiseases,
        pastDiseases = pastDiseases,
        specialFeatures = specialFeatures,
    )

    private fun Child.toEntity() = ChildEntity(
        id = id,
        name = name,
        birthDate = birthDate,
        gender = gender.name,
        isPremature = isPremature,
        allergies = allergies,
        chronicDiseases = chronicDiseases,
        pastDiseases = pastDiseases,
        specialFeatures = specialFeatures,
    )

    private companion object {
        private const val TAG = "ChildRepository"
        private const val SYNC_INTERVAL_MS = 30 * 60 * 1000L
    }
}
