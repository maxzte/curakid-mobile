package com.misterio.core.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.misterio.core.domain.model.DomainError
import com.misterio.core.ui.R

@StringRes
fun DomainError.toMessageRes(): Int {
    return when (this) {
        is DomainError.NetworkError -> R.string.error_network
        is DomainError.ServerUnreachableError -> R.string.error_server_unreachable
        is DomainError.ServerError -> R.string.error_server
        is DomainError.AuthenticationError -> R.string.error_session_expired
        is DomainError.NotFoundError -> R.string.error_not_found
        is DomainError.BadRequestError -> R.string.error_bad_request
        is DomainError.ForbiddenError -> R.string.error_forbidden
        is DomainError.ConflictError -> R.string.error_conflict
        is DomainError.RateLimitError -> R.string.error_rate_limit
        is DomainError.UnknownError -> R.string.error_generic
    }
}

@Composable
fun DomainError.asString(): String {
    return stringResource(toMessageRes())
}
