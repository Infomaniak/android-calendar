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
package com.infomaniak.calendar.components.day.layout

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class EventLayoutConfig(
    val horizontalSpacing: Float,
    val horizontalPadding: Float,
    val titleHeight: Float,
    val minEventWidth: Float,
    val minEventHeight: Float,
    val verticalSpacing: Float,
)

object EventLayoutDefaults {
    val HorizontalSpacing: Dp = 8.dp
    val HorizontalPadding: Dp = 8.dp
    val TitleHeight: Dp = 20.dp
    val MinEventWidth: Dp = 4.dp
    val MinEventHeight: Dp = 16.dp
    val VerticalSpacing: Dp = 1.dp
}

fun Density.eventLayoutConfig(): EventLayoutConfig = EventLayoutConfig(
    horizontalSpacing = EventLayoutDefaults.HorizontalSpacing.toPx(),
    horizontalPadding = EventLayoutDefaults.HorizontalPadding.toPx(),
    titleHeight = EventLayoutDefaults.TitleHeight.toPx(),
    minEventWidth = EventLayoutDefaults.MinEventWidth.toPx(),
    minEventHeight = EventLayoutDefaults.MinEventHeight.toPx(),
    verticalSpacing = EventLayoutDefaults.VerticalSpacing.toPx(),
)
