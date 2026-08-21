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
    private val _containerColor: Int,
    private val _onContainerColor: ThemedColorUi,
    private val _containerVariantColor: Int,
    private val _onContainerVariantColor: ThemedColorUi,
) {
    val sourceColor: Color @Composable get() = Color(_sourceColor)
    val containerColor: Color @Composable get() = Color(_containerColor)
    val onContainerColor: Color @Composable get() = _onContainerColor.toColor()
    val containerVariantColor: Color @Composable get() = Color(_containerVariantColor)
    val onContainerVariantColor: Color @Composable get() = _onContainerVariantColor.toColor()
}
