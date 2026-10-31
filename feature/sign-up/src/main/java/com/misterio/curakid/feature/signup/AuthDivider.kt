package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.theme.CurakidTheme

@Composable
internal fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.spaceMd)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = CurakidTheme.colors.outline)
        Text(
            text = stringResource(R.string.or_divider),
            style = CurakidTheme.typography.bodySmall
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = CurakidTheme.colors.outline)
    }
}