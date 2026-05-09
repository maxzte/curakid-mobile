package com.misterio.core.data.repository

import com.misterio.core.domain.repository.AiRepository
import com.misterio.core.network.api.AiApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val aiApi: AiApi
) : AiRepository {
    override suspend fun getAnalysis(childId: String): Result<String> {
        return try {
            val response = aiApi.requestAnalysis(childId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.report)
            } else {
                Result.failure(Exception("AI analysis failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
