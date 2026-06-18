package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AiRepository
import java.util.UUID
import javax.inject.Inject

class SyncChildAiReportsUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(childId: UUID) = aiRepository.syncReports(childId)
}