# :motion-ui

Motion UI is the optional Compose component layer built on Motion Core. It is intended for reusable, configurable UI building blocks, not app screens or a replacement for Material 3.

## Current API — MOT0.3A

The first delivery provides a theme entry point and a small semantic token contract:

- `MotionUiTheme` — provides a Material 3 theme and Motion UI token scope. Defaults follow system light/dark appearance.
- `MotionUiThemeTokens.current` — reads the tokens from the nearest Motion UI theme.
- `MotionUiTokens` — groups component shape, spacing, and material roles.
- `MotionUiShapes` — default card and sheet shapes.
- `MotionUiSpacing` — compact, medium, and large spacing values.
- `MotionUiMaterials` — card and glass surface colors plus card elevation.

Example:

```kotlin
MotionUiTheme(
    colorScheme = appColorScheme,
    typography = appTypography,
    shapes = appShapes,
    tokens = MotionUiTokens(
        shapes = MotionUiShapes(),
        spacing = MotionUiSpacing(medium = 20.dp),
        materials = MotionUiMaterials(
            cardContainer = appCardColor,
            glassContainer = appGlassTint,
            cardElevation = 2.dp
        )
    )
) {
    AppContent()
}
```

When no custom `tokens` are supplied, neutral surface values are derived from the provided Material 3 `ColorScheme`. Apps remain responsible for their actual brand colors, typography, and visual identity.

Read the token values inside Compose:

```kotlin
val tokens = MotionUiThemeTokens.current
val cardShape = tokens.shapes.card
val cardColor = tokens.materials.cardContainer
```

## Boundaries

- `:motion-ui` depends on `:motion`; Core never depends on UI.
- Material 3 remains responsible for standard controls, typography, and semantics.
- No consumer-specific or third-party application code, resources, assets, or branding are included.
- No Haze dependency or blur implementation is included in MOT0.3A.
- The module currently targets Android / Jetpack Compose only.

The API is intentionally small. Card, page-frame, sheet, and glass components are not part of this initial foundation.
