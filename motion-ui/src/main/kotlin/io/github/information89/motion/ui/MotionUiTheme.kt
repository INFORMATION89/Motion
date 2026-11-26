/*
 * Copyright 2026 INFORMATION89
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.information89.motion.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Semantic component tokens owned by Motion UI and configured by the consuming application.
 *
 * These are intentionally limited to values needed by the first shared surfaces. They are not a
 * replacement for the app's design system or Material 3 theme.
 */
@Immutable
data class MotionUiTokens(
    val shapes: MotionUiShapes = MotionUiShapes(),
    val spacing: MotionUiSpacing = MotionUiSpacing(),
    val materials: MotionUiMaterials
) {
    companion object {
        /** Creates neutral defaults derived from the supplied Material color scheme. */
        fun defaults(colorScheme: ColorScheme): MotionUiTokens = MotionUiTokens(
            materials = MotionUiMaterials(
                cardContainer = colorScheme.surfaceContainerLow,
                glassContainer = colorScheme.surface.copy(alpha = 0.82f)
            )
        )
    }
}

/** Shape roles used by Motion UI building blocks. Apps may replace any role. */
@Immutable
data class MotionUiShapes(
    val card: Shape = RoundedCornerShape(24.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
)

/** A deliberately small spacing scale for shared component layouts. */
@Immutable
data class MotionUiSpacing(
    val compact: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp
)

/** Surface material roles; actual brand colors and opacity remain app-owned. */
@Immutable
data class MotionUiMaterials(
    val cardContainer: Color,
    val glassContainer: Color,
    val cardElevation: Dp = 1.dp
)

private val LocalMotionUiTokens = staticCompositionLocalOf { MotionUiTokens.defaults(lightColorScheme()) }

/**
 * Entry point for Motion UI components.
 *
 * Material 3 remains the source of standard controls, typography, and semantic color roles.
 * Consumers can supply their own Material theme and override Motion-specific component tokens.
 */
@Composable
fun MotionUiTheme(
    colorScheme: ColorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
    typography: Typography = Typography(),
    shapes: Shapes = Shapes(),
    tokens: MotionUiTokens = MotionUiTokens.defaults(colorScheme),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalMotionUiTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

/** Access the semantic tokens supplied to the nearest [MotionUiTheme]. */
object MotionUiThemeTokens {
    val current: MotionUiTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalMotionUiTokens.current
}
