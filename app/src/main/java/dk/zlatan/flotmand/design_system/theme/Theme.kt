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

private val MidnightHarvestColorScheme = darkColorScheme(
    primary = HarvestPrimary,
    onPrimary = HarvestOnPrimary,
    primaryContainer = HarvestPrimaryContainer,
    onPrimaryContainer = HarvestOnPrimaryContainer,

    secondary = MidnightSecondary,
    onSecondary = MidnightOnSecondary,
    secondaryContainer = MidnightSecondaryContainer,
    onSecondaryContainer = MidnightOnSecondaryContainer,

    tertiary = MidnightTertiary,
    onTertiary = MidnightOnTertiary,
    tertiaryContainer = MidnightTertiaryContainer,
    onTertiaryContainer = MidnightOnTertiaryContainer,

    background = MidnightBackground,
    onBackground = MidnightOnBackground,
    surface = MidnightSurface,
    onSurface = MidnightOnSurface,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MidnightOnSurfaceVariant,

    error = HarvestErrorDark,
    onError = HarvestOnErrorDark,
    errorContainer = HarvestErrorContainerDark,
    onErrorContainer = HarvestOnErrorContainerDark,

    outline = MidnightOutline,
    outlineVariant = MidnightOutlineVariant,
    scrim = Black,
)

private val HarvestLightColorScheme = lightColorScheme(
    primary = HarvestLightPrimary,
    onPrimary = HarvestLightOnPrimary,
    primaryContainer = HarvestLightPrimaryContainer,
    onPrimaryContainer = HarvestLightOnPrimaryContainer,

    secondary = HarvestLightSecondary,
    onSecondary = HarvestLightOnSecondary,
    secondaryContainer = HarvestLightSecondaryContainer,
    onSecondaryContainer = HarvestLightOnSecondaryContainer,

    tertiary = HarvestLightTertiary,
    onTertiary = HarvestLightOnTertiary,
    tertiaryContainer = HarvestLightTertiaryContainer,
    onTertiaryContainer = HarvestLightOnTertiaryContainer,

    background = HarvestLightBackground,
    onBackground = HarvestLightOnBackground,
    surface = HarvestLightSurface,
    onSurface = HarvestLightOnSurface,
    surfaceVariant = HarvestLightSurfaceVariant,
    onSurfaceVariant = HarvestLightOnSurfaceVariant,

    error = HarvestError,
    onError = HarvestOnError,
    errorContainer = HarvestErrorContainer,
    onErrorContainer = HarvestOnErrorContainer,

    outline = HarvestLightOutline,
    outlineVariant = HarvestLightOutlineVariant,
    scrim = Black,
)

@Composable
fun FlotMandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MidnightHarvestColorScheme
        else -> HarvestLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}