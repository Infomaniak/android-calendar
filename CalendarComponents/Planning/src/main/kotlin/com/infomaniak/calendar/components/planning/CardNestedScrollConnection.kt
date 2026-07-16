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
package com.infomaniak.calendar.components.planning

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.eventcard.EventCardState

class CardNestedScrollConnection(
    private val eventCardState: EventCardState,
    private val currentCardSize: () -> Dp?,
    private val updateCurrentCardSize: (Dp) -> Unit,
    private val density: Density,
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val cardMinSize = with(density) { eventCardState.collapsedHeight?.toDp() } ?: return Offset.Zero
        val cardMaxSize = with(density) { eventCardState.expandedHeight?.toDp() } ?: return Offset.Zero
        val previousCardSize = currentCardSize() ?: cardMaxSize

        // Calculate the change in card size based on scroll delta
        val availableYDp = with(density) { available.y.toDp() }
        val newCardSize = previousCardSize + availableYDp

        // Constrain the card size within the allowed bounds
        val coercedNewCardSize = newCardSize.coerceIn(cardMinSize, cardMaxSize)
        updateCurrentCardSize(coercedNewCardSize)
        val consumed = coercedNewCardSize - previousCardSize

        // Return the consumed scroll amount
        return Offset(0f, with(density) { consumed.toPx() })
    }
}
