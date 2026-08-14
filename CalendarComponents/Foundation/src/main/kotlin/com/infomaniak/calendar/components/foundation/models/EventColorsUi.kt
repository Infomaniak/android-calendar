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
package com.infomaniak.calendar.components.foundation.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class EventColorsUi(
    private val _sourceColor: Int,
    private val _sourceVariantColor: Int,
    private val _onSourceVariantColor: ThemedColorUi,
) {
    val sourceColor: Color @Composable get() = Color(_sourceColor)
    val sourceVariantColor: Color @Composable get() = Color(_sourceVariantColor)
    val onSourceVariantColor: Color @Composable get() = _onSourceVariantColor.toColor()
}
