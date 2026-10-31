package com.misterio.core.network.api

import com.misterio.core.network.dto.ChatMessageResponse
import com.misterio.core.network.dto.ChatRequest
import com.misterio.core.network.dto.PagedResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.util.UUID

interface ChatApi {

    /**
     * Отримання історії чату по конкретній дитині.
     */
    @GET("api/v1/chat/history/{childId}")
    suspend fun getChatHistory(
        @Path("childId") childId: UUID,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedResponse<ChatMessageResponse>>

    /**
     * Відправка повідомлення та отримання відповіді у форматі Server-Sent Events (SSE).
     * Анотація @Streaming важлива, щоб OkHttp не намагався завантажити всю відповідь
     * у пам'ять до її завершення.
     */
    @Streaming
    @POST("api/v1/chat/stream")
    fun streamChat(
        @Body request: ChatRequest,
    ): Call<ResponseBody>

}
