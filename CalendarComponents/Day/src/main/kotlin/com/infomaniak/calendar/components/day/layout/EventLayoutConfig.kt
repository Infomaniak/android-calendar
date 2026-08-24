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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme

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
    val HorizontalSpacing: Dp @Composable get() = EsdsTheme.spacing.xs
    val HorizontalPadding: Dp @Composable get() = EsdsTheme.spacing.md
    val TitleHeight: Dp @Composable get() = EsdsTheme.spacing.twoXl
    val MinEventWidth: Dp @Composable get() = EsdsTheme.spacing.xs
    val MinEventHeight: Dp @Composable get() = EsdsTheme.spacing.xl

    /** A hairline keeping two events from touching, finer than the smallest step the scale has. */
    val VerticalSpacing: Dp = 1.dp
}

@Composable
fun eventLayoutConfig(): EventLayoutConfig = with(LocalDensity.current) {
    EventLayoutConfig(
        horizontalSpacing = EventLayoutDefaults.HorizontalSpacing.toPx(),
        horizontalPadding = EventLayoutDefaults.HorizontalPadding.toPx(),
        titleHeight = EventLayoutDefaults.TitleHeight.toPx(),
        minEventWidth = EventLayoutDefaults.MinEventWidth.toPx(),
        minEventHeight = EventLayoutDefaults.MinEventHeight.toPx(),
        verticalSpacing = EventLayoutDefaults.VerticalSpacing.toPx(),
    )
}
