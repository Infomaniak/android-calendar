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
package com.infomaniak.calendar.components.foundation.preview

import androidx.annotation.ColorInt
import androidx.compose.runtime.staticCompositionLocalOf
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.ThemedColorUi
import com.infomaniak.calendar.components.foundation.preview.EventColorsUiFactory.Companion.dummyEventColorsUiFactory

val LocalEventColorsUiFactory = staticCompositionLocalOf { dummyEventColorsUiFactory }

fun interface EventColorsUiFactory {
    fun create(@ColorInt color: Int): EventColorsUi

    companion object {
        val dummyEventColorsUiFactory by lazy {
            EventColorsUiFactory {
                EventColorsUi(
                    _sourceColor = 0xFF6750A4.toInt(),
                    _sourceVariantColor = 0x206750A4,
                    _onSourceVariantColor = ThemedColorUi(light = 0xFF21005D.toInt(), dark = 0xFFEADDFF.toInt()),
                )
            }
        }
    }
}
