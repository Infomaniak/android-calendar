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
package com.infomaniak.calendar.ui.component.drawer.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.foundation.models.ThemedColorUi

data class CalendarColorsUi(
    val sourceColor: Color,
    val sourceVariantColor: Color,
    private val _onSourceColor: ThemedColorUi,
    private val _onSourceVariantColor: ThemedColorUi,
) {
    val onSourceColor: Color @Composable get() = _onSourceColor.toColor()
}
