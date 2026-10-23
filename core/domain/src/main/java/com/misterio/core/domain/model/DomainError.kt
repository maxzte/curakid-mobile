package com.misterio.core.domain.model

import kotlin.coroutines.cancellation.CancellationException

sealed class DomainError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    class NetworkError(message: String = "Відсутній зв'язок з інтернетом", cause: Throwable? = null) : DomainError(message, cause)
    class ServerError(message: String = "Помилка сервера. Спробуйте пізніше", cause: Throwable? = null) : DomainError(message, cause)
    class AuthenticationError(message: String = "Сесія закінчилася", cause: Throwable? = null) : DomainError(message, cause)
    class NotFoundError(message: String = "Ресурс не знайдено", cause: Throwable? = null) : DomainError(message, cause)
    class BadRequestError(message: String = "Некоректний запит", cause: Throwable? = null) : DomainError(message, cause)
    class UnknownError(message: String = "Виникла непередбачувана помилка", cause: Throwable? = null) : DomainError(message, cause)

    companion object {
        fun fromThrowable(throwable: Throwable): DomainError {
            return when (throwable) {
                is CancellationException -> throw throwable
                is DomainError -> throwable
                else -> UnknownError(throwable.message ?: "Виникла непередбачувана помилка", cause = throwable)
            }
        }
    }
}
