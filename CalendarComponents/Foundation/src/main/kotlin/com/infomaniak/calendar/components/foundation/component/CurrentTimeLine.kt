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
package com.infomaniak.calendar.components.foundation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme

private val DotRadius = 4.dp
private val LineWidth = 1.dp

/**
 * Purely visual: a landmark for reading the current time at a glance. It carries no semantics on purpose,
 * having nothing to announce that the clock does not already say.
 *
 * Runs across its whole width at [lineY], its dot centred on its start edge.
 */
@Composable
fun CurrentTimeLine(modifier: Modifier = Modifier, lineY: DrawScope.() -> Float = { center.y }) {
    val color = MaterialTheme.extendedColorScheme.datavizPink

    Canvas(modifier = modifier.defaultMinSize(minHeight = DotRadius * 2)) {
        val y = lineY()

        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = LineWidth.toPx(),
        )

        val startX = if (layoutDirection == LayoutDirection.Ltr) 0f else size.width
        drawCircle(color = color, radius = DotRadius.toPx(), center = Offset(startX, y))
    }
}

@Preview
@Composable
private fun CurrentTimeLinePreview() {
    Surface {
        CurrentTimeLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
