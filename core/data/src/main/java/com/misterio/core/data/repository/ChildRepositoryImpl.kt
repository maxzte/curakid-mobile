package com.misterio.core.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.misterio.core.common.SettingsManager
import com.misterio.core.database.AppDatabase
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.network.api.ChildApi
import com.misterio.core.network.dto.ChildDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildRepositoryImpl @Inject constructor(
    private val childApi: ChildApi,
    private val childDao: ChildDao,
    private val settingsManager: SettingsManager,
    private val database: AppDatabase,
) : ChildRepository {

    override fun getChildren(): Flow<List<Child>> {
        return childDao.getAllChildren().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChild(childId: String): Flow<Child?> {
        return childDao.getChildByIdFlow(childId).map { it?.toDomain() }
    }

    override suspend fun syncChildren(): Result<Unit> {
        return try {
            val response = childApi.getChildren()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                Log.d(TAG, "SYNC $dtos")
                childDao.insertChildren(dtos.map { it.toEntity() })
                Result.success(Unit)
            } else {
                Log.e(TAG, "ERROR while SYNC ${response.code()}")
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERROR while SYNC $e")
            Result.failure(e)
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

        return try {
            val dto = ChildDto(
                id = null,
                name = name,
                birthDate = birthDateLocal.toString(),
                gender = gender.name,
                isPremature = isPremature,
                allergies = allergies,
                chronicDiseases = chronicDiseases,
                pastDiseases = pastDiseases,
                specialFeatures = specialFeatures,
            )
            val response = childApi.createChild(dto)
            if (response.isSuccessful && response.body() != null) {
                val createdChild = response.body()!!
                // 2. Replace with real ID and data atomically
                database.withTransaction {
                    childDao.deleteChild(tempEntity)
                    childDao.insertChild(createdChild.toEntity())
                }
                Result.success(createdChild.id ?: "")
            } else {
                Result.failure(Exception("Add failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChild(child: Child): Result<Unit> {
        val originalEntity = childDao.getChildById(child.id)
        
        // 1. Optimistic update
        childDao.insertChild(child.toEntity())

        return try {
            val dto = ChildDto(
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
            val response = childApi.updateChild(child.id, dto)
            if (response.isSuccessful && response.body() != null) {
                childDao.insertChild(response.body()!!.toEntity())
                Result.success(Unit)
            } else {
                // Rollback on failure
                originalEntity?.let { childDao.insertChild(it) }
                Result.failure(Exception("Update failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Rollback on error
            originalEntity?.let { childDao.insertChild(it) }
            Result.failure(e)
        }
    }

    override fun getSelectedChildId(): Flow<String?> = settingsManager.selectedChildId

    override suspend fun setSelectedChildId(id: String?) {
        settingsManager.saveSelectedChildId(id)
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

    private fun ChildDto.toEntity() = ChildEntity(
        id = id ?: "",
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
    }
}
