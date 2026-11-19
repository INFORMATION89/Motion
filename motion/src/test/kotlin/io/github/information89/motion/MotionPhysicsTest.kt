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

import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MotionPhysicsTest {

    @Test
    fun `spring response and stiffness convert both ways`() {
        MotionSpring.responseFor(380f) shouldBe (0.3223f plusOrMinus 0.0005f)
        MotionSpring.stiffnessFor(MotionSpring.responseFor(260f)) shouldBe (260f plusOrMinus 0.01f)
        MotionSpring(response = 0.5f, damping = 1f).stiffness shouldBe (157.91f plusOrMinus 0.01f)
    }

    @Test
    fun `tokens keep the values used before the framework`() {
        Motion.container.stiffness shouldBe (380f plusOrMinus 0.01f)
        Motion.container.damping shouldBe 0.78f
        Motion.dismiss.stiffness shouldBe (260f plusOrMinus 0.01f)
        Motion.control.stiffness shouldBe (400f plusOrMinus 0.01f)
        Motion.content.damping shouldBe 0.85f
        Motion.settle.damping shouldBe 1f
    }

    @Test
    fun `rubber band follows the finger at first, then resists, never passing its dimension`() {
        MotionPhysics.rubberBand(0f, 100f) shouldBe 0f
        // Small pulls move about 0.55 of the finger.
        MotionPhysics.rubberBand(1f, 100f) shouldBe (0.55f plusOrMinus 0.01f)
        val a = MotionPhysics.rubberBand(50f, 100f)
        val b = MotionPhysics.rubberBand(500f, 100f)
        a shouldBeLessThan b
        b shouldBeLessThan 100f
        MotionPhysics.rubberBand(1_000_000f, 100f) shouldBeLessThan 100f
    }

    @Test
    fun `resist leaves the range alone and resists past each edge`() {
        MotionPhysics.resist(0.5f, 0f, 1f, 0.1f, 0.2f) shouldBe 0.5f
        val above = MotionPhysics.resist(1.5f, 0f, 1f, 0.1f, 0.2f)
        above shouldBeLessThan 1.2f
        (above > 1f) shouldBe true
        val below = MotionPhysics.resist(-0.5f, 0f, 1f, 0.1f, 0.2f)
        (below > -0.1f && below < 0f) shouldBe true
    }

    @Test
    fun `unresist is the inverse of resist`() {
        listOf(-0.5f, -0.05f, 0f, 0.3f, 1f, 1.02f, 1.3f, 3f).forEach { raw ->
            val shown = MotionPhysics.resist(raw, 0f, 1f, 0.1f, 0.2f)
            MotionPhysics.unresist(shown, 0f, 1f, 0.1f, 0.2f) shouldBe (raw plusOrMinus 0.0005f)
        }
        MotionPhysics.unRubberBand(MotionPhysics.rubberBand(37f, 100f), 100f) shouldBe (37f plusOrMinus 0.001f)
    }

    @Test
    fun `resistance is 1 inside the range and smaller further out`() {
        MotionPhysics.resistance(0.5f, 0f, 1f, 0.1f, 0.1f) shouldBe 1f
        val near = MotionPhysics.resistance(1.01f, 0f, 1f, 0.1f, 0.1f)
        val far = MotionPhysics.resistance(2f, 0f, 1f, 0.1f, 0.1f)
        far shouldBeLessThan near
        near shouldBeLessThan 1f
    }

    @Test
    fun `projection matches UIKit's formula`() {
        // 1000 units/s for a normal deceleration ends 499 units further.
        MotionPhysics.project(0f, 1000f, 0.998f) shouldBe (499f plusOrMinus 0.5f)
        MotionPhysics.project(10f, 0f) shouldBe 10f
    }

    @Test
    fun `slow releases count as no fling`() {
        MotionPhysics.chooseTarget(0.4f, 0.3f, listOf(0f, 1f), slowFlick = 0.5f, fastFlick = 2f) shouldBe 0f
        MotionPhysics.chooseTarget(0.6f, -0.3f, listOf(0f, 1f), slowFlick = 0.5f, fastFlick = 2f) shouldBe 1f
    }

    @Test
    fun `a fling lands on the rest point nearest its projection`() {
        // 6 units/s from 0.2 with the sheet deceleration ends near 0.79: closest to 1.
        MotionPhysics.chooseTarget(
            0.2f,
            6f,
            listOf(0f, 1f),
            slowFlick = 0.5f,
            fastFlick = 100f,
            deceleration = 0.99f
        ) shouldBe 1f
        // A gentle 1 unit/s only reaches ~0.3: stays at 0.
        MotionPhysics.chooseTarget(
            0.2f,
            1f,
            listOf(0f, 1f),
            slowFlick = 0.5f,
            fastFlick = 100f,
            deceleration = 0.99f
        ) shouldBe 0f
    }

    @Test
    fun `a fast flick that would land back where it started moves one step`() {
        // Projection with a strong deceleration barely moves, but the flick is fast: next point.
        MotionPhysics.chooseTarget(
            0.05f,
            3f,
            listOf(0f, 0.5f, 1f),
            slowFlick = 0.5f,
            fastFlick = 2f,
            deceleration = 0.5f
        ) shouldBe 0.5f
        MotionPhysics.chooseTarget(
            0.95f,
            -3f,
            listOf(0f, 0.5f, 1f),
            slowFlick = 0.5f,
            fastFlick = 2f,
            deceleration = 0.5f
        ) shouldBe 0.5f
    }

    @Test
    fun `window maps progress through a range`() {
        MotionValue.window(0.35f, 0.35f, 0.70f) shouldBe 0f
        MotionValue.window(0.525f, 0.35f, 0.70f) shouldBe (0.5f plusOrMinus 0.001f)
        MotionValue.window(1.2f, 0.35f, 0.70f) shouldBe 1f
        MotionValue.window(-0.2f, 0f, 0.15f) shouldBe 0f
    }
}
