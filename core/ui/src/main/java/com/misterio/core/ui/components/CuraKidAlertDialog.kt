package com.misterio.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.misterio.core.ui.R
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun CuraKidAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String = stringResource(R.string.action_confirm),
    dismissButtonText: String = stringResource(R.string.dialog_cancel),
    confirmButtonColor: Color = CurakidTheme.colors.primary
) {
    CuraKidAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm,
        title = title,
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        confirmButtonColor = confirmButtonColor,
        content = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

@Composable
fun CuraKidAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    confirmButtonText: String = stringResource(R.string.action_confirm),
    dismissButtonText: String = stringResource(R.string.dialog_cancel),
    confirmButtonColor: Color = CurakidTheme.colors.primary,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = content,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmButtonText,
                    color = confirmButtonColor,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = dismissButtonText,
                    color = CurakidTheme.colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = CurakidTheme.colors.surfaceContainer
    )
}
