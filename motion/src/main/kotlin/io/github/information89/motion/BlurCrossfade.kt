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

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/**
 * Content that changes by crossing with a blur (agreed 2026-11-21, MOTION.md "morph, never pop"): when
 * [target] changes, the old content blurs and fades out as the new one sharpens in, on [spring], the same
 * blur replace a [SpringMorph] hands over with ([MorphHandOff.Blur]). A change mid-way starts from what's
 * shown. Both are laid out in the same place, so text of different lengths never jumps.
 */
@Composable
fun <T> BlurCrossfade(
    target: T,
    modifier: Modifier = Modifier,
    spring: MotionSpring = Motion.content,
    blur: Dp = MorphHandOff.Blur,
    content: @Composable (T) -> Unit,
) {
    var shown by remember { mutableStateOf(target) }
    var leaving by remember { mutableStateOf<T?>(null) }
    var hasLeaving by remember { mutableStateOf(false) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (target == shown) return@LaunchedEffect
        leaving = shown
        hasLeaving = true
        shown = target
        progress.snapTo(0f)
        progress.animateTo(1f, spring.spec())
        hasLeaving = false
        leaving = null
    }
    Box(modifier) {
        @Suppress("UNCHECKED_CAST")
        if (hasLeaving) {
            Box(Modifier.blurFade(blur) { 1f - progress.value.coerceIn(0f, 1f) }) { content(leaving as T) }
        }
        Box(Modifier.blurFade(blur) { progress.value.coerceIn(0f, 1f) }) { content(shown) }
    }
}

/** Fully there at [presence] 1; faded and blurred up to [blur] towards 0. */
private fun Modifier.blurFade(blur: Dp, presence: () -> Float): Modifier = graphicsLayer {
    val p = presence()
    alpha = p
    val radius = blur.toPx() * (1f - p)
    renderEffect = if (radius > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BlurEffect(radius, radius, TileMode.Decal)
    } else {
        null
    }
}
