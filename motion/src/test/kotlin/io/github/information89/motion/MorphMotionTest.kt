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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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

private suspend fun advanceMorphFrames(count: Int) = repeat(count) { yield() }

private fun sourceAnchor() = MorphAnchor(Rect(0f, 0f, 100f, 100f), 20f)
private fun targetAnchor() = MorphAnchor(Rect(100f, 100f, 300f, 300f), 40f)
private fun alternateTargetAnchor() = MorphAnchor(Rect(200f, 50f, 500f, 350f), 60f)
private fun alternateSourceAnchor() = MorphAnchor(Rect(20f, 30f, 140f, 150f), 24f)

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
        val wasOpen = motion.isOpen

        assertThrows(IllegalArgumentException::class.java) { motion.release(Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(Float.POSITIVE_INFINITY) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, 0f) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, 1f) }
        assertThrows(IllegalArgumentException::class.java) { motion.release(0f, Float.POSITIVE_INFINITY) }

        motion.bounds shouldBe before
        motion.isOpen shouldBe wasOpen
    }

    @Test
    fun `immediate open then close honors the latest command`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()

        motion.open()
        motion.close()

        advanceMorphFrames(240)

        motion.isOpen shouldBe false
        motion.bounds.center.x shouldBe (50f plusOrMinus 0.01f)
        motion.bounds.center.y shouldBe (50f plusOrMinus 0.01f)
        motion.isMoving shouldBe false
        motion.isVisible shouldBe false
    }

    @Test
    fun `close then open during closing reverses toward the target`() = morphMotionTest {
        val motion = MorphMotion(this)
        val source = sourceAnchor()
        val target = targetAnchor()
        motion.source = source
        motion.target = target
        motion.open()
        advanceMorphFrames(240)

        motion.close()
        advanceMorphFrames(4)
        motion.isVisible shouldBe true
        motion.open()
        advanceMorphFrames(240)

        motion.isOpen shouldBe true
        motion.bounds.center.x shouldBe (200f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (200f plusOrMinus 0.1f)
        motion.isMoving shouldBe false
        motion.isVisible shouldBe true
    }

    @Test
    fun `repeated commands leave the last open or close intent in control`() = morphMotionTest {
        val motion = MorphMotion(this)
        val source = sourceAnchor()
        val target = targetAnchor()
        motion.source = source
        motion.target = target

        motion.open()
        advanceMorphFrames(3)
        motion.close()
        advanceMorphFrames(3)
        motion.open()
        advanceMorphFrames(240)
        motion.isOpen shouldBe true
        motion.bounds.center.x shouldBe (200f plusOrMinus 0.1f)

        motion.close()
        advanceMorphFrames(3)
        motion.open()
        advanceMorphFrames(3)
        motion.close()
        advanceMorphFrames(240)
        motion.isOpen shouldBe false
        motion.bounds.center.x shouldBe (50f plusOrMinus 0.1f)
        motion.isMoving shouldBe false
        motion.isVisible shouldBe false
    }

    @Test
    fun `target retargeting while opening is continuous and settles at latest target`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(5)
        val before = motion.bounds

        val nextTarget = alternateTargetAnchor()
        motion.updateTarget(nextTarget)
        motion.bounds shouldBe before
        advanceMorphFrames(3)
        (motion.bounds.center.x >= before.center.x) shouldBe true
        advanceMorphFrames(237)

        motion.bounds.center.x shouldBe (350f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (200f plusOrMinus 0.1f)
        motion.isMoving shouldBe false
    }

    @Test
    fun `target retargeting while closed is deferred until opening`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.close()
        advanceMorphFrames(240)
        val before = motion.bounds

        val nextTarget = alternateTargetAnchor()
        motion.updateTarget(nextTarget)
        motion.bounds shouldBe before
        motion.open()
        advanceMorphFrames(240)

        motion.bounds.center.x shouldBe (350f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (200f plusOrMinus 0.1f)
    }

    @Test
    fun `source retargeting while closing preserves velocity and settles at latest source`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        motion.close()
        advanceMorphFrames(5)
        val previous = motion.bounds
        advanceMorphFrames(3)
        val before = motion.bounds
        val directionBeforeRetarget = before.center.x - previous.center.x

        motion.updateSource(alternateSourceAnchor())
        motion.bounds shouldBe before
        advanceMorphFrames(3)
        val afterRetarget = motion.bounds
        if (directionBeforeRetarget > 0f) {
            (afterRetarget.center.x >= before.center.x) shouldBe true
        } else if (directionBeforeRetarget < 0f) {
            (afterRetarget.center.x <= before.center.x) shouldBe true
        }
        advanceMorphFrames(234)

        motion.bounds.center.x shouldBe (80f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (90f plusOrMinus 0.1f)
        motion.isMoving shouldBe false
    }

    @Test
    fun `source retargeting while open is deferred until closing`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        val before = motion.bounds

        motion.updateSource(alternateSourceAnchor())
        motion.bounds shouldBe before
        motion.close()
        advanceMorphFrames(240)

        motion.bounds.center.x shouldBe (80f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (90f plusOrMinus 0.1f)
    }

    @Test
    fun `scrub supersedes an in-flight close and open resumes from the gesture position`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        motion.close()
        advanceMorphFrames(4)

        motion.scrub(0.4f)
        advanceMorphFrames(12)
        motion.bounds.center.x shouldBe (110f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (110f plusOrMinus 0.1f)
        motion.open()
        advanceMorphFrames(240)

        motion.isOpen shouldBe true
        motion.bounds.center.x shouldBe (200f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (200f plusOrMinus 0.1f)
    }

    @Test
    fun `caught morph remains visible and stationary until resumed`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(5)
        motion.catch()
        advanceMorphFrames(40)
        val caughtBounds = motion.bounds

        advanceMorphFrames(20)
        motion.bounds shouldBe caughtBounds
        motion.isVisible shouldBe true
        motion.isMoving shouldBe false

        motion.open()
        advanceMorphFrames(240)
        motion.bounds.center.x shouldBe (200f plusOrMinus 0.1f)
        motion.isVisible shouldBe true
    }

    @Test
    fun `release after scrub selects endpoint from projected landing`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.scrub(0.4f)
        motion.release(0f)
        advanceMorphFrames(240)

        motion.isOpen shouldBe false
        motion.bounds.center.x shouldBe (50f plusOrMinus 0.1f)
        motion.isMoving shouldBe false
    }

    @Test
    fun `target retargeting after opening has settled follows the new anchor`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        val before = motion.bounds

        motion.updateTarget(alternateTargetAnchor())
        motion.bounds shouldBe before
        motion.isVisible shouldBe true
        advanceMorphFrames(240)

        motion.bounds.center.x shouldBe (350f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (200f plusOrMinus 0.1f)
        motion.isMoving shouldBe false
    }

    @Test
    fun `target update while caught waits for release and uses latest anchor`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        motion.catch()
        advanceMorphFrames(40)
        val caughtBounds = motion.bounds

        motion.updateTarget(alternateTargetAnchor())
        motion.bounds shouldBe caughtBounds
        motion.isVisible shouldBe true
        motion.isMoving shouldBe false

        motion.release(0f)
        advanceMorphFrames(240)
        motion.isOpen shouldBe true
        motion.bounds.center.x shouldBe (350f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (200f plusOrMinus 0.1f)
    }

    @Test
    fun `scrubbing a closing morph keeps it visible under gesture ownership`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        motion.close()
        advanceMorphFrames(5)

        motion.scrub(0.4f)
        advanceMorphFrames(3)
        motion.isVisible shouldBe true
        motion.isMoving shouldBe false
        motion.bounds.center.x shouldBe (110f plusOrMinus 0.1f)

        motion.open()
        advanceMorphFrames(240)
        motion.isOpen shouldBe true
        motion.bounds.center.x shouldBe (200f plusOrMinus 0.1f)
    }

    @Test
    fun `drag takes over a closing morph and release resumes from its current path`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        motion.close()
        advanceMorphFrames(5)

        motion.drag(1f)
        advanceMorphFrames(30)
        motion.isVisible shouldBe true
        motion.isMoving shouldBe false
        (motion.progress > 0.5f) shouldBe true
        val draggedBounds = motion.bounds
        advanceMorphFrames(20)
        motion.bounds shouldBe draggedBounds

        motion.release(0f)
        advanceMorphFrames(240)
        motion.isOpen shouldBe true
        motion.bounds.center.x shouldBe (200f plusOrMinus 0.1f)
        (motion.bounds.center.x != draggedBounds.center.x) shouldBe true
    }

    @Test
    fun `source update while caught is deferred until the next close`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.open()
        advanceMorphFrames(240)
        motion.catch()
        advanceMorphFrames(40)
        val caughtBounds = motion.bounds

        motion.updateSource(alternateSourceAnchor())
        motion.bounds shouldBe caughtBounds
        motion.isVisible shouldBe true

        motion.close()
        advanceMorphFrames(240)
        motion.bounds.center.x shouldBe (80f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (90f plusOrMinus 0.1f)
    }

    @Test
    fun `finite extreme release velocity remains bounded and settles safely`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()
        motion.scrub(0.5f)

        motion.release(Float.MAX_VALUE)
        advanceMorphFrames(240)

        motion.isOpen shouldBe true
        motion.bounds.center.x shouldBe (200f plusOrMinus 0.1f)
        motion.bounds.center.y shouldBe (200f plusOrMinus 0.1f)
        motion.isMoving shouldBe false
    }

    @Test
    fun `newer command wins while initial placement interrupts an existing spring`() = morphMotionTest {
        val motion = MorphMotion(this)
        motion.source = sourceAnchor()
        motion.target = targetAnchor()

        motion.scrub(0.4f)
        motion.release(0f)
        motion.open()
        motion.close()
        advanceMorphFrames(240)

        motion.isOpen shouldBe false
        motion.bounds.center.x shouldBe (50f plusOrMinus 0.01f)
        motion.bounds.center.y shouldBe (50f plusOrMinus 0.01f)
        motion.isMoving shouldBe false
        motion.isVisible shouldBe false
    }
}
