package com.misterio.curakid.feature.signup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme

@Composable
internal fun GoogleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CurakidTheme.Radius.md,
        border = BorderStroke(1.dp, CurakidTheme.colors.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CuraKidColors.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Google G icon via text — replace with painterResource(R.drawable.ic_google)
            Text(
                text = "G",
                style = CurakidTheme.typography.bodyMedium.copy(
                    fontSize = 17.sp,
                    color = Color(0xFF4285F4)
                )
            )
            Text(
                text = "Продовжити з Google",
                style = CurakidTheme.typography.bodyMedium.copy(
                    color = CuraKidColors.Blue30,
                    fontSize = 15.sp
                )
            )
        }
    }
}