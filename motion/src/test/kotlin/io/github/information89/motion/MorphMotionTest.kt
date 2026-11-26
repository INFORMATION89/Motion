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

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Rect
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows

private class MorphStepClock : MonotonicFrameClock {
    private var time = 0L
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        yield()
        time += 16_000_000L
        return onFrame(time)
    }
}

private fun morphMotionTest(block: suspend CoroutineScope.() -> Unit) = runBlocking(MorphStepClock()) {
    block()
    coroutineContext.cancelChildren()
}

class MorphMotionTest {

    @Test
    fun `progress includes position when dimensions do not change`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = MorphAnchor(Rect(0f, 0f, 100f, 100f), 20f)
        motion.target = MorphAnchor(Rect(100f, 0f, 200f, 100f), 20f)

        motion.scrub(0.5f)

        motion.bounds.center.x shouldBe (100f plusOrMinus 0.001f)
        motion.progress shouldBe (0.5f plusOrMinus 0.001f)
    }

    @Test
    fun `progress includes vertical position and size`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = MorphAnchor(Rect(0f, 0f, 100f, 100f), 20f)
        motion.target = MorphAnchor(Rect(0f, 100f, 200f, 300f), 40f)

        motion.scrub(0.25f)

        motion.progress shouldBe (0.25f plusOrMinus 0.001f)
    }
    @Test
    fun `drag rejects non-finite deltas without changing geometry`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = MorphAnchor(Rect(0f, 0f, 100f, 100f), 20f)
        motion.target = MorphAnchor(Rect(100f, 0f, 200f, 100f), 20f)
        motion.scrub(0.4f)
        val before = motion.bounds

        assertThrows(IllegalArgumentException::class.java) { motion.drag(Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { motion.drag(Float.POSITIVE_INFINITY) }
        assertThrows(IllegalArgumentException::class.java) { motion.drag(Float.NEGATIVE_INFINITY) }

        motion.bounds shouldBe before
    }

    @Test
    fun `scrub rejects non-finite progress without changing geometry`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = MorphAnchor(Rect(0f, 0f, 100f, 100f), 20f)
        motion.target = MorphAnchor(Rect(100f, 0f, 200f, 100f), 20f)
        motion.scrub(0.4f)
        val before = motion.bounds

        assertThrows(IllegalArgumentException::class.java) { motion.scrub(Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { motion.scrub(Float.POSITIVE_INFINITY) }
        assertThrows(IllegalArgumentException::class.java) { motion.scrub(Float.NEGATIVE_INFINITY) }

        motion.bounds shouldBe before
    }

    @Test
    fun `release rejects invalid velocity and deceleration`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = MorphAnchor(Rect(0f, 0f, 100f, 100f), 20f)
        motion.target = MorphAnchor(Rect(100f, 0f, 200f, 100f), 20f)
        motion.scrub(0.4f)
        val before = motion.bounds

        assertThrows(IllegalArgumentException::class.java) { motion.release(Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(Float.POSITIVE_INFINITY) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, 0f) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, 1f) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, Float.POSITIVE_INFINITY) }

        motion.bounds shouldBe before
    }

}
