package com.misterio.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.DomainError
import com.misterio.core.ui.R
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun ErrorView(
    error: DomainError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(CurakidTheme.paddings.large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(CurakidTheme.paddings.medium))
        Text(
            text = error.message ?: stringResource(R.string.error_generic),
            style = CurakidTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        val cause = error.cause
        if (cause?.message != null) {
            Text(
                text = cause.message!!,
                style = CurakidTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(CurakidTheme.paddings.large))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
