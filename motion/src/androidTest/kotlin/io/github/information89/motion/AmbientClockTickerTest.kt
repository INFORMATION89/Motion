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
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AmbientClockTickerTest {
    @Test
    fun inactiveTickerDoesNotRequestFramesAndCanBeActivatedByStateChange() = runTest {
        val running = mutableStateOf(false)
        val frameClock = ManualFrameClock()
        var ticks = 0
        val job = launch(frameClock) {
            runAmbientClockTicker(
                frameIntervalNanos = 33_333_333L,
                running = { running.value },
                onTick = { ticks++ },
            )
        }

        runCurrent()
        assertEquals(0, frameClock.pendingFrames)

        running.value = true
        runCurrent()
        assertEquals(1, frameClock.pendingFrames)

        frameClock.sendFrame(1_000_000_000L)
        runCurrent()
        assertEquals(1, ticks)

        job.cancel()
        runCurrent()
        assertEquals(0, frameClock.pendingFrames)
    }

    @Test
    fun deactivatingTickerCancelsPendingFrameRequest() = runTest {
        val running = mutableStateOf(true)
        val frameClock = ManualFrameClock()
        var ticks = 0
        val job = launch(frameClock) {
            runAmbientClockTicker(
                frameIntervalNanos = 33_333_333L,
                running = { running.value },
                onTick = { ticks++ },
            )
        }

        runCurrent()
        assertEquals(1, frameClock.pendingFrames)

        running.value = false
        runCurrent()
        assertEquals(0, frameClock.pendingFrames)
        frameClock.sendFrame(2_000_000_000L)
        runCurrent()
        assertEquals(0, ticks)

        job.cancel()
    }

    @Test
    fun timerDoesNotRequestFramesMoreFrequentlyThanConfiguredInterval() = runTest {
        val running = mutableStateOf(true)
        val frameClock = ManualFrameClock()
        val job = launch(frameClock) {
            runAmbientClockTicker(
                frameIntervalNanos = 50_000_000L,
                running = { running.value },
                onTick = {},
            )
        }

        runCurrent()
        assertEquals(1, frameClock.pendingFrames)
        frameClock.sendFrame(1_000_000_000L)
        runCurrent()
        assertEquals(0, frameClock.pendingFrames)

        advanceTimeBy(49)
        runCurrent()
        assertEquals(0, frameClock.pendingFrames)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, frameClock.pendingFrames)

        job.cancel()
    }
}

private class ManualFrameClock : MonotonicFrameClock {
    private val waiting = mutableListOf<(Long) -> Unit>()
    val pendingFrames: Int
        get() = waiting.size

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R =
        suspendCancellableCoroutine { continuation: CancellableContinuation<R> ->
            val request: (Long) -> Unit = { frameNanos ->
                if (continuation.isActive) continuation.resume(onFrame(frameNanos))
            }
            waiting += request
            continuation.invokeOnCancellation { waiting.remove(request) }
        }

    fun sendFrame(frameNanos: Long) {
        val current = waiting.toList()
        waiting.clear()
        current.forEach { it(frameNanos) }
    }
}
