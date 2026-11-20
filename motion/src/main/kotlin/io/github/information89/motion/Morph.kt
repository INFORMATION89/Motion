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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
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

/** Where anchor [key] is in the current [MorphHost] (null before it's laid out). */
@Composable
fun morphAnchorOf(key: Any): MorphAnchor? = LocalMorphRegistry.current?.get(key)

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
 * With [reduced] (e.g. "Remove animations"), there's no travel or scaling: the surface sits at the
 * target and only its alpha follows [value]. Compose finishes animations instantly at that setting,
 * so in practice it appears at once (no fade).
 */
@Composable
fun Morph(
    from: Any,
    to: Any,
    value: MotionValue,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    shape: ((frame: MorphFrame, p: Float) -> Shape)? = null,
    surface: Modifier = Modifier,
    reduced: Boolean = rememberReducedMotion(),
    content: @Composable () -> Unit,
) {
    val registry = LocalMorphRegistry.current ?: return
    val target = registry[to] ?: return
    Morph(from, target, value, modifier, contentAlignment, shape, surface, reduced, content)
}

/**
 * The same, with the target's geometry given directly (in the host's coordinates) instead of
 * measured: use it when the target is known before it's laid out (a sheet docked at the bottom), so
 * the morph draws from its very first frame.
 */
@Composable
fun Morph(
    from: Any,
    target: MorphAnchor,
    value: MotionValue,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    shape: ((frame: MorphFrame, p: Float) -> Shape)? = null,
    surface: Modifier = Modifier,
    reduced: Boolean = rememberReducedMotion(),
    content: @Composable () -> Unit,
) {
    val registry = LocalMorphRegistry.current ?: return
    val source = registry[from] ?: return
    fun frame() = if (reduced) {
        MorphFrame(target.bounds, target.cornerRadius)
    } else {
        MorphGeometry.frame(source, target, value.value)
    }
    val scope = remember(target, value) { MorphContentScope(target, value) }
    // Reuses the shape while the radius doesn't change (no allocation per frame at rest).
    val roundShape = remember { MorphRoundShapes() }
    CompositionLocalProvider(LocalMorphContent provides scope) {
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
                    val f = frame()
                    this.shape = shape?.invoke(f, value.value) ?: roundShape.at(f.cornerRadius)
                    clip = true
                    if (reduced) alpha = value.value.coerceIn(0f, 1f)
                }
                // The surface's own look (glass, background, edge), at its current size.
                .then(surface)
                // 3. Content keeps the target's size the whole time: measured once, never re-measured,
                // and placed in the moving surface by [contentAlignment].
                .layout { measurable, constraints ->
                    val tw = target.bounds.width.roundToInt().coerceAtLeast(0)
                    val th = target.bounds.height.roundToInt().coerceAtLeast(0)
                    val placeable = measurable.measure(Constraints.fixed(tw, th))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        val offset = contentAlignment.align(
                            IntSize(tw, th),
                            IntSize(constraints.maxWidth, constraints.maxHeight),
                            layoutDirection
                        )
                        placeable.place(offset)
                    }
                }
                .onPlaced { scope.root = it }
        ) { content() }
    }
}

/** What content inside a [Morph] needs to know for [morphFrom]. */
@Stable
class MorphContentScope internal constructor(val target: MorphAnchor, val value: MotionValue) {
    internal var root: LayoutCoordinates? = null
}

private val LocalMorphContent = staticCompositionLocalOf<MorphContentScope?> { null }

/** A rounded rectangle with a fixed radius. */
private data class MorphRoundShape(val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rounded(
            RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius.coerceAtMost(size.minDimension / 2f)))
        )
}

/**
 * Hands out [MorphRoundShape]s, reusing the last one while the radius is the same: a new object when
 * the radius changes, so the layer always notices (an object changed in place might not be).
 */
private class MorphRoundShapes {
    private var last = MorphRoundShape(0f)
    fun at(radius: Float): Shape {
        if (last.radius != radius) last = MorphRoundShape(radius)
        return last
    }
}

/**
 * Inside a [Morph]'s content: this element travels from anchor [from] (e.g. a button's label) to its
 * own resting place in the target, scaling with it, as the morph's value goes 0 → 1. Text morphs
 * (label into heading) use it; the element is laid out once at its final size.
 */
@Composable
fun Modifier.morphFrom(from: Any): Modifier {
    val registry = LocalMorphRegistry.current ?: return this
    val scope = LocalMorphContent.current ?: return this
    val placed = remember { arrayOfNulls<LayoutCoordinates>(1) }
    return this
        .onPlaced { placed[0] = it }
        .graphicsLayer {
            val source = registry[from] ?: return@graphicsLayer
            val host = registry.host?.takeIf { it.isAttached } ?: return@graphicsLayer
            val me = placed[0]?.takeIf { it.isAttached } ?: return@graphicsLayer
            val root = scope.root?.takeIf { it.isAttached } ?: return@graphicsLayer
            val p = scope.value.value
            // Where it sits now (moving with the surface), and where it rests in the target.
            val now = host.localPositionOf(me, Offset.Zero)
            val inContent = root.localPositionOf(me, Offset.Zero)
            val rest = scope.target.bounds.topLeft + inContent
            val h = me.size.height.toFloat().coerceAtLeast(1f)
            val left = lerp(source.bounds.left, rest.x, p)
            val top = lerp(source.bounds.top, rest.y, p)
            val k = lerp(source.bounds.height / h, 1f, p)
            transformOrigin = TransformOrigin(0f, 0f)
            translationX = left - now.x
            translationY = top - now.y
            scaleX = k
            scaleY = k
        }
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
