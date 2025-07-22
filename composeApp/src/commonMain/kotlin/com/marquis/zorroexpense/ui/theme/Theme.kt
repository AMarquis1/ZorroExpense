package com.marquis.zorroexpense.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ZorroPrimary,
    onPrimary = ZorroOnPrimary,
    primaryContainer = ZorroPrimaryVariant,
    onPrimaryContainer = ZorroOnPrimary,
    secondary = ZorroSecondary,
    onSecondary = ZorroOnSecondary,
    secondaryContainer = ZorroSecondaryVariant,
    onSecondaryContainer = ZorroOnSecondary,
    tertiary = ZorroSecondary,
    onTertiary = ZorroOnSecondary,
    background = ZorroBackground,
    onBackground = ZorroOnBackground,
    surface = ZorroSurface,
    onSurface = ZorroOnSurface,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = ZorroOnSurface,
    error = ZorroError,
    onError = ZorroOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = ZorroPrimaryDark,
    onPrimary = ZorroOnPrimaryDark,
    primaryContainer = ZorroPrimaryVariantDark,
    onPrimaryContainer = ZorroOnPrimaryDark,
    secondary = ZorroSecondaryDark,
    onSecondary = ZorroOnSecondaryDark,
    secondaryContainer = ZorroSecondaryVariantDark,
    onSecondaryContainer = ZorroOnSecondaryDark,
    tertiary = ZorroSecondaryDark,
    onTertiary = ZorroOnSecondaryDark,
    background = ZorroBackgroundDark,
    onBackground = ZorroOnBackgroundDark,
    surface = ZorroSurfaceDark,
    onSurface = ZorroOnSurfaceDark,
    surfaceVariant = Color(0xFF2E2E2E),
    onSurfaceVariant = ZorroOnSurfaceDark,
    error = ZorroErrorDark,
    onError = ZorroOnErrorDark
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ZorroExpenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}