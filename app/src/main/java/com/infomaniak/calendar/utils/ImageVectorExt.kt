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

import android.graphics.Bitmap
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.coroutines.EmptyCoroutineContext

/** Renders a white notification silhouette on a transparent background, without an attached Compose UI. */
internal fun ImageVector.toBitmap(density: Density): Bitmap {
    val bitmap = ImageBitmap(
        width = with(density) { defaultWidth.roundToPx() },
        height = with(density) { defaultHeight.roundToPx() },
    )
    val recomposer = Recomposer(EmptyCoroutineContext)
    val composition = Composition(
        applier = object : AbstractApplier<Unit>(Unit) {
            override fun insertTopDown(index: Int, instance: Unit) = error("No UI nodes expected")
            override fun insertBottomUp(index: Int, instance: Unit) = error("No UI nodes expected")
            override fun move(from: Int, to: Int, count: Int) = error("No UI nodes expected")
            override fun remove(index: Int, count: Int) = error("No UI nodes expected")
            override fun onClear() = Unit
        },
        parent = recomposer,
    )
    try {
        lateinit var painter: VectorPainter
        composition.setContent {
            CompositionLocalProvider(LocalDensity provides density) {
                painter = rememberVectorPainter(this@toBitmap)
            }
        }
        val size = Size(bitmap.width.toFloat(), bitmap.height.toFloat())
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), size) {
            with(painter) { draw(size, colorFilter = ColorFilter.tint(Color.White)) }
        }
        return bitmap.asAndroidBitmap()
    } finally {
        composition.dispose()
        recomposer.cancel()
    }
}
