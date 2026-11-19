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

import kotlin.math.abs

/**
 * The maths behind gestures, after UIKit (see Glasmio's notes, `ios-sheet-motion.md`). Pure
 * functions, no Compose, so they're unit-tested.
 */
object MotionPhysics {
    /** UIKit's resistance coefficient for over-scrolling. */
    const val RUBBER_BAND = 0.55f

    /** UIKit's `.normal` scroll deceleration (per millisecond). */
    const val DECELERATION_NORMAL = 0.998f

    /** UIKit's `.fast` deceleration, used for sheets. */
    const val DECELERATION_FAST = 0.99f

    /**
     * How far something moves when pulled [over] past its limit: close to the finger at first, then
     * ever more resistant, never passing [dimension] (the most it may travel past the edge).
     */
    fun rubberBand(over: Float, dimension: Float, coefficient: Float = RUBBER_BAND): Float {
        if (over <= 0f || dimension <= 0f) return 0f
        return (1f - 1f / (over * coefficient / dimension + 1f)) * dimension
    }

    /**
     * Applies rubber-banding to a raw (finger) position: inside [min]..[max] it's unchanged; past
     * an edge it resists, travelling at most [overMin] below [min] and [overMax] above [max].
     */
    fun resist(raw: Float, min: Float, max: Float, overMin: Float, overMax: Float): Float = when {
        raw > max -> max + rubberBand(raw - max, overMax)
        raw < min -> min - rubberBand(min - raw, overMin)
        else -> raw
    }

    /**
     * The inverse of [rubberBand]: how far the finger is past the limit when the element shows
     * [shown] past it. Shown distances at or beyond [dimension] can't be reached; they map to a
     * very large pull.
     */
    fun unRubberBand(shown: Float, dimension: Float, coefficient: Float = RUBBER_BAND): Float {
        if (shown <= 0f || dimension <= 0f) return 0f
        val f = (shown / dimension).coerceAtMost(0.999f)
        return shown / (coefficient * (1f - f))
    }

    /** The inverse of [resist]: the raw finger position that shows as [shown]. */
    fun unresist(shown: Float, min: Float, max: Float, overMin: Float, overMax: Float): Float = when {
        shown > max -> max + unRubberBand(shown - max, overMax)
        shown < min -> min - unRubberBand(min - shown, overMin)
        else -> shown
    }

    /**
     * How much of the finger's motion still shows past an edge (1 inside the range, smaller the
     * further out). Release velocity is scaled by this so a release from an over-pull doesn't
     * shoot off.
     */
    fun resistance(raw: Float, min: Float, max: Float, overMin: Float, overMax: Float): Float {
        val over = when {
            raw > max -> raw - max
            raw < min -> min - raw
            else -> return 1f
        }
        val shown = abs(resist(raw, min, max, overMin, overMax) - if (raw > max) max else min)
        return (shown / over).coerceIn(0f, 1f)
    }

    /**
     * Where a fling would come to rest, given a [velocity] in units per second and a per-millisecond
     * [deceleration] (UIKit's `decelerationRate`).
     */
    fun project(value: Float, velocity: Float, deceleration: Float = DECELERATION_NORMAL): Float =
        value + velocity / 1000f * deceleration / (1f - deceleration)

    /**
     * Picks the rest point a release goes to:
     * - releases slower than [slowFlick] count as no fling (a slow lift doesn't bounce);
     * - otherwise the rest point nearest to where the fling would end ([project]);
     * - a fling faster than [fastFlick] that would land back where it started moves one step in
     *   its direction.
     * Velocities and thresholds are in the same units per second as [position].
     */
    fun chooseTarget(
        position: Float,
        velocity: Float,
        restPoints: List<Float>,
        slowFlick: Float,
        fastFlick: Float,
        deceleration: Float = DECELERATION_NORMAL,
    ): Float {
        require(restPoints.isNotEmpty()) { "restPoints must not be empty" }
        val points = restPoints.sorted()
        val v = if (abs(velocity) < slowFlick) 0f else velocity
        val nearestNow = points.minBy { abs(it - position) }
        val target = points.minBy { abs(it - project(position, v, deceleration)) }
        if (abs(v) > fastFlick && target == nearestNow) {
            val next = if (v > 0f) points.firstOrNull { it > nearestNow } else points.lastOrNull { it < nearestNow }
            if (next != null) return next
        }
        return target
    }
}
