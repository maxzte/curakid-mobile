package com.misterio.core.domain.model

import kotlin.coroutines.cancellation.CancellationException

sealed class DomainError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    class NetworkError(message: String = "No internet connection", cause: Throwable? = null) : DomainError(message, cause)
    class ServerUnreachableError(message: String = "Server is unreachable. Please check your connection", cause: Throwable? = null) : DomainError(message, cause)
    class ServerError(message: String = "Server error. Please try again later", cause: Throwable? = null) : DomainError(message, cause)
    class AuthenticationError(message: String = "Session expired", cause: Throwable? = null) : DomainError(message, cause)
    class NotFoundError(message: String = "Resource not found", cause: Throwable? = null) : DomainError(message, cause)
    class BadRequestError(message: String = "Bad request", cause: Throwable? = null) : DomainError(message, cause)
    class ForbiddenError(message: String = "Access denied or account not activated", cause: Throwable? = null) : DomainError(message, cause)
    class ConflictError(message: String = "Data conflict or resource already exists", cause: Throwable? = null) : DomainError(message, cause)
    class RateLimitError(message: String = "Too many requests. Please wait a moment", cause: Throwable? = null) : DomainError(message, cause)
    class UnknownError(message: String = "An unexpected error occurred", cause: Throwable? = null) : DomainError(message, cause)

    companion object {
        fun fromThrowable(throwable: Throwable): DomainError {
            return when (throwable) {
                is CancellationException -> throw throwable
                is DomainError -> throwable
                else -> UnknownError(throwable.message ?: "An unexpected error occurred", cause = throwable)
            }
        }
    }
}
