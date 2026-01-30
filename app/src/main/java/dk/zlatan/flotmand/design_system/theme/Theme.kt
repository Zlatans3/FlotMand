package dk.zlatan.flotmand.design_system.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = PrimaryBlueLight,
    onPrimary = White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,

    // Secondary colors
    secondary = SecondaryBlue,
    onSecondary = BackgroundDark,
    secondaryContainer = SecondaryBlueDark,
    onSecondaryContainer = SecondaryBlueLight,

    // Tertiary (optional, using secondary variants)
    tertiary = InfoBlue,
    onTertiary = White,

    // Background & Surface
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextLightSecondary,

    // Error colors
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRed,

    // Outline (for borders, dividers)
    outline = DividerDark,
    outlineVariant = SurfaceDarkVariant,

    // Scrim (for overlays)
    scrim = BackgroundDark
)

private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = PrimaryBlue,
    onPrimary = White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = White,

    // Secondary colors
    secondary = SecondaryBlue,
    onSecondary = White,
    secondaryContainer = SecondaryBlueLight,
    onSecondaryContainer = SecondaryBlueDark,

    // Tertiary (optional, using secondary variants)
    tertiary = InfoBlue,
    onTertiary = White,

    // Background & Surface
    background = BackgroundLightBlue,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = TextDarkSecondary,

    // Error colors
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRed,

    // Outline (for borders, dividers)
    outline = Divider,
    outlineVariant = SurfaceLightVariant,

    // Scrim (for overlays)
    scrim = BackgroundDark
)

@Composable
fun FlotMandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}