package com.misterio.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.R
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun ScreenHeader(
    title: String,
    childName: String? = null,
    onChatClick: (() -> Unit)? = null,
    onChildClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val name = childName?.takeIf { it.isNotEmpty() }?.take(1)?.uppercase().orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = CurakidTheme.paddings.spaceXs,
                vertical = CurakidTheme.paddings.spaceSm
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = CurakidTheme.typography.headlineSmall,
            color = CurakidTheme.colors.onBackground,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.spaceSm)
        ) {
            actions()

            if (onChatClick != null) {
                IconButton(onClick = onChatClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = stringResource(R.string.cd_chat),
                    )
                }
            }

            if (name.isNotEmpty()) {
                val avatarModifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CurakidTheme.colors.surfaceVariant)
                    .then(
                        if (onChildClick != null) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onChildClick
                            )
                        } else Modifier
                    )

                Box(
                    modifier = avatarModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = CurakidTheme.typography.bodyMedium,
                        color = CurakidTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
