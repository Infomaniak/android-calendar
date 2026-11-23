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
package com.infomaniak.calendar.components.month.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.infomaniak.calendar.components.event.EventItemDefaults
import com.infomaniak.designsystem.core.theme.EsdsTheme

internal object MonthDefaults {
    val DayNumberStyle: TextStyle @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodySmall
    // Just like all day events for coherence
    val EventTitleStyle: TextStyle @Composable @ReadOnlyComposable get() = EventItemDefaults.AllDayTitleStyle

    // The day number at the top, inside a cell. It needs to adapt to the user font size accessibility settings and grow but be at
    // least 20.dp. Because this is required for LaneMetrics computations, it must be computed here and heightIn(min = 20.dp) is
    // not enough.
    val DayNumberSize: Dp
        @Composable @ReadOnlyComposable get() {
            return max(20.dp, DayNumberStyle.lineHeightDp() + EsdsTheme.spacing.twoXs * 2)
        }
    // Room between the day number's circle and the edges of its cell, above the first lane.
    val DayNumberPadding: PaddingValues
        @Composable @ReadOnlyComposable get() = PaddingValues(top = EsdsTheme.spacing.twoXs, bottom = EsdsTheme.spacing.md)

    // Padding inside the EventItemCard
    val EventTitlePadding: PaddingValues @Composable @ReadOnlyComposable get() = PaddingValues(start = EsdsTheme.spacing.xs)
    // Height of a MonthEventChip
    val LaneHeight: Dp
        @Composable @ReadOnlyComposable get() = with(EventTitlePadding) {
            calculateTopPadding() + EventTitleStyle.lineHeightDp() + calculateBottomPadding()
        }
    val LaneSpacing: Dp @Composable @ReadOnlyComposable get() = EsdsTheme.spacing.twoXs
    // Room between an event and the edges of the first and last days it covers.
    val EventHorizontalInset: Dp @Composable @ReadOnlyComposable get() = EsdsTheme.spacing.xs

    @Composable
    @ReadOnlyComposable
    private fun TextStyle.lineHeightDp(): Dp = with(LocalDensity.current) { lineHeight.toDp() }
}
