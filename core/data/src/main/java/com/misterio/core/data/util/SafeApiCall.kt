package com.misterio.core.data.util

import com.misterio.core.domain.model.DomainError
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else if (response.isSuccessful && response.body() == null && response.code() == 204) {
             @Suppress("UNCHECKED_CAST")
             Result.success(Unit as T)
        } else {
            Result.failure(mapToDomainError(response))
        }
    } catch (e: IOException) {
        Result.failure(DomainError.NetworkError(cause = e))
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Result.failure(DomainError.UnknownError(cause = e))
    }
}

private fun <T> mapToDomainError(response: Response<T>): DomainError {
    return when (response.code()) {
        401 -> DomainError.AuthenticationError(cause = Exception("HTTP ${response.code()}"))
        404 -> DomainError.NotFoundError(cause = Exception("HTTP ${response.code()}"))
        in 500..599 -> DomainError.ServerError(cause = Exception("HTTP ${response.code()}"))
        else -> DomainError.UnknownError("Помилка: ${response.code()}", cause = Exception("HTTP ${response.code()}"))
    }
}
