package com.misterio.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.BOT_BAR_H
import com.misterio.core.ui.theme.CurakidTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemBarsAwareBox(
    modifier: Modifier = Modifier,
    applyTop: Boolean = true,
    applyBottom: Boolean = true,
    applyHorizontal: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val insets = WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .union(WindowInsets.navigationBarsIgnoringVisibility)

    val gestureInsets = WindowInsets.systemGestures

    val combinedInsets = insets.union(gestureInsets)

    val paddingValues = combinedInsets.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = if (applyHorizontal) paddingValues.calculateStartPadding(LayoutDirection.Ltr) else 0.dp,
                end = if (applyHorizontal) paddingValues.calculateEndPadding(LayoutDirection.Ltr) else 0.dp,
                top = if (applyTop) paddingValues.calculateTopPadding() else 0.dp,
                bottom = if (applyBottom) paddingValues.calculateBottomPadding() else 0.dp
            )
    ) {
        content()
    }
}

@Composable
fun rememberListContentPadding(): PaddingValues {
    val insets = WindowInsets.systemBars
        .only(WindowInsetsSides.Vertical)
        .asPaddingValues()

    return PaddingValues(
        top = insets.calculateTopPadding() + CurakidTheme.paddings.medium,
        bottom = insets.calculateBottomPadding() + BOT_BAR_H + CurakidTheme.paddings.medium
    )
}