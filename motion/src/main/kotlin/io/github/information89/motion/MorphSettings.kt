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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.LayoutDirection

/**
 * How a [SpringMorph] behaves, as one setting (agreed 2026-11-21: morph behaviour is configured in
 * Motion, not hand-made in the app; the seed of the Page and Sheet kinds).
 */
@Immutable
data class MorphSettings(
    /**
     * When touches reach the page below while it moves. By default only while closing (2026-11-21: with
     * the moon card opening or settling, a scroll went to the page underneath).
     */
    val touches: MorphTouches = MorphTouches.WhileClosing,
    /**
     * The page its source lives on, if any: the morph then belongs to that page, moving 1:1 with it and,
     * while closing, clipped to it, so it folds home inside its own page and never draws over another.
     */
    val page: MorphPage? = null,
    /**
     * Whether a touch presses it like a button (it sinks and springs back with a bounce). Off by
     * default (2026-11-22, the user: cards bounced on quick taps like buttons); buttons opt in.
     */
    val pressable: Boolean = false,
)

/**
 * Page [index] of [pager], for a morph that belongs to it ([MorphSettings.page]). Its movement comes from
 * the pager itself (page size, spacing, orientation, reversed layout, right-to-left, and [overshoot] past
 * an end), never from screen sizes.
 */
@Stable
class MorphPage(
    private val pager: PagerState,
    private val index: Int,
    private val overshoot: PageOvershoot? = null,
) {
    /** How far the page is from where it rests (when it's the pager's current page), in pixels, as drawn. */
    fun offset(layoutDirection: LayoutDirection): Offset {
        val info = pager.layoutInfo
        val step = (info.pageSize + info.pageSpacing).toFloat()
        var along = (index - (pager.currentPage + pager.currentPageOffsetFraction)) * step
        if (info.reverseLayout) along = -along
        // The bounce past an end is drawn as a shift of all the pages (pageOvershoot).
        along -= overshoot?.pixels ?: 0f
        return if (info.orientation == Orientation.Vertical) {
            Offset(0f, along)
        } else {
            Offset(if (layoutDirection == LayoutDirection.Rtl) -along else along, 0f)
        }
    }
}
