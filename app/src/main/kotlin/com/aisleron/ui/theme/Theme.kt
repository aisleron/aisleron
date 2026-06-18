/*
 * Copyright (C) 2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.aisleron.domain.preferences.ApplicationTheme
import com.aisleron.domain.preferences.PureBlackStyle

enum class AppContrast { STANDARD, MEDIUM, HIGH }

private val LightStandardScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurfaceBright,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest
)

private val LightMediumContrastScheme = lightColorScheme(
    primary = LightPrimaryMedium,
    onPrimary = LightOnPrimaryMedium,
    primaryContainer = LightPrimaryContainerMedium,
    onPrimaryContainer = LightOnPrimaryContainerMedium,
    secondary = LightSecondaryMedium,
    onSecondary = LightOnSecondaryMedium,
    secondaryContainer = LightSecondaryContainerMedium,
    onSecondaryContainer = LightOnSecondaryContainerMedium,
    tertiary = LightTertiaryMedium,
    onTertiary = LightOnTertiaryMedium,
    tertiaryContainer = LightTertiaryContainerMedium,
    onTertiaryContainer = LightOnTertiaryContainerMedium,
    error = LightErrorMedium,
    onError = LightOnErrorMedium,
    errorContainer = LightErrorContainerMedium,
    onErrorContainer = LightOnErrorContainerMedium,
    background = LightBackgroundMedium,
    onBackground = LightOnBackgroundMedium,
    surface = LightSurfaceMedium,
    onSurface = LightOnSurfaceMedium,
    surfaceVariant = LightSurfaceVariantMedium,
    onSurfaceVariant = LightOnSurfaceVariantMedium,
    outline = LightOutlineMedium,
    outlineVariant = LightOutlineVariantMedium,
    scrim = LightScrim,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    surfaceDim = LightSurfaceDimMedium,
    surfaceBright = LightSurfaceBrightMedium,
    surfaceContainerLowest = LightSurfaceContainerLowestMedium,
    surfaceContainerLow = LightSurfaceContainerLowMedium,
    surfaceContainer = LightSurfaceContainerMediumRes,
    surfaceContainerHigh = LightSurfaceContainerHighMedium,
    surfaceContainerHighest = LightSurfaceContainerHighestMedium
)

private val LightHighContrastScheme = lightColorScheme(
    primary = LightPrimaryHigh,
    onPrimary = LightOnPrimaryHigh,
    primaryContainer = LightPrimaryContainerHigh,
    onPrimaryContainer = LightOnPrimaryContainerHigh,
    secondary = LightSecondaryHigh,
    onSecondary = LightOnSecondaryHigh,
    secondaryContainer = LightSecondaryContainerHigh,
    onSecondaryContainer = LightOnSecondaryContainerHigh,
    tertiary = LightTertiaryHigh,
    onTertiary = LightOnTertiaryHigh,
    tertiaryContainer = LightTertiaryContainerHigh,
    onTertiaryContainer = LightOnTertiaryContainerHigh,
    error = LightErrorHigh,
    onError = LightOnErrorHigh,
    errorContainer = LightErrorContainerHigh,
    onErrorContainer = LightOnErrorContainerHigh,
    background = LightBackgroundHigh,
    onBackground = LightOnBackgroundHigh,
    surface = LightSurfaceHigh,
    onSurface = LightOnSurfaceHigh,
    surfaceVariant = LightSurfaceVariantHigh,
    onSurfaceVariant = LightOnSurfaceVariantHigh,
    outline = LightOutlineHigh,
    outlineVariant = LightOutlineVariantHigh,
    scrim = LightScrim,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    surfaceDim = LightSurfaceDimHigh,
    surfaceBright = LightSurfaceBrightHigh,
    surfaceContainerLowest = LightSurfaceContainerLowestHigh,
    surfaceContainerLow = LightSurfaceContainerLowHigh,
    surfaceContainer = LightSurfaceContainerHighRes,
    surfaceContainerHigh = LightSurfaceContainerHighHigh,
    surfaceContainerHighest = LightSurfaceContainerHighestHigh
)

private val DarkStandardScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest
)

private val DarkMediumContrastScheme = darkColorScheme(
    primary = DarkPrimaryMedium,
    onPrimary = DarkOnPrimaryMedium,
    primaryContainer = DarkPrimaryContainerMedium,
    onPrimaryContainer = DarkOnPrimaryContainerMedium,
    secondary = DarkSecondaryMedium,
    onSecondary = DarkOnSecondaryMedium,
    secondaryContainer = DarkSecondaryContainerMedium,
    onSecondaryContainer = DarkOnSecondaryContainerMedium,
    tertiary = DarkTertiaryMedium,
    onTertiary = DarkOnTertiaryMedium,
    tertiaryContainer = DarkTertiaryContainerMedium,
    onTertiaryContainer = DarkOnTertiaryContainerMedium,
    error = DarkErrorMedium,
    onError = DarkOnErrorMedium,
    errorContainer = DarkErrorContainerMedium,
    onErrorContainer = DarkOnErrorContainerMedium,
    background = DarkBackgroundMedium,
    onBackground = DarkOnBackgroundMedium,
    surface = DarkSurfaceMedium,
    onSurface = DarkOnSurfaceMedium,
    surfaceVariant = DarkSurfaceVariantMedium,
    onSurfaceVariant = DarkOnSurfaceVariantMedium,
    outline = DarkOutlineMedium,
    outlineVariant = DarkOutlineVariantMedium,
    scrim = DarkScrim,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    surfaceDim = DarkSurfaceDimMedium,
    surfaceBright = DarkSurfaceBrightMedium,
    surfaceContainerLowest = DarkSurfaceContainerLowestMedium,
    surfaceContainerLow = DarkSurfaceContainerLowMedium,
    surfaceContainer = DarkSurfaceContainerMediumRes,
    surfaceContainerHigh = DarkSurfaceContainerHighMedium,
    surfaceContainerHighest = DarkSurfaceContainerHighestMedium
)

private val DarkHighContrastScheme: ColorScheme
    get() = darkColorScheme(
        primary = DarkPrimaryHigh,
        onPrimary = DarkOnPrimaryHigh,
        primaryContainer = DarkPrimaryContainerHigh,
        onPrimaryContainer = DarkOnPrimaryContainerHigh,
        secondary = DarkSecondaryHigh,
        onSecondary = DarkOnSecondaryHigh,
        secondaryContainer = DarkSecondaryContainerHigh,
        onSecondaryContainer = DarkOnSecondaryContainerHigh,
        tertiary = DarkTertiaryHigh,
        onTertiary = DarkOnTertiaryHigh,
        tertiaryContainer = DarkTertiaryContainerHigh,
        onTertiaryContainer = DarkOnTertiaryContainerHigh,
        error = DarkErrorHigh,
        onError = DarkOnErrorHigh,
        errorContainer = DarkErrorContainerHigh,
        onErrorContainer = DarkOnErrorContainerHigh,
        background = DarkBackgroundHigh,
        onBackground = DarkOnBackgroundHigh,
        surface = DarkSurfaceHigh,
        onSurface = DarkOnSurfaceHigh,
        surfaceVariant = DarkSurfaceVariantHigh,
        onSurfaceVariant = DarkOnSurfaceVariantHigh,
        outline = DarkOutlineHigh,
        outlineVariant = DarkOutlineVariantHigh,
        scrim = DarkScrim,
        inverseSurface = DarkInverseSurface,
        inverseOnSurface = DarkInverseOnSurface,
        inversePrimary = DarkInversePrimary,
        surfaceDim = DarkSurfaceDimHigh,
        surfaceBright = DarkSurfaceBrightHigh,
        surfaceContainerLowest = DarkSurfaceContainerLowestHigh,
        surfaceContainerLow = DarkSurfaceContainerLowHigh,
        surfaceContainer = DarkSurfaceContainerHighRes,
        surfaceContainerHigh = DarkSurfaceContainerHighHigh,
        surfaceContainerHighest = DarkSurfaceContainerHighestHigh
    )

private fun ColorScheme.withPureBlack(style: PureBlackStyle): ColorScheme {
    if (style == PureBlackStyle.DEFAULT) return this
    return when (style) {
        PureBlackStyle.ECONOMY -> this.copy(
            background = PureBlack
        )

        PureBlackStyle.BUSINESS_CLASS -> this.copy(
            background = PureBlack,
            surface = PureBlack,
            surfaceVariant = PureBlack,
            surfaceContainer = PureBlack
        )

        PureBlackStyle.FIRST_CLASS -> this.copy(
            background = PureBlack,
            surface = PureBlack,
            surfaceVariant = PureBlack,
            surfaceContainer = PureBlack,
            surfaceContainerLow = PureBlack,
            surfaceContainerHigh = PureBlack,
            surfaceContainerHighest = PureBlack,
            surfaceDim = PureBlack
        )
    }
}

// ============================================================================
// THEME COMPOSABLE EXTENSION
// ============================================================================

@Composable
fun AisleronTheme(
    applicationTheme: ApplicationTheme = ApplicationTheme.SYSTEM_THEME,
    contrast: AppContrast = AppContrast.STANDARD,
    dynamicColor: Boolean = false,
    pureBlackStyle: PureBlackStyle = PureBlackStyle.DEFAULT,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = (applicationTheme == ApplicationTheme.DARK_THEME)
            || (applicationTheme == ApplicationTheme.SYSTEM_THEME && isSystemInDarkTheme())

    val colorScheme = when {
        // 1. Dynamic Wallpaper Extraction (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context).withPureBlack(pureBlackStyle)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // 2. Custom Night Scheme Layering
        darkTheme -> {
            when (contrast) {
                AppContrast.STANDARD -> DarkStandardScheme
                AppContrast.MEDIUM -> DarkMediumContrastScheme
                AppContrast.HIGH -> DarkHighContrastScheme
            }.withPureBlack(pureBlackStyle)
        }
        // 3. Custom Day Scheme Layering
        else -> {
            when (contrast) {
                AppContrast.STANDARD -> LightStandardScheme
                AppContrast.MEDIUM -> LightMediumContrastScheme
                AppContrast.HIGH -> LightHighContrastScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}