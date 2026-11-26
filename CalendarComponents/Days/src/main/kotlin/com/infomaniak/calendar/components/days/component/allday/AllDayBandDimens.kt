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
package com.infomaniak.calendar.components.days.component.allday

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme

private val ChipHeight = 32.dp
private const val WHOLE_ROWS = 2
private val NextRowPeek = 12.dp

/** Room for two rows of chips, the top of a third one peeking below to tell more are scrolled to. */
internal val AllDayBandMaxHeight: Dp
    @Composable get() = ChipHeight * WHOLE_ROWS + AllDayEventSpacing * WHOLE_ROWS + NextRowPeek

internal val AllDayEventSpacing: Dp
    @Composable get() = EsdsTheme.spacing.xs
