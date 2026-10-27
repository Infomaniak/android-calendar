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
package com.infomaniak.calendar.utils

import android.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CalendarNumberOne
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageVectorExtTest {

    @Test
    fun calendarIconRendersWithTransparentBackgroundAtDeviceDensity() {
        val icon = EsdsIcons.CalendarNumberOne.Medium

        for (scale in listOf(1f, 3f)) {
            val density = Density(scale)
            val bitmap = icon.toBitmap(density)
            assertEquals(with(density) { icon.defaultWidth.roundToPx() }, bitmap.width)
            assertEquals(with(density) { icon.defaultHeight.roundToPx() }, bitmap.height)

            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            assertTrue(pixels.any { Color.alpha(it) == 0 })
            assertTrue(pixels.any { Color.alpha(it) > 0 })
            assertTrue(pixels.filter { Color.alpha(it) > 0 }.all {
                Color.red(it) == 255 && Color.green(it) == 255 && Color.blue(it) == 255
            })
        }
    }
}
