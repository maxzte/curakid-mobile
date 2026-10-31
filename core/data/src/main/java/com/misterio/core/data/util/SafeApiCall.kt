package com.misterio.core.data.util

import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.util.suspendRunCatching
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
                is UnknownHostException, is ConnectException -> DomainError.ServerUnreachableError(cause = error)
                is SocketTimeoutException -> DomainError.ServerUnreachableError(message = "Час очікування відповіді сервера вичерпано", cause = error)
                is IOException -> DomainError.NetworkError(cause = error)
                else -> DomainError.UnknownError(cause = error)
            }
            Result.failure(domainError)
        }
    )
}

private fun <T> mapToDomainError(response: Response<T>): DomainError {
    val errorBodyStr = try {
        response.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    val extractedMessage = errorBodyStr?.let { body ->
        try {
            val json = org.json.JSONObject(body)
            if (json.has("message")) json.getString("message")
            else if (json.has("error")) json.getString("error")
            else if (json.has("detail")) json.getString("detail")
            else null
        } catch (_: Exception) {
            body.takeIf { it.isNotBlank() && it.length < 200 }
        }
    }

    val defaultMessage = extractedMessage ?: "HTTP ${response.code()}"
    val cause = Exception(defaultMessage)

    return when (response.code()) {
        400 -> DomainError.BadRequestError(extractedMessage ?: "Некоректний запит", cause = cause)
        401 -> DomainError.AuthenticationError(extractedMessage ?: "Сесія закінчилася", cause = cause)
        403 -> DomainError.ForbiddenError(extractedMessage ?: "Доступ заборонено", cause = cause)
        404 -> DomainError.NotFoundError(extractedMessage ?: "Ресурс не знайдено", cause = cause)
        409 -> DomainError.ConflictError(extractedMessage ?: "Конфлікт даних або значення вже існує", cause = cause)
        429 -> DomainError.RateLimitError(extractedMessage ?: "Забагато запитів. Зачекайте хвилину", cause = cause)
        in 500..599 -> DomainError.ServerError(extractedMessage ?: "Помилка сервера. Спробуйте пізніше", cause = cause)
        else -> DomainError.UnknownError(extractedMessage ?: "Помилка: ${response.code()}", cause = cause)
    }
}
