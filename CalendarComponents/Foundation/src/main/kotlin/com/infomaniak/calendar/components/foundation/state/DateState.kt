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
package com.infomaniak.calendar.components.foundation.state

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class DateState(
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
    val borderColor: @Composable () -> Color,
    val showDots: Boolean = true,
) {
    Today(
        containerColor = { Color.Transparent },
        contentColor = { MaterialTheme.colorScheme.primary },
        borderColor = { MaterialTheme.colorScheme.primary },
        showDots = false,
    ),
    Selected(
        containerColor = { MaterialTheme.colorScheme.primary },
        contentColor = { MaterialTheme.colorScheme.onPrimary },
        borderColor = { Color.Transparent },
        showDots = false,
    ),
    None(
        containerColor = { Color.Transparent },
        contentColor = { MaterialTheme.colorScheme.onSurface },
        borderColor = { Color.Transparent },
    ),
    NotMonth(
        containerColor = { Color.Transparent },
        contentColor = { MaterialTheme.colorScheme.onSurface.copy(0.38f) },
        borderColor = { Color.Transparent },
        showDots = false,
    ),
}
