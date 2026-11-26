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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasRole
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.information89.motion.ui.MotionUiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class ToolbarPrototypeUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tabTapRequestsDestinationWithoutChangingHostSelection() {
        val requested = AtomicReference<String?>(null)
        setToolbar(onSelectionRequest = requested::set)

        composeRule.onNode(
            hasRole(Role.Tab) and hasContentDescription("Saved items destination")
        ).performClick()
        composeRule.waitForIdle()

        assertEquals("saved", requested.get())
        composeRule.onNode(
            hasRole(Role.Tab) and hasContentDescription("Overview destination")
        ).assertIsSelected()
        composeRule.onNode(
            hasRole(Role.Tab) and hasContentDescription("Saved items destination")
        ).assertIsNotSelected()
    }

    @Test
    fun attachedContextActionHasOneSemanticButtonTarget() {
        assertContextActionHasOneTarget(detached = false)
    }

    @Test
    fun detachedContextActionKeepsTheSameSingleSemanticButtonTarget() {
        assertContextActionHasOneTarget(detached = true)
    }

    @Test
    fun hiddenContextActionHasNoSemanticOrClickableTarget() {
        setToolbar(showContextAction = false)

        composeRule.waitForIdle()
        composeRule.onAllNodes(hasRole(Role.Button)).assertCountEquals(0)
    }

    @Test
    fun arrowRightMovesTheSingleFocusStopForwardInLtr() {
        val requested = AtomicReference<String?>(null)
        setToolbar(onSelectionRequest = requested::set)

        val overview = composeRule.onNodeWithContentDescription("Overview destination")
        overview.performSemanticsAction(SemanticsActions.RequestFocus)
        composeRule.waitForIdle()
        overview.assertIsFocused()

        overview.performKeyInput { pressKey(Key.DirectionRight) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Saved items destination").assertIsFocused()
        overview.assertIsNotFocused()
        assertEquals("saved", requested.get())
    }

    @Test
    fun arrowLeftMovesTheSingleFocusStopForwardInRtl() {
        val requested = AtomicReference<String?>(null)
        setToolbar(
            layoutDirection = LayoutDirection.Rtl,
            onSelectionRequest = requested::set
        )

        val overview = composeRule.onNodeWithContentDescription("Overview destination")
        overview.performSemanticsAction(SemanticsActions.RequestFocus)
        composeRule.waitForIdle()
        overview.assertIsFocused()

        overview.performKeyInput { pressKey(Key.DirectionLeft) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Saved items destination").assertIsFocused()
        overview.assertIsNotFocused()
        assertEquals("saved", requested.get())
    }

    private fun assertContextActionHasOneTarget(detached: Boolean) {
        val clicks = AtomicInteger(0)
        setToolbar(
            contextActionDetached = detached,
            onActionClick = { clicks.incrementAndGet() }
        )

        composeRule.waitForIdle()
        composeRule.onAllNodes(hasRole(Role.Button)).assertCountEquals(1)
        composeRule.onNode(hasRole(Role.Button)).performClick()
        composeRule.waitForIdle()

        assertEquals(1, clicks.get())
        composeRule.onAllNodes(hasRole(Role.Button)).assertCountEquals(1)
    }

    private fun setToolbar(
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        showContextAction: Boolean = true,
        contextActionDetached: Boolean = false,
        onActionClick: () -> Unit = {},
        onSelectionRequest: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MotionUiTheme {
                    Box(Modifier.width(380.dp)) {
                        ToolbarPrototypeBar(
                            selectedTabId = "overview",
                            hostProgress = null,
                            profile = ToolbarVisualProfile.EXPRESSIVE,
                            showContextAction = showContextAction,
                            contextActionDetached = contextActionDetached,
                            actionActive = false,
                            hapticsEnabled = false,
                            onActionClick = onActionClick,
                            onSelectionRequest = onSelectionRequest,
                            onSelectionInteractionStarted = {}
                        )
                    }
                }
            }
        }
    }
}
