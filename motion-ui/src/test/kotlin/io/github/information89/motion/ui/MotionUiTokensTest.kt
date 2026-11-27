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

package io.github.information89.motion.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MotionUiTokensTest {

    @Test
    fun `defaults derive surface colors from the supplied Material scheme`() {
        val light = lightColorScheme()
        val dark = darkColorScheme()

        MotionUiTokens.defaults(light).materials.cardContainer shouldBe light.surfaceContainerLow
        MotionUiTokens.defaults(light).materials.glassContainer shouldBe light.surface.copy(alpha = 0.82f)
        MotionUiTokens.defaults(dark).materials.cardContainer shouldBe dark.surfaceContainerLow
        MotionUiTokens.defaults(dark).materials.glassContainer shouldBe dark.surface.copy(alpha = 0.82f)
    }

    @Test
    fun `apps can replace semantic material and layout values`() {
        val custom = MotionUiTokens(
            shapes = MotionUiShapes(card = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
            spacing = MotionUiSpacing(compact = 4.dp, medium = 12.dp, large = 20.dp),
            materials = MotionUiMaterials(
                cardContainer = Color(0xFF101820),
                glassContainer = Color(0xCC101820),
                cardElevation = 3.dp
            )
        )

        custom.shapes.card shouldBe androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        custom.spacing shouldBe MotionUiSpacing(compact = 4.dp, medium = 12.dp, large = 20.dp)
        custom.materials.cardContainer shouldBe Color(0xFF101820)
        custom.materials.glassContainer shouldBe Color(0xCC101820)
        custom.materials.cardElevation shouldBe 3.dp
    }

    @Test
    fun `defaults provide a compact spacing scale and neutral shapes`() {
        val defaults = MotionUiTokens.defaults(lightColorScheme())

        defaults.spacing shouldBe MotionUiSpacing()
        defaults.shapes shouldBe MotionUiShapes()
        defaults.materials.cardElevation shouldBe 1.dp
    }
}
