package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme

import com.misterio.core.ui.components.CuraKidButton

@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    CuraKidButton(
        text = text,
        onClick = onClick,
        loading = isLoading,
        enabled = enabled
    )
}