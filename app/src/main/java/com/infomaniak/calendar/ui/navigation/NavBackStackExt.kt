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
package com.infomaniak.calendar.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.infomaniak.calendar.ui.navigation.replaceRoot

fun <T : NavKey> NavBackStack<T>.replaceRoot(destination: T) {
    if (isEmpty()) {
        add(destination)
        return
    }

    this[lastIndex] = destination
    while (size > 1) removeAt(0)
}

fun <T : NavKey> NavBackStack<T>.popOrReplaceRoot(fallback: T) {
    if (size <= 1) replaceRoot(fallback) else removeLastOrNull()
}

fun <T : NavKey> NavBackStack<T>.addOnce(destination: T) {
    if (lastOrNull() != destination) add(destination)
}
