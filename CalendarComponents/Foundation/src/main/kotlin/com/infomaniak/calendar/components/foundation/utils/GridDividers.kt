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
package com.infomaniak.calendar.components.foundation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.theme.ComponentColors
import com.infomaniak.calendar.components.foundation.theme.Dimens

/** A horizontal line of the calendar grids. */
@Composable
fun HorizontalGridLine(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, Dimens.GridLineThickness, ComponentColors.GridDividerColor)
}

/** A vertical line of the calendar grids. */
@Composable
fun VerticalGridLine(modifier: Modifier = Modifier) {
    VerticalDivider(modifier, Dimens.GridLineThickness, ComponentColors.GridDividerColor)
}

/** A grid line along the end edge of the component, behind its content. */
@Composable
fun Modifier.verticalEndDivider(color: Color = ComponentColors.GridDividerColor): Modifier = drawBehind {
    val x = if (layoutDirection == LayoutDirection.Ltr) size.width else 0f
    drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = Dimens.GridLineThickness.toPx(),
    )
}

@Preview
@Composable
private fun Preview() {
    Surface {
        Box(Modifier.size(64.dp).verticalEndDivider()) {
            HorizontalGridLine()
            VerticalGridLine()
        }
    }
}
