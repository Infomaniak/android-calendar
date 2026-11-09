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
package com.infomaniak.calendar.ui.component.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.lerp

enum class SegmentCorner(val level: Float) {
    None(0f),
    Inner(1f),
    Outer(2f),
}

// ListItemDefaults.segmentedShapes has no square corner for rows joined within a section, and SegmentedListItem snaps
// to a new shape instead of animating it. Keeping one shape whose corners animate solves both.
@Composable
fun manuallyAnimatedSegmentedShapes(top: SegmentCorner, bottom: SegmentCorner): ListItemShapes {
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val topLevel = animateFloatAsState(top.level, spec)
    val bottomLevel = animateFloatAsState(bottom.level, spec)
    val inner = MaterialTheme.shapes.extraSmall.topStart
    val outer = MaterialTheme.shapes.large.topStart

    val shape = remember(inner, outer) {
        val topCorner = AnimatedCornerSize(topLevel, inner, outer)
        val bottomCorner = AnimatedCornerSize(bottomLevel, inner, outer)
        RoundedCornerShape(topStart = topCorner, topEnd = topCorner, bottomEnd = bottomCorner, bottomStart = bottomCorner)
    }

    return ListItemDefaults.shapes(shape = shape)
}

private class AnimatedCornerSize(
    private val level: State<Float>,
    private val inner: CornerSize,
    private val outer: CornerSize,
) : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float {
        // The spatial spring overshoots, which would give a negative corner when going back to None
        val level = level.value.coerceAtLeast(0f)
        val innerPx = inner.toPx(shapeSize, density)

        return if (level <= 1f) lerp(0f, innerPx, level) else lerp(innerPx, outer.toPx(shapeSize, density), level - 1f)
    }
}
