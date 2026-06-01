package com.misterio.core.network.api

import com.misterio.core.network.dto.LegalDocumentResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface LegalDocumentApi {

    @GET("api/legal")
    suspend fun getAllDocuments(): Response<List<LegalDocumentResponse>>

    @GET
    suspend fun downloadContent(@Url url: String): ResponseBody

}
