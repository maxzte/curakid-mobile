package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.LegalDocument
import com.misterio.core.domain.repository.LegalDocumentRepository
import javax.inject.Inject

class GetLegalDocumentsUseCase @Inject constructor(
    private val legalDocumentRepository: LegalDocumentRepository,
) {

    suspend operator fun invoke(): Result<List<LegalDocument>> {
        return legalDocumentRepository.getLegalDocuments()
    }

}
