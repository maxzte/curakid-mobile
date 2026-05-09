package com.misterio.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

object CurakidTheme {
    val paddings: AppPaddings
        @Composable
        get() = LocalAppPaddings.current

    val colors: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val typography: androidx.compose.material3.Typography
        @Composable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        get() = MaterialTheme.shapes

    object Radius {
        val none = RoundedCornerShape(0.dp)
        val xs = RoundedCornerShape(4.dp)
        val sm = RoundedCornerShape(8.dp)
        val md = RoundedCornerShape(12.dp)
        val lg = RoundedCornerShape(16.dp)
        val xl = RoundedCornerShape(20.dp)
        val xxl = RoundedCornerShape(24.dp)
        val full = RoundedCornerShape(50)      // pill / circle
    }

}

private val LightColorScheme = lightColorScheme(
    // Primary — blue (main actions, FAB, selected nav, progress)
    primary = CuraKidColors.Blue40,
    onPrimary = CuraKidColors.White,
    primaryContainer = CuraKidColors.Blue95,
    onPrimaryContainer = CuraKidColors.Blue30,

    // Secondary — teal (doctor visits, success badges)
    secondary = CuraKidColors.Teal40,
    onSecondary = CuraKidColors.White,
    secondaryContainer = CuraKidColors.Teal90,
    onSecondaryContainer = CuraKidColors.Teal30,

    // Tertiary — green (exercises, progress bars)
    tertiary = CuraKidColors.Green30,
    onTertiary = CuraKidColors.White,
    tertiaryContainer = CuraKidColors.Green90,
    onTertiaryContainer = CuraKidColors.Green10,

    // Error — red (overdue, alerts)
    error = CuraKidColors.Red30,
    onError = CuraKidColors.White,
    errorContainer = CuraKidColors.Red90,
    onErrorContainer = CuraKidColors.Red10,

    // Surfaces
    background = CuraKidColors.Blue99,
    onBackground = CuraKidColors.Blue30,
    surface = CuraKidColors.White,
    onSurface = CuraKidColors.Blue30,
    surfaceVariant = CuraKidColors.Blue95,
    onSurfaceVariant = CuraKidColors.Neutral40,
    surfaceContainerLow = CuraKidColors.Blue99,
    surfaceContainer = CuraKidColors.Blue99,

    // Outlines
    outline = CuraKidColors.Blue90,
    outlineVariant = CuraKidColors.Blue90,

    // Inverse
    inverseSurface = CuraKidColors.Blue30,
    inverseOnSurface = CuraKidColors.Blue95,
    inversePrimary = CuraKidColors.Blue80,
)

private val DarkColorScheme = darkColorScheme(
    primary = CuraKidColors.Blue80,
    onPrimary = CuraKidColors.Blue10,
    primaryContainer = CuraKidColors.Blue20,
    onPrimaryContainer = CuraKidColors.Blue90,

    secondary = CuraKidColors.Teal80,
    onSecondary = CuraKidColors.Teal10,
    secondaryContainer = CuraKidColors.Teal20,
    onSecondaryContainer = CuraKidColors.Teal90,

    tertiary = CuraKidColors.Green80,
    onTertiary = CuraKidColors.Green10,
    tertiaryContainer = CuraKidColors.Green10,
    onTertiaryContainer = CuraKidColors.Green90,

    error = CuraKidColors.Red80,
    onError = CuraKidColors.Red10,
    errorContainer = CuraKidColors.Red30,
    onErrorContainer = CuraKidColors.Red90,

    background = CuraKidColors.Blue10,
    onBackground = CuraKidColors.Blue90,
    surface = CuraKidColors.Neutral10,
    onSurface = CuraKidColors.Blue90,
    surfaceVariant = CuraKidColors.Blue20,
    onSurfaceVariant = CuraKidColors.Blue80,
    surfaceContainer = CuraKidColors.Blue20,

    outline = CuraKidColors.Blue30,
    outlineVariant = CuraKidColors.Blue20,

    inverseSurface = CuraKidColors.Blue90,
    inverseOnSurface = CuraKidColors.Blue30,
    inversePrimary = CuraKidColors.Blue40,
)

@Composable
fun CurakidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val paddings = remember { AppPaddings() }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    LaunchedEffect(Unit) {
//        Window.setNavigationBarContrastEnforced()
    }

    CompositionLocalProvider(
        LocalAppPaddings provides paddings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
