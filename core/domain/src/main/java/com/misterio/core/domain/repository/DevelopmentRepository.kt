package com.misterio.core.domain.repository

import com.misterio.core.domain.model.LifePeriod
import kotlinx.coroutines.flow.Flow

interface DevelopmentRepository {

    fun getLifePeriods(): Flow<List<LifePeriod>>

    fun getLifePeriod(code: String): Flow<LifePeriod?>

    suspend fun syncLifePeriods(): Result<Unit>

}
