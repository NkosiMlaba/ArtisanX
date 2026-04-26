package com.example.artisanx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary            = IndigoPrimary,
    onPrimary          = IndigoOnPrimary,
    primaryContainer   = IndigoContainer,
    onPrimaryContainer = IndigoOnContainer,

    secondary            = GoldSecondary,
    onSecondary          = GoldOnSecondary,
    secondaryContainer   = GoldContainer,
    onSecondaryContainer = GoldOnContainer,

    tertiary            = TealTertiary,
    onTertiary          = TealOnTertiary,
    tertiaryContainer   = TealContainer,
    onTertiaryContainer = TealOnContainer,

    background         = LightBackground,
    onBackground       = LightOnBackground,
    surface            = LightSurface,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightSurfaceVariant,
    onSurfaceVariant   = LightOnSurfaceVariant,
    outline            = LightOutline,
    outlineVariant     = LightOutlineVariant,
    scrim              = LightScrim,

    error              = ErrorColor,
    onError            = OnErrorColor,
    errorContainer     = ErrorContainer,
    onErrorContainer   = OnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary            = DarkIndigoPrimary,
    onPrimary          = DarkIndigoOnPrimary,
    primaryContainer   = DarkIndigoContainer,
    onPrimaryContainer = DarkIndigoOnContainer,

    secondary            = DarkGoldSecondary,
    onSecondary          = DarkGoldOnSecondary,
    secondaryContainer   = DarkGoldContainer,
    onSecondaryContainer = DarkGoldOnContainer,

    tertiary            = DarkTealTertiary,
    onTertiary          = DarkTealOnTertiary,
    tertiaryContainer   = DarkTealContainer,
    onTertiaryContainer = DarkTealOnContainer,

    background         = DarkBackground,
    onBackground       = DarkOnBackground,
    surface            = DarkSurface,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkSurfaceVariant,
    onSurfaceVariant   = DarkOnSurfaceVariant,
    outline            = DarkOutline,
    outlineVariant     = DarkOutlineVariant,

    error              = DarkErrorColor,
    onError            = DarkOnErrorColor,
    errorContainer     = DarkErrorContainer,
    onErrorContainer   = DarkOnErrorContainer,
)

@Composable
fun ArtisanXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
