/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.calendar.components.eventdetail.state

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

/**
 * Everything a `Text` needs to collapse to [collapsedMaxLines] and animate its height in both directions.
 *
 * Using [animateContentSize] on the parent of the description is not enough because when the text is collapsing, it suddenly
 * truncates the text to [collapsedMaxLines] which makes it disappear instantly. To fix this issue, we need to compose the whole
 * text until the whole collapsing animation is finished. This also means we now need to handle the animation of the collapsing
 * height manually.
 *
 * Wire it to a `Text` through [maxLines], [onTextLayout] and [animateCollapse].
 */
@Stable
internal class CollapsibleTextState(
    private val collapsedMaxLines: Int,
    private val expandProgress: State<Float>,
    private val isOverflowingState: MutableState<Boolean>,
) {
    /** `null` until the text has been laid out whole once. */
    private var collapsedLinesHeight by mutableStateOf<Int?>(null)
    private val isFullyCollapsed by derivedStateOf { expandProgress.value == 0f }

    /** Whether the text has more to show than what it displays once collapsed. */
    val isOverflowing by isOverflowingState

    /** Truncating is only wanted once settled collapsed, the text must stay whole while its height animates. */
    val maxLines: Int get() = if (isFullyCollapsed) collapsedMaxLines else Int.MAX_VALUE

    fun onTextLayout(textLayoutResult: TextLayoutResult) = with(textLayoutResult) {
        val isOverflowingDetectedWhenCollapsed = hasVisualOverflow
        val isOverflowingDetectedWhenExpanded = lineCount > collapsedMaxLines

        if (isOverflowingDetectedWhenExpanded) collapsedLinesHeight = getLineBottom(collapsedMaxLines - 1).roundToInt()
        isOverflowingState.value = isOverflowingDetectedWhenCollapsed || isOverflowingDetectedWhenExpanded
    }

    internal fun animatedHeight(fullHeight: Int): Int {
        return lerp(collapsedLinesHeight?.coerceAtMost(fullHeight) ?: fullHeight, fullHeight, expandProgress.value)
    }
}

@Composable
internal fun rememberCollapsibleTextState(isCollapsed: Boolean, collapsedMaxLines: Int): CollapsibleTextState {
    val expandProgress = animateFloatAsState(if (isCollapsed) 0f else 1f, label = "Collapsible text expansion")
    val isOverflowing = rememberSaveable { mutableStateOf(false) }

    return remember(collapsedMaxLines) { CollapsibleTextState(collapsedMaxLines, expandProgress, isOverflowing) }
}

/** Lets the text lay itself out whole, but only reports and displays the height [state] currently animates to. */
internal fun Modifier.animateCollapse(state: CollapsibleTextState): Modifier = clipToBounds().layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
    layout(placeable.width, state.animatedHeight(placeable.height)) { placeable.place(x = 0, y = 0) }
}
