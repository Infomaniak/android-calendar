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
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.foundation.models.EventColorUi
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.preview.EventColorsUiFactory.Companion.dummyEventColorsUiFactory

val LocalEventColorsUiFactory = staticCompositionLocalOf { dummyEventColorsUiFactory }

fun interface EventColorsUiFactory {
    fun create(@ColorInt color: Int): EventColorsUi

    companion object {
        val dummyEventColorsUiFactory by lazy {
            EventColorsUiFactory { color ->
                EventColorsUi(
                    color = Color(color),
                    onColor = Color.White,
                    _datavizContainer = EventColorUi(light = 0xFFFFFFFF.toInt(), dark = 0xFF381E72.toInt()),
                    _onDatavizContainer = EventColorUi(light = 0xFF6750A4.toInt(), dark = 0xFFCFBCFF.toInt()),
                    _datavizContainerVariant = EventColorUi(light = 0xFFE9DDFF.toInt(), dark = 0xFF4F378A.toInt()),
                    _onDatavizContainerVariant = EventColorUi(light = 0xFF4F378A.toInt(), dark = 0xFFE9DDFF.toInt()),
                )
            }
        }
    }
}
