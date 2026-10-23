package com.misterio.core.data.util

import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.util.suspendRunCatching
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
    return suspendRunCatching {
        val response = call()
        if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else if (response.isSuccessful && response.body() == null && response.code() == 204) {
            @Suppress("UNCHECKED_CAST")
            Unit as T
        } else {
            throw mapToDomainError(response)
        }
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { error ->
            val domainError = when (error) {
                is DomainError -> error
                is IOException -> DomainError.NetworkError(cause = error)
                else -> DomainError.UnknownError(cause = error)
            }
            Result.failure(domainError)
        }
    )
}

private fun <T> mapToDomainError(response: Response<T>): DomainError {
    return when (response.code()) {
        400 -> DomainError.BadRequestError(cause = Exception("HTTP ${response.code()}"))
        401 -> DomainError.AuthenticationError(cause = Exception("HTTP ${response.code()}"))
        404 -> DomainError.NotFoundError(cause = Exception("HTTP ${response.code()}"))
        in 500..599 -> DomainError.ServerError(cause = Exception("HTTP ${response.code()}"))
        else -> DomainError.UnknownError("Помилка: ${response.code()}", cause = Exception("HTTP ${response.code()}"))
    }
}
