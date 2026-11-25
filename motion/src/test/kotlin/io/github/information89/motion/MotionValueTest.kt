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
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test
import kotlin.math.abs

/** A frame every 16 ms; each frame waits for one `yield()`, so a test can step through motion. */
private class StepClock : MonotonicFrameClock {
    private var time = 0L
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        yield()
        time += 16_000_000L
        return onFrame(time)
    }
}

private fun motionTest(block: suspend CoroutineScope.() -> Unit) = runBlocking(StepClock()) {
    block()
    coroutineContext.cancelChildren()
}

private suspend fun frames(n: Int) = repeat(n) { yield() }

class MotionValueTest {

    private fun CoroutineScope.value(initial: Float = 0f) =
        MotionValue(initial, this, overMin = 0.1f, overMax = 0.1f, slowFlick = 0.5f, fastFlick = 2f)

    @Test
    fun `rest points are copied sorted and deduplicated`() = motionTest {
        val supplied = mutableListOf(1f, 0f, 1f, 0.5f)
        val v = MotionValue(0f, this, restPoints = supplied)
        supplied += 2f
        v.restPoints shouldBe listOf(0f, 0.5f, 1f)

        v.restPoints = listOf(2f, 0f, 2f, 1f)
        v.restPoints shouldBe listOf(0f, 1f, 2f)
    }

    @Test
    fun `rest points reject empty and non finite values`() = motionTest {
        val v = value()
        (
            runCatching { v.restPoints = emptyList() }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.restPoints = listOf(0f, Float.NaN) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.restPoints = listOf(Float.POSITIVE_INFINITY) }.exceptionOrNull() is
                    IllegalArgumentException
        ) shouldBe true
    }

    @Test
    fun `constructor rejects non finite and invalid configuration values`() = motionTest {
        (
            runCatching { MotionValue(Float.NaN, this) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { MotionValue(0f, this, overMin = -1f) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { MotionValue(0f, this, overMax = Float.POSITIVE_INFINITY) }.exceptionOrNull() is
                    IllegalArgumentException
        ) shouldBe true
        (
            runCatching { MotionValue(0f, this, slowFlick = Float.NaN) }.exceptionOrNull() is
                    IllegalArgumentException
        ) shouldBe true
        (
            runCatching { MotionValue(0f, this, fastFlick = -1f) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { MotionValue(0f, this, slowFlick = 3f, fastFlick = 2f) }.exceptionOrNull() is
                    IllegalArgumentException
        ) shouldBe true
        (
            runCatching { MotionValue(0f, this, deceleration = 1f) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
    }

    @Test
    fun `runtime inputs reject non finite values`() = motionTest {
        val v = value()
        (
            runCatching { v.drag(Float.NaN) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.release(Float.POSITIVE_INFINITY) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.targetFor(Float.NEGATIVE_INFINITY) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.animateTo(Float.NaN) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.animateTo(1f, velocity = Float.NaN) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.snapTo(Float.POSITIVE_INFINITY) }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
    }

    @Test
    fun `flick thresholds reject invalid assignments immediately`() = motionTest {
        val v = value()
        (
            runCatching { v.slowFlick = -1f }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.fastFlick = Float.NaN }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
        (
            runCatching { v.slowFlick = 3f }.exceptionOrNull() is IllegalArgumentException
        ) shouldBe true
    }

    @Test
    fun `drag moves one to one inside the range and resists past it`() = motionTest {
        val v = value()
        v.drag(0.4f)
        v.value shouldBe (0.4f plusOrMinus 0.0001f)
        v.drag(0.8f) // finger at 1.2
        (v.value > 1f) shouldBe true
        v.value shouldBeLessThan 1.1f
    }

    @Test
    fun `catching an overshoot keeps the surface where it is`() = motionTest {
        val v = value()
        v.drag(1.3f) // pulled well past open
        val before = v.value
        v.stop() // a new touch catches it
        v.drag(0.0001f)
        // Without unresist the next drag would resist twice and jump back towards 1.
        abs(v.value - before) shouldBeLessThan 0.001f
    }

    @Test
    fun `a slow release goes back to the nearest rest point`() = motionTest {
        val v = value()
        v.drag(0.4f)
        v.release(0.2f)
        yield()
        v.target shouldBe 0f
    }

    @Test
    fun `a fast flick moves one step even from close to the start`() = motionTest {
        val v = value()
        v.drag(0.05f)
        v.release(3f)
        yield()
        v.target shouldBe 1f
    }

    @Test
    fun `a release from an over-pull settles at the end`() = motionTest {
        val v = value()
        v.drag(1.5f)
        v.release(5f)
        yield()
        v.target shouldBe 1f
    }

    @Test
    fun `stop mid-flight holds the current position`() = motionTest {
        val v = value()
        v.animateTo(1f, velocity = 0f)
        frames(6)
        val mid = v.value
        (mid > 0f && mid < 1f) shouldBe true
        v.stop()
        frames(6)
        v.value shouldBe (mid plusOrMinus 0.0001f)
    }

    @Test
    fun `an animation reaches its rest point`() = motionTest {
        val v = value()
        v.animateTo(1f, velocity = 0f)
        frames(200)
        v.value shouldBe (1f plusOrMinus 0.001f)
        v.isRunning shouldBe false
    }
}
