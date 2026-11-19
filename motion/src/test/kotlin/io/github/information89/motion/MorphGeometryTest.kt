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

import androidx.compose.ui.geometry.Rect
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MorphGeometryTest {
    private val pill = MorphAnchor(Rect(100f, 1000f, 300f, 1060f), cornerRadius = 30f)
    private val sheet = MorphAnchor(Rect(0f, 400f, 1000f, 2000f), cornerRadius = 48f)

    @Test
    fun `ends are the anchors`() {
        MorphGeometry.frame(pill, sheet, 0f) shouldBe MorphFrame(pill.bounds, 30f)
        MorphGeometry.frame(pill, sheet, 1f) shouldBe MorphFrame(sheet.bounds, 48f)
    }

    @Test
    fun `halfway is the midpoint`() {
        val f = MorphGeometry.frame(pill, sheet, 0.5f)
        f.bounds shouldBe Rect(50f, 700f, 650f, 1530f)
        f.cornerRadius shouldBe 39f
    }

    @Test
    fun `overshoot extrapolates past the target`() {
        val f = MorphGeometry.frame(pill, sheet, 1.1f)
        (f.bounds.width > sheet.bounds.width) shouldBe true
        (f.bounds.top < sheet.bounds.top) shouldBe true
    }

    @Test
    fun `corners stay within half the shorter side`() {
        val tiny = MorphAnchor(Rect(0f, 0f, 10f, 10f), cornerRadius = 40f)
        MorphGeometry.frame(tiny, tiny, 0f).cornerRadius shouldBe 5f
    }
}
