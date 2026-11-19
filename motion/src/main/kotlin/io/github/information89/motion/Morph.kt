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

package io.github.information89.motion

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

/*
 * Container transforms drawn from anchors (M2): a source (a pill, a button) and a target (a sheet)
 * register their bounds; [Morph] draws one surface at `lerp(source, target, p)` for position, size
 * and corners. No shared-element seeking and no fake tweens: overshoot comes from lerping past 1,
 * and source and target stay composed (hidden by the caller) so nothing is rebuilt mid-motion.
 */

/** Where an anchor sits inside its [MorphHost], and its corner radius in pixels. */
@Immutable
data class MorphAnchor(val bounds: Rect, val cornerRadius: Float)

/** The geometry of a morph at progress [p] (unclamped: past 0 or 1 it extrapolates, for overshoot). */
@Immutable
data class MorphFrame(val bounds: Rect, val cornerRadius: Float)

object MorphGeometry {
    fun frame(source: MorphAnchor, target: MorphAnchor, p: Float): MorphFrame {
        val a = source.bounds
        val b = target.bounds
        val rect =
            Rect(lerp(a.left, b.left, p), lerp(a.top, b.top, p), lerp(a.right, b.right, p), lerp(a.bottom, b.bottom, p))
        // Corners never go negative or past half the shorter side.
        val maxRadius = minOf(rect.width, rect.height).coerceAtLeast(0f) / 2f
        val radius = lerp(source.cornerRadius, target.cornerRadius, p).coerceIn(0f, maxRadius)
        return MorphFrame(rect, radius)
    }
}

/** Anchors registered inside one [MorphHost]. */
@Stable
class MorphRegistry internal constructor() {
    internal val anchors = mutableStateMapOf<Any, MorphAnchor>()
    internal var host: LayoutCoordinates? = null

    operator fun get(key: Any): MorphAnchor? = anchors[key]
}

private val LocalMorphRegistry = staticCompositionLocalOf<MorphRegistry?> { null }

/**
 * The area morphs happen in, usually the whole screen. Anchors and [Morph]s inside it share its
 * coordinates.
 */
@Composable
fun MorphHost(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val registry = remember { MorphRegistry() }
    CompositionLocalProvider(LocalMorphRegistry provides registry) {
        Box(modifier.onGloballyPositioned { registry.host = it }) { content() }
    }
}

/** Registers this element's bounds and corner radius as morph anchor [key]. */
@Composable
fun Modifier.morphAnchor(key: Any, cornerRadius: Dp = 0.dp): Modifier {
    val registry = LocalMorphRegistry.current ?: return this
    val radiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    return this.onGloballyPositioned { coordinates ->
        val host = registry.host?.takeIf { it.isAttached } ?: return@onGloballyPositioned
        val anchor = MorphAnchor(host.localBoundingBoxOf(coordinates, clipBounds = false), radiusPx)
        if (registry.anchors[key] != anchor) registry.anchors[key] = anchor
    }
}

/**
 * Draws a surface morphing from anchor [from] to anchor [to] as [value] goes 0 → 1 (and past, for
 * overshoot). [content] is laid out once at the target's size and clipped by the moving surface,
 * so it never re-measures during the motion. Reads [value] only in layout and draw.
 *
 * With [reduced] (e.g. "Remove animations"), there's no travel or scaling: the content fades in
 * place at the target.
 */
@Composable
fun Morph(
    from: Any,
    to: Any,
    value: MotionValue,
    modifier: Modifier = Modifier,
    reduced: Boolean = rememberReducedMotion(),
    content: @Composable () -> Unit,
) {
    val registry = LocalMorphRegistry.current ?: return
    val source = registry[from] ?: return
    val target = registry[to] ?: return
    fun frame() = if (reduced) {
        MorphFrame(target.bounds, target.cornerRadius)
    } else {
        MorphGeometry.frame(source, target, value.value)
    }
    Box(
        modifier
            // 1. Size and place the surface at this frame's geometry, inside the host.
            .layout { measurable, constraints ->
                val f = frame()
                val w = f.bounds.width.roundToInt().coerceAtLeast(0)
                val h = f.bounds.height.roundToInt().coerceAtLeast(0)
                val placeable = measurable.measure(Constraints.fixed(w, h))
                val hostW = if (constraints.hasBoundedWidth) constraints.maxWidth else w
                val hostH = if (constraints.hasBoundedHeight) constraints.maxHeight else h
                layout(hostW, hostH) { placeable.place(f.bounds.left.roundToInt(), f.bounds.top.roundToInt()) }
            }
            // 2. Clip to the surface's corners at its current size.
            .graphicsLayer {
                shape = RoundedCornerShape(frame().cornerRadius)
                clip = true
                if (reduced) alpha = value.value.coerceIn(0f, 1f)
            }
            // 3. Content keeps the target's size the whole time: measured once, never re-measured.
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints.fixed(
                        target.bounds.width.roundToInt().coerceAtLeast(0),
                        target.bounds.height.roundToInt().coerceAtLeast(0)
                    )
                )
                layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, 0) }
            }
    ) { content() }
}

/**
 * Choreography: fades and scales this element in through [from]..[to] of [value] (e.g. content at
 * 0.35…0.70). Closing plays it backwards, so content leaves first without extra code.
 */
fun Modifier.reveal(value: MotionValue, from: Float, to: Float, startScale: Float = 0.92f): Modifier =
    graphicsLayer {
        val t = value.window(from, to)
        alpha = t
        val s = lerp(startScale, 1f, t)
        scaleX = s
        scaleY = s
    }

/** Choreography: the source's own content (a label) fading out through [from]..[to] of [value]. */
fun Modifier.handOff(value: MotionValue, from: Float, to: Float): Modifier =
    graphicsLayer { alpha = 1f - value.window(from, to) }

/** True when the system's "Remove animations" (animator duration scale 0) is on. */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
