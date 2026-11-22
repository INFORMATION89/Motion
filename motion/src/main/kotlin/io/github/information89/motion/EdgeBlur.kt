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

import android.os.Build
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * One edge's treatment (F2): within [size] of the edge, the content is blurred progressively (up to
 * [blur] at the very edge; Android 12+) and faded, either into [fadeTo] (a colour, e.g. the page
 * behind, up to [fadeAlpha] over [fadeLength] of the band) or, with [fadeOut], to transparent (it
 * then really disappears, for content over anything). A band with no blur and no fade does nothing.
 */
@Immutable
data class EdgeBand(
    val size: Dp,
    val blur: Dp = 0.dp,
    val fadeTo: Color? = null,
    val fadeAlpha: Float = 1f,
    val fadeLength: Float = 1f,
    val fadeOut: Boolean = false,
)

private enum class Side { Top, Bottom, Start, End }

/**
 * A progressive edge blur and fade on any edges (F2, agreed 2026-11-21): top and bottom for lists,
 * start and end for dials and carousels. Content near an edge blurs more and fades, so it never ends
 * in a hard cut and it hints that there's more. Only the edge bands are blurred, each with one blur
 * effect set once. [strength] (read in the draw phase, 0..1) fades the whole treatment in or out,
 * e.g. while the page under it comes in; each edge also has its own ([topStrength]…), e.g. only while
 * content is scrolled under it ([scrollEdgeBlur]).
 */
@Composable
fun Modifier.edgeBlur(
    top: EdgeBand? = null,
    bottom: EdgeBand? = null,
    start: EdgeBand? = null,
    end: EdgeBand? = null,
    strength: () -> Float = { 1f },
    topStrength: () -> Float = { 1f },
    bottomStrength: () -> Float = { 1f },
    startStrength: () -> Float = { 1f },
    endStrength: () -> Float = { 1f },
): Modifier {
    val sideStrength = listOf(topStrength, bottomStrength, startStrength, endStrength)
    val content = rememberGraphicsLayer()
    val layers =
        listOf(rememberGraphicsLayer(), rememberGraphicsLayer(), rememberGraphicsLayer(), rememberGraphicsLayer())
    val bands = listOf(Side.Top to top, Side.Bottom to bottom, Side.Start to start, Side.End to end)
        .mapNotNull { (side, band) -> band?.let { side to it } }
    val blurs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && bands.any { it.second.blur.value > 0f }
    val fadesOut = bands.any { it.second.fadeOut }
    return this
        // Fading to transparent needs its own layer to cut into.
        .then(
            if (fadesOut) {
                Modifier.graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
            } else {
                Modifier
            }
        )
        .drawWithContent {
            val all = strength().coerceIn(0f, 1f)
            fun kOf(side: Side) = all * sideStrength[side.ordinal]().coerceIn(0f, 1f)
            if (blurs && bands.any { kOf(it.first) > 0f }) {
                content.record { this@drawWithContent.drawContent() }
                drawLayer(content)
                bands.forEach { (side, band) ->
                    val k = kOf(side)
                    if (band.blur.value > 0f && k > 0f) {
                        val layer = layers[side.ordinal]
                        layer.alpha = k
                        blurBand(layer, content, side, band.size.toPx(), band.blur.toPx())
                    }
                }
            } else {
                drawContent()
            }
            bands.forEach { (side, band) ->
                val k = kOf(side)
                val length = band.size.toPx() * band.fadeLength.coerceIn(0f, 1f)
                if (band.fadeOut) {
                    drawRect(
                        fadeBrush(side, length, Color.Transparent, Color.Black, k),
                        topLeft(side, length),
                        bandSize(side, length),
                        blendMode = BlendMode.DstIn
                    )
                }
                val colour = band.fadeTo
                if (colour != null) {
                    val edge = colour.copy(alpha = colour.alpha * band.fadeAlpha * k)
                    drawRect(
                        fadeBrush(side, length, edge, colour.copy(alpha = 0f), 1f),
                        topLeft(side, length),
                        bandSize(side, length)
                    )
                }
            }
        }
}

private fun DrawScope.topLeft(side: Side, length: Float): Offset = when (side) {
    Side.Top, Side.Start -> Offset.Zero
    Side.Bottom -> Offset(0f, size.height - length)
    Side.End -> Offset(size.width - length, 0f)
}

private fun DrawScope.bandSize(side: Side, length: Float): Size = when (side) {
    Side.Top, Side.Bottom -> Size(size.width, length)
    Side.Start, Side.End -> Size(length, size.height)
}

/** A gradient across the band, [atEdge] at the edge to [inward] inside; [k] blends a mask towards none. */
private fun DrawScope.fadeBrush(side: Side, length: Float, atEdge: Color, inward: Color, k: Float): Brush {
    val edge = if (k < 1f) androidx.compose.ui.graphics.lerp(inward, atEdge, k) else atEdge
    return when (side) {
        Side.Top -> Brush.verticalGradient(0f to edge, 1f to inward, startY = 0f, endY = length)
        Side.Bottom -> Brush.verticalGradient(
            0f to inward,
            1f to edge,
            startY = size.height - length,
            endY = size.height
        )
        Side.Start -> Brush.horizontalGradient(0f to edge, 1f to inward, startX = 0f, endX = length)
        Side.End -> Brush.horizontalGradient(0f to inward, 1f to edge, startX = size.width - length, endX = size.width)
    }
}

