package com.misterio.core.data.repository

import android.content.Context
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.domain.model.LegalDocument
import com.misterio.core.domain.repository.LegalDocumentRepository
import com.misterio.core.domain.util.suspendRunCatching
import com.misterio.core.network.api.LegalDocumentApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

class LegalDocumentRepositoryImpl @Inject constructor(
    private val legalDocumentApi: LegalDocumentApi,
    @param:ApplicationContext private val context: Context,
) : LegalDocumentRepository {

    override suspend fun getLegalDocuments(): Result<List<LegalDocument>> {
        return safeApiCall {
            legalDocumentApi.getAllDocuments()
        }.map { it.map { it.toDomain() } }
    }

    override suspend fun downloadContent(url: String): Result<String> = withContext(Dispatchers.IO) {
        val fileName = urlToFileName(url)
        val cacheFile = File(context.cacheDir, fileName)

        if (cacheFile.exists()) {
            return@withContext Result.success(cacheFile.readText())
        }

        suspendRunCatching {
            val content = legalDocumentApi.downloadContent(url).string()
            cacheFile.writeText(content)
            content
        }
    }

    private fun urlToFileName(url: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
