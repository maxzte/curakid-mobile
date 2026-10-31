package com.misterio.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
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
        val xxxl = RoundedCornerShape(32.dp)
        val full = RoundedCornerShape(50)      // pill / circle
    }
}

val CuraKidShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

private val LightColorScheme = lightColorScheme(
    primary = CuraKidColors.Blue40,
    onPrimary = CuraKidColors.White,
    primaryContainer = CuraKidColors.Blue95,
    onPrimaryContainer = CuraKidColors.Blue30,

    secondary = CuraKidColors.Teal40,
    onSecondary = CuraKidColors.White,
    secondaryContainer = CuraKidColors.Teal90,
    onSecondaryContainer = CuraKidColors.Teal30,

    tertiary = CuraKidColors.Green30,
    onTertiary = CuraKidColors.White,
    tertiaryContainer = CuraKidColors.Green90,
    onTertiaryContainer = CuraKidColors.Green10,

    error = CuraKidColors.Red30,
    onError = CuraKidColors.White,
    errorContainer = CuraKidColors.Red90,
    onErrorContainer = CuraKidColors.Red10,

    background = CuraKidColors.Blue99,
    onBackground = CuraKidColors.Blue30,
    surface = CuraKidColors.White,
    onSurface = CuraKidColors.Blue30,
    surfaceVariant = CuraKidColors.Blue95,
    onSurfaceVariant = CuraKidColors.Neutral40,
    surfaceContainerLow = CuraKidColors.Blue99,
    surfaceContainer = CuraKidColors.Blue99,

    outline = CuraKidColors.Blue90,
    outlineVariant = CuraKidColors.Blue90,

    inverseSurface = CuraKidColors.Blue30,
    inverseOnSurface = CuraKidColors.Blue95,
    inversePrimary = CuraKidColors.Blue80,
)

private val DarkColorScheme = darkColorScheme(
    primary = CuraKidColors.Primary,
    onPrimary = CuraKidColors.OnPrimary,
    primaryContainer = CuraKidColors.PrimaryContainer,
    onPrimaryContainer = CuraKidColors.OnPrimaryContainer,
    inversePrimary = CuraKidColors.InversePrimary,

    secondary = CuraKidColors.Secondary,
    onSecondary = CuraKidColors.OnSecondary,
    secondaryContainer = CuraKidColors.SecondaryContainer,
    onSecondaryContainer = CuraKidColors.OnSecondaryContainer,

    tertiary = CuraKidColors.Tertiary,
    onTertiary = CuraKidColors.OnTertiary,
    tertiaryContainer = CuraKidColors.TertiaryContainer,
    onTertiaryContainer = CuraKidColors.OnTertiaryContainer,

    error = CuraKidColors.Error,
    onError = CuraKidColors.OnError,
    errorContainer = CuraKidColors.ErrorContainer,
    onErrorContainer = CuraKidColors.OnErrorContainer,

    background = CuraKidColors.Background,
    onBackground = CuraKidColors.OnBackground,

    surface = CuraKidColors.Surface,
    onSurface = CuraKidColors.OnSurface,
    surfaceVariant = CuraKidColors.SurfaceVariant,
    onSurfaceVariant = CuraKidColors.OnSurfaceVariant,

    surfaceDim = CuraKidColors.SurfaceDim,
    surfaceBright = CuraKidColors.SurfaceBright,
    surfaceContainerLowest = CuraKidColors.SurfaceContainerLowest,
    surfaceContainerLow = CuraKidColors.SurfaceContainerLow,
    surfaceContainer = CuraKidColors.SurfaceContainer,
    surfaceContainerHigh = CuraKidColors.SurfaceContainerHigh,
    surfaceContainerHighest = CuraKidColors.SurfaceContainerHighest,

    outline = CuraKidColors.Outline,
    outlineVariant = CuraKidColors.OutlineVariant,

    inverseSurface = CuraKidColors.InverseSurface,
    inverseOnSurface = CuraKidColors.InverseOnSurface,
    surfaceTint = CuraKidColors.SurfaceTint,
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
            shapes = CuraKidShapes,
            content = content
        )
    }
}