/** A blurred copy of [content]'s band on [side], fading from full blur at the edge to none inward. */
private fun DrawScope.blurBand(layer: GraphicsLayer, content: GraphicsLayer, side: Side, length: Float, blurPx: Float) {
    // Set once: a new RenderEffect every frame would make the GPU rebuild the blur each time.
    if (layer.renderEffect == null) layer.renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
    val origin = topLeft(side, length)
    val area = bandSize(side, length)
    layer.record(IntSize(area.width.toInt().coerceAtLeast(1), area.height.toInt().coerceAtLeast(1))) {
        translate(-origin.x, -origin.y) { drawLayer(content) }
        val mask = when (side) {
            Side.Top -> Brush.verticalGradient(0f to Color.Black, 1f to Color.Transparent)
            Side.Bottom -> Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black)
            Side.Start -> Brush.horizontalGradient(0f to Color.Black, 1f to Color.Transparent)
            Side.End -> Brush.horizontalGradient(0f to Color.Transparent, 1f to Color.Black)
        }
        drawRect(mask, blendMode = BlendMode.DstIn)
    }
    translate(origin.x, origin.y) { drawLayer(layer) }
}

/**
 * The edge look agreed for Solstice (F2, 2026-11-21), as defaults: a progressive blur and a fade, the
 * band as tall as what sits over that edge (measured by the app: status bar, a header, a toolbar).
 */
object EdgeBlurDefaults {
    /** The blur at the very edge. */
    val Blur: Dp = 18.dp

    /** How strongly a band fades into the colour behind (at the edge). */
    const val FADE_ALPHA = 0.85f

    /** How much of a top band the fade covers. */
    const val TOP_FADE_LENGTH = 0.8f

    /** How much of a bottom band the fade covers. */
    const val BOTTOM_FADE_LENGTH = 0.7f

    /**
     * A page's top edge under [size] of chrome: fading into [fadeTo] (a plain page), or to transparent
     * when it's null (over a sky or anything else).
     */
    fun top(size: Dp, fadeTo: Color?): EdgeBand = band(size, fadeTo, TOP_FADE_LENGTH)

    /** A page's bottom edge under [size] of chrome (e.g. a toolbar); see [top]. */
    fun bottom(size: Dp, fadeTo: Color?): EdgeBand = band(size, fadeTo, BOTTOM_FADE_LENGTH)

    private fun band(size: Dp, fadeTo: Color?, fadeLength: Float) = EdgeBand(
        size = size,
        blur = Blur,
        fadeTo = fadeTo,
        fadeAlpha = FADE_ALPHA,
        fadeLength = fadeLength,
        fadeOut = fadeTo == null
    )
}

/**
 * [edgeBlur] for a scrolling column: each edge shows only while content is scrolled under it, coming in
 * over its band's height (agreed 2026-11-21).
 */
@Composable
fun Modifier.scrollEdgeBlur(
    scroll: ScrollState,
    top: EdgeBand?,
    bottom: EdgeBand?,
    strength: () -> Float = { 1f },
): Modifier {
    val density = LocalDensity.current
    fun under(px: Int, band: EdgeBand?) = band?.let { px / with(density) { it.size.toPx() }.coerceAtLeast(1f) } ?: 0f
    return edgeBlur(
        top = top,
        bottom = bottom,
        strength = strength,
        topStrength = { under(scroll.value, top) },
        bottomStrength = { under(scroll.maxValue - scroll.value, bottom) }
    )
}

/** [scrollEdgeBlur] for a lazy list (reversed layouts too). */
@Composable
fun Modifier.scrollEdgeBlur(
    list: LazyListState,
    top: EdgeBand?,
    bottom: EdgeBand?,
    strength: () -> Float = { 1f },
): Modifier {
    val density = LocalDensity.current
    fun px(band: EdgeBand?) = band?.let { with(density) { it.size.toPx() }.coerceAtLeast(1f) } ?: 1f

    // How far content runs past the list's start and end, in pixels (capped: only the band matters).
    fun beforeStart(): Float = if (list.firstVisibleItemIndex >
        0
    ) {
        Float.MAX_VALUE
    } else {
        list.firstVisibleItemScrollOffset.toFloat()
    }
    fun afterEnd(): Float {
        val info = list.layoutInfo
        val last = info.visibleItemsInfo.lastOrNull() ?: return 0f
        if (last.index < info.totalItemsCount - 1) return Float.MAX_VALUE
        return (last.offset + last.size + info.afterContentPadding - info.viewportEndOffset).toFloat()
    }
    fun reversed() = list.layoutInfo.reverseLayout
    return edgeBlur(
        top = top,
        bottom = bottom,
        strength = strength,
        topStrength = { (if (reversed()) afterEnd() else beforeStart()) / px(top) },
        bottomStrength = { (if (reversed()) beforeStart() else afterEnd()) / px(bottom) }
    )
}
