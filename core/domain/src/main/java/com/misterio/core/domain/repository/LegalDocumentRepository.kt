package com.misterio.core.domain.repository

import com.misterio.core.domain.model.LegalDocument

interface LegalDocumentRepository {

    suspend fun getLegalDocuments(): Result<List<LegalDocument>>

    suspend fun downloadContent(url: String): Result<String>

}
