package com.misterio.core.domain

import com.misterio.core.domain.model.DomainError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface UiState<out T> {

    data object Initial : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: DomainError) : UiState<Nothing> {
        val errorMessage: String get() = error.message ?: "Виникла помилка"
    }

    fun getOrNull(): T? = (this as? Success)?.data
}

fun <T> Result<T>.toUiState(): UiState<T> {
    return fold(
        onSuccess = { UiState.Success(it) },
        onFailure = { 
            val domainError = (it as? DomainError) ?: DomainError.UnknownError(it.message ?: "Unknown error", it)
            UiState.Error(domainError)
        }
    )
}

fun <T> Flow<T>.asUiState(): Flow<UiState<T>> {
    return this
        .map<T, UiState<T>> { UiState.Success(it) }
        .onStart { emit(UiState.Loading) }
        .catch { 
            val domainError = (it as? DomainError) ?: DomainError.UnknownError(it.message ?: "Unknown error", it)
            emit(UiState.Error(domainError))
        }
}
