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
import com.infomaniak.calendar.components.event.EventItemDefaults
import com.infomaniak.designsystem.core.theme.EsdsTheme

internal object MonthDefaults {
    val DayNumberStyle: TextStyle
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography.labelMedium
    /** The title of an all-day [com.infomaniak.calendar.components.event.EventItem], so both read alike. */
    val EventTitleStyle: TextStyle
        @Composable @ReadOnlyComposable get() = EventItemDefaults.AllDayTitleStyle

    /** The day number's circle: one line of [DayNumberStyle] with room around it. */
    val DayNumberSize: Dp
        @Composable @ReadOnlyComposable get() = DayNumberStyle.lineHeightDp() + EsdsTheme.spacing.xs * 2
    /** Room between the day number's circle and the edges of its cell, above the first lane. */
    val DayNumberPadding: Dp
        @Composable @ReadOnlyComposable get() = EsdsTheme.spacing.md

    /** Room around an event's title, its start padding clearing the card's accent bar. */
    val EventTitlePadding: PaddingValues
        @Composable @ReadOnlyComposable get() = PaddingValues(
            start = EsdsTheme.spacing.xs,
            top = EsdsTheme.spacing.twoXs,
            bottom = EsdsTheme.spacing.twoXs,
        )
    /** An event chip: one line of [EventTitleStyle] within its [EventTitlePadding]. */
    val LaneHeight: Dp
        @Composable @ReadOnlyComposable get() = with(EventTitlePadding) {
            calculateTopPadding() + EventTitleStyle.lineHeightDp() + calculateBottomPadding()
        }
    val LaneSpacing: Dp
        @Composable @ReadOnlyComposable get() = EsdsTheme.spacing.twoXs
    /** Room between an event and the edges of the first and last days it covers. */
    val EventHorizontalInset: Dp
        @Composable @ReadOnlyComposable get() = EsdsTheme.spacing.xs

    @Composable
    @ReadOnlyComposable
    private fun TextStyle.lineHeightDp(): Dp = with(LocalDensity.current) { lineHeight.toDp() }
}
