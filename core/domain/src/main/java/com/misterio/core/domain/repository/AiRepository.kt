package com.misterio.core.domain.repository

interface AiRepository {
    suspend fun getAnalysis(childId: String): Result<String>
}
