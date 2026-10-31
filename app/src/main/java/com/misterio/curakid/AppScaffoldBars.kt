package com.misterio.curakid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.BOT_BAR_H
import com.misterio.core.ui.TopBarConfiguration
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun OfflineIndicator(
    isConnected: Boolean,
    isSyncing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val showBanner = !isConnected || isSyncing

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        val backgroundColor = if (!isConnected) {
            CurakidTheme.colors.errorContainer
        } else {
            CurakidTheme.colors.surfaceVariant
        }
        val contentColor = if (!isConnected) {
            CurakidTheme.colors.onErrorContainer
        } else {
            CurakidTheme.colors.onSurfaceVariant
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (!isConnected) Icons.Default.CloudOff else Icons.Default.Refresh,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (!isConnected) {
                        stringResource(R.string.offline_mode)
                    } else {
                        "Синхронізація даних з сервером..."
                    },
                    color = contentColor,
                    style = CurakidTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun AppTopBar(
    config: TopBarConfiguration,
    modifier: Modifier = Modifier
) {
    val backgroundColor = CurakidTheme.colors.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(vertical = CurakidTheme.paddings.small)
    ) {
        Text(
            text = config.title,
            style = CurakidTheme.typography.titleMedium,
            color = CurakidTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Center)
                .padding(horizontal = CurakidTheme.paddings.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (config.showBack) {
                PillButton(onClick = config.onBack)
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            if (config.actions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    config.actions.forEach { action ->
                        IconPillButton(
                            icon = action.icon,
                            onClick = action.onClick,
                            contentDescription = action.contentDescription
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }
        }
    }
}

@Composable
fun PillButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val pillColor = CurakidTheme.colors.onSurface.copy(alpha = 0.10f)
    val borderColor = CurakidTheme.colors.onSurface.copy(alpha = 0.18f)

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(pillColor)
            .border(width = Dp.Hairline, color = borderColor, shape = CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(
                horizontal = CurakidTheme.paddings.small,
                vertical = CurakidTheme.paddings.small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = CurakidTheme.colors.onSurface.copy(alpha = 0.92f)
        )
    }
}

@Composable
fun IconPillButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val pillColor = CurakidTheme.colors.onSurface.copy(alpha = 0.10f)
    val borderColor = CurakidTheme.colors.onSurface.copy(alpha = 0.18f)

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(pillColor)
            .border(width = Dp.Hairline, color = borderColor, shape = CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = CurakidTheme.colors.onSurface.copy(alpha = 0.90f)
        )
    }
}

@Composable
fun AppBottomBar(
    activeTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = CurakidTheme.colors.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        backgroundColor
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        shape = CircleShape
                        clip = true
                    }
                    .background(backgroundColor.copy(alpha = 0.95f))
                    .border(
                        width = Dp.Hairline,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(BOT_BAR_H)
                    .padding(horizontal = CurakidTheme.paddings.small),
                horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidTabItem(tab = tab, isSelected = isSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidTabItem(
    tab: BottomTab,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) CurakidTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) CurakidTheme.colors.primary else CurakidTheme.colors.onSurfaceVariant,
    )

    Column(
        modifier = modifier
            .wrapContentSize()
            .background(backgroundColor, CircleShape)
            .padding(
                horizontal = CurakidTheme.paddings.medium,
                vertical = CurakidTheme.paddings.extraSmall
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            modifier = Modifier.size(22.dp),
            tint = contentColor
        )

        Text(
            text = tab.label,
            style = CurakidTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1
        )
    }
}
