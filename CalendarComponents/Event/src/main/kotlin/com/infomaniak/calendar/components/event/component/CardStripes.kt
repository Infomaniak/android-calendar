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
package com.infomaniak.calendar.components.event.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.EventItemStatus
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private val STRIPE_WIDTH = 2.dp
private val STRIPE_SPACING = 8.dp

@Composable
internal fun Modifier.cardStripes(status: EventItemStatus): Modifier {
    val stripesColor = status.stripesColor()
    return then(if (stripesColor != null) Modifier.diagonalStripes(stripesColor) else Modifier)
}

/**
 * Draws diagonal stripes on top of the existing background, behind the content.
 */
private fun Modifier.diagonalStripes(color: Color, rotationDegrees: Float = 45f): Modifier = drawWithContent {
    val stripeWidthPx = STRIPE_WIDTH.toPx()
    val step = stripeWidthPx + STRIPE_SPACING.toPx()

    if (step > 0f) {
        val angle = Math.toRadians(rotationDegrees.toDouble())
        val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
        val perpendicular = Offset(-sin(angle).toFloat(), cos(angle).toFloat())
        val center = Offset(size.width / 2f, size.height / 2f)
        val halfLength = hypot(size.width, size.height)

        var offset = -halfLength
        while (offset <= halfLength) {
            val base = center + perpendicular * offset
            drawLine(
                color = color,
                start = base - direction * halfLength,
                end = base + direction * halfLength,
                strokeWidth = stripeWidthPx,
            )
            offset += step
        }
    }

    drawContent()
}

@Preview
@Composable
private fun Preview() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Gray)
            .diagonalStripes(Color.DarkGray),
    )
}
