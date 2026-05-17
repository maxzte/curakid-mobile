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

@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CurakidTheme.Radius.md,
        colors = ButtonDefaults.buttonColors(
            containerColor = CurakidTheme.colors.primary,
            disabledContainerColor = CurakidTheme.colors.inversePrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = CuraKidColors.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = CurakidTheme.typography.bodyMedium.copy(
                    color = CuraKidColors.White,
                    fontSize = 15.sp
                )
            )
        }
    }
}