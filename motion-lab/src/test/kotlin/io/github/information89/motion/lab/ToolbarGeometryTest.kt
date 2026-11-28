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

package io.github.information89.motion.lab

import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ToolbarGeometryTest {
    private val ids = listOf("overview", "saved", "discover", "settings")

    @Test
    fun `host progress interpolates by stable adjacent IDs`() {
        toolbarSelectionFractions(
            ids,
            selectedTabId = "saved",
            hostProgress = ToolbarHostProgress("overview", "saved", 0.25f)
        ) shouldBe mapOf(
            "overview" to 0.75f,
            "saved" to 0.25f,
            "discover" to 0f,
            "settings" to 0f
        )
    }

    @Test
    fun `reverse host progress retains endpoint order and fraction`() {
        toolbarSelectionFractions(
            ids,
            selectedTabId = "discover",
            hostProgress = ToolbarHostProgress("discover", "saved", 0.4f)
        ) shouldBe mapOf(
            "overview" to 0f,
            "saved" to 0.4f,
            "discover" to 0.6f,
            "settings" to 0f
        )
    }

    @Test
    fun `invalid progress falls back to committed selection`() {
        val invalid = listOf(
            ToolbarHostProgress("overview", "saved", Float.NaN),
            ToolbarHostProgress("overview", "saved", Float.POSITIVE_INFINITY),
            ToolbarHostProgress("overview", "saved", -0.1f),
            ToolbarHostProgress("overview", "saved", 1.1f),
            ToolbarHostProgress("overview", "settings", 0.5f),
            ToolbarHostProgress("missing", "saved", 0.5f)
        )

        invalid.forEach { progress ->
            toolbarSelectionFractions(ids, "discover", progress) shouldBe mapOf(
                "overview" to 0f,
                "saved" to 0f,
                "discover" to 1f,
                "settings" to 0f
            )
        }
    }

    @Test
    fun `bounds interpolate both position and width`() {
        val result = interpolateToolbarBounds(
            from = ToolbarTabBounds(leftPx = 12f, widthPx = 80f),
            to = ToolbarTabBounds(leftPx = 120f, widthPx = 144f),
            fraction = 0.25f
        )

        result?.leftPx shouldBe (39f plusOrMinus 0.001f)
        result?.widthPx shouldBe (96f plusOrMinus 0.001f)
    }

    @Test
    fun `invalid geometry progress is ignored`() {
        interpolateToolbarBounds(
            ToolbarTabBounds(12f, 80f),
            ToolbarTabBounds(120f, 144f),
            Float.NaN
        ) shouldBe null
    }

    @Test
    fun `indicator falls back to selected bounds when endpoint geometry is missing`() {
        val selected = ToolbarTabBounds(120f, 144f)

        toolbarIndicatorBounds(
            tabIds = ids,
            boundsById = mapOf("saved" to selected),
            selectedTabId = "saved",
            hostProgress = ToolbarHostProgress("overview", "saved", 0.5f)
        ) shouldBe selected
    }

    @Test
    fun `non adjacent host progress does not move the indicator`() {
        val selected = ToolbarTabBounds(100f, 80f)
        val bounds = mapOf(
            "overview" to ToolbarTabBounds(0f, 64f),
            "saved" to selected,
            "discover" to ToolbarTabBounds(200f, 96f),
            "settings" to ToolbarTabBounds(320f, 88f)
        )

        toolbarIndicatorBounds(
            tabIds = ids,
            boundsById = bounds,
            selectedTabId = "saved",
            hostProgress = ToolbarHostProgress("overview", "settings", 0.5f)
        ) shouldBe selected
    }

    @Test
    fun `nearest tab uses measured centers and preserves logical order on ties`() {
        val bounds = mapOf(
            "overview" to ToolbarTabBounds(0f, 80f),
            "saved" to ToolbarTabBounds(80f, 120f),
            "discover" to ToolbarTabBounds(200f, 100f)
        )

        nearestToolbarTabId(ids, bounds, 100f) shouldBe "saved"
        nearestToolbarTabId(ids, bounds, 90f) shouldBe "overview"
        nearestToolbarTabId(ids, bounds, Float.NaN) shouldBe null
    }

    @Test
    fun `keyboard navigation moves forward and backward with wrap around`() {
        toolbarAdjacentTabId(ids, "saved", forward = true) shouldBe "discover"
        toolbarAdjacentTabId(ids, "saved", forward = false) shouldBe "overview"
        toolbarAdjacentTabId(ids, "settings", forward = true) shouldBe "overview"
        toolbarAdjacentTabId(ids, "overview", forward = false) shouldBe "settings"
    }

    @Test
    fun `keyboard navigation returns null for missing IDs or a single tab`() {
        toolbarAdjacentTabId(ids, "missing", forward = true) shouldBe null
        toolbarAdjacentTabId(listOf("only"), "only", forward = true) shouldBe null
        toolbarAdjacentTabId(emptyList(), "missing", forward = false) shouldBe null
    }

    @Test
    fun `edge auto scroll accelerates toward the nearest physical edge`() {
        val left = toolbarEdgeScrollDelta(
            pointerViewportX = 4f,
            viewportWidthPx = 300f,
            frameSeconds = 1f / 60f,
            reverseDirection = false,
            edgeSizePx = 48f,
            maxSpeedPxPerSecond = 900f
        )
        val right = toolbarEdgeScrollDelta(
            pointerViewportX = 296f,
            viewportWidthPx = 300f,
            frameSeconds = 1f / 60f,
            reverseDirection = false,
            edgeSizePx = 48f,
            maxSpeedPxPerSecond = 900f
        )

        (left < 0f) shouldBe true
        (right > 0f) shouldBe true
        toolbarEdgeScrollDelta(
            pointerViewportX = 150f,
            viewportWidthPx = 300f,
            frameSeconds = 1f / 60f,
            reverseDirection = false,
            edgeSizePx = 48f,
            maxSpeedPxPerSecond = 900f
        ) shouldBe 0f
    }

    @Test
    fun `edge auto scroll mirrors its direction in RTL`() {
        val ltrLeft = toolbarEdgeScrollDelta(
            4f,
            300f,
            1f / 60f,
            reverseDirection = false,
            edgeSizePx = 48f,
            maxSpeedPxPerSecond = 900f
        )
        val rtlLeft = toolbarEdgeScrollDelta(
            4f,
            300f,
            1f / 60f,
            reverseDirection = true,
            edgeSizePx = 48f,
            maxSpeedPxPerSecond = 900f
        )

        (ltrLeft < 0f) shouldBe true
        (rtlLeft > 0f) shouldBe true
        rtlLeft shouldBe -ltrLeft
    }

    @Test
    fun `viewport and content pointer coordinates round trip in LTR and RTL`() {
        val ltrContent = toolbarContentPosition(42f, 120f, reverseDirection = false)
        toolbarViewportPosition(ltrContent, 120f, reverseDirection = false) shouldBe 42f

        val rtlContent = toolbarContentPosition(42f, 120f, reverseDirection = true)
        toolbarViewportPosition(rtlContent, 120f, reverseDirection = true) shouldBe 42f
    }

    @Test
    fun `edge auto scroll rejects invalid frame and geometry values`() {
        toolbarEdgeScrollDelta(
            Float.NaN,
            300f,
            1f / 60f,
            reverseDirection = false,
            edgeSizePx = 48f,
            maxSpeedPxPerSecond = 900f
        ) shouldBe 0f
        toolbarEdgeScrollDelta(
            4f,
            300f,
            Float.POSITIVE_INFINITY,
            reverseDirection = false,
            edgeSizePx = 48f,
            maxSpeedPxPerSecond = 900f
        ) shouldBe 0f
    }

    @Test
    fun `duplicate tab IDs are rejected`() {
        val failure = runCatching {
            toolbarSelectionFractions(
                listOf("overview", "overview"),
                "overview",
                null
            )
        }.exceptionOrNull()

        (failure is IllegalArgumentException) shouldBe true
    }
}
