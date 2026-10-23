package com.misterio.core.domain.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Coroutine-safe equivalent of [runCatching] that does not intercept [CancellationException],
 * preserving cooperative cancellation in coroutines.
 */
inline fun <R> suspendRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
}
