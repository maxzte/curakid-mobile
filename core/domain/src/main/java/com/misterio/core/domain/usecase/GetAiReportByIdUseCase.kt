package com.misterio.core.domain.usecase

import com.misterio.core.domain.repository.AiRepository
import javax.inject.Inject

class GetAiReportByIdUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke(id: String) = aiRepository.getReportBy(id)
}