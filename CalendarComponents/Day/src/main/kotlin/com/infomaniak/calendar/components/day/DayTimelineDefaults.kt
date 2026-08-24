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
package com.infomaniak.calendar.components.day

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme

object DayTimelineDefaults {
    val HourHeight: Dp = 64.dp
    val HourGutterWidth: Dp = 64.dp

    /**
     * Room between the end of the timeline's content and the edge of the screen. The hour lines,
     * the events and the current time indicator all stop at the same x, so they share this value.
     */
    val TimelineEndPadding: Dp
        @Composable get() = EsdsTheme.spacing.xl

    /**
     * Room kept under the last hour. The floating toolbar hovers over the bottom of the screen
     * rather than sitting below the content, so without it the end of the day would stay under the
     * toolbar however far the timeline is scrolled.
     *
     * This is the height the toolbar reserves, not the height of its bar: HorizontalFloatingToolbar
     * sets its own minimum to the largest size its FAB can take, and the FAB grows into it as the
     * toolbar collapses. The height therefore does not change between the expanded and collapsed
     * states. Material keeps that value internal, as `FloatingToolbarDefaults.FabSizeRange`
     * (56dp to 80dp), so it cannot be read from the public API and is repeated here.
     */
    val BottomPadding: Dp = 80.dp
}
