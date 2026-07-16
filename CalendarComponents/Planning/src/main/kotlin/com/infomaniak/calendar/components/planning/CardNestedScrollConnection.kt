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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventcard.EventCardState

class CardNestedScrollConnection(
    private val eventCardState: EventCardState,
    private val currentCardSize: () -> Dp?,
    private val updateCurrentCardSize: (Dp) -> Unit,
    private val density: Density,
) : NestedScrollConnection {
    private val flingVelocityThreshold = with(density) { 150.dp.toPx() }

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

    override suspend fun onPreFling(available: Velocity): Velocity {
        val cardMinSize = with(density) { eventCardState.collapsedHeight?.toDp() } ?: return Velocity.Zero
        val cardMaxSize = with(density) { eventCardState.expandedHeight?.toDp() } ?: return Velocity.Zero
        val current = currentCardSize() ?: return Velocity.Zero

        // Compute what state to snap towards based on if the user was flinging fast or otherwise based on what progress the user
        // was at when the finger was lifted.
        val snapToExpanded = when {
            available.y > flingVelocityThreshold -> true
            available.y < -flingVelocityThreshold -> false
            else -> {
                val progress = eventCardState.computeProgress(current, density)
                progress >= 0.5f
            }
        }

        val target = if (snapToExpanded) cardMaxSize else cardMinSize

        if (current == target) return Velocity.Zero

        Animatable(current, Dp.VectorConverter).animateTo(
            targetValue = target,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            initialVelocity = with(density) { available.y.toDp() },
        ) { updateCurrentCardSize(value) }

        // Consume the fling so the list doesn't also fling.
        return available
    }
}
