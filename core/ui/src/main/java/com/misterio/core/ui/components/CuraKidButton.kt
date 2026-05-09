package com.misterio.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.theme.CurakidTheme

enum class IconPosition {
    Start, End
}

@Composable
fun CuraKidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    iconPosition: IconPosition = IconPosition.Start,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor ?: CurakidTheme.colors.primary,
            contentColor = contentColor ?: CurakidTheme.colors.onPrimary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor ?: CurakidTheme.colors.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null && iconPosition == IconPosition.Start) {
                    Icon(imageVector = icon, contentDescription = contentDescription)
                    Spacer(Modifier.width(8.dp))
                }

                Text(text = text, style = CurakidTheme.typography.labelLarge)

                if (icon != null && iconPosition == IconPosition.End) {
                    Spacer(Modifier.width(8.dp))
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
        }
    }
}
