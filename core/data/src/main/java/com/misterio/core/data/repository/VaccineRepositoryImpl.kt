package com.misterio.core.data.repository

import com.misterio.core.database.dao.VaccineDao
import com.misterio.core.database.entity.VaccineEntity
import com.misterio.core.domain.model.Vaccine
import com.misterio.core.domain.model.VaccineStatus
import com.misterio.core.domain.model.VaccineType
import com.misterio.core.domain.repository.VaccineRepository
import com.misterio.core.network.api.VaccineApi
import com.misterio.core.network.dto.VaccineDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccineRepositoryImpl @Inject constructor(
    private val vaccineApi: VaccineApi,
    private val vaccineDao: VaccineDao,
) : VaccineRepository {

    override fun getVaccines(childId: String): Flow<List<Vaccine>> = vaccineDao
        .getAllVaccines()
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncVaccines(childId: String): Result<Unit> {
        return try {
            val response = vaccineApi.getVaccines(childId)
            if (response.isSuccessful && response.body() != null) {
                vaccineDao.insertVaccines(response.body()!!.map { it.toEntity() })
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch vaccines: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun VaccineEntity.toDomain() = Vaccine(
        id = id,
        name = name,
        disease = disease,
        doseNumber = doseNumber,
        totalDoses = totalDoses,
        type = VaccineType.valueOf(type),
        scheduledAgeMonths = scheduledAgeMonths,
        scheduledAgeDays = scheduledAgeDays,
        administeredDate = administeredDate,
        batchNumber = batchNumber,
        administeredBy = administeredBy,
        notes = notes,
        status = VaccineStatus.valueOf(status),
    )

    private fun VaccineDto.toEntity() = VaccineEntity(
        id = id,
        name = name,
        disease = disease,
        doseNumber = doseNumber,
        totalDoses = totalDoses,
        type = type,
        scheduledAgeMonths = scheduledAgeMonths,
        scheduledAgeDays = scheduledAgeDays,
        administeredDate = administeredDate?.let { LocalDate.parse(it) },
        batchNumber = batchNumber,
        administeredBy = administeredBy,
        notes = notes,
        status = status
    )
}
