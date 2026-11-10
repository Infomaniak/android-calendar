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
package com.infomaniak.calendar.components.foundation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

/** Requests focus once on first entry if [shouldRequestFocus], but not again after a configuration change. */
@Composable
fun rememberInitialFocusRequester(shouldRequestFocus: () -> Boolean = { true }): FocusRequester {
    val focusRequester = remember { FocusRequester() }
    var hasHandledInitialFocus by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasHandledInitialFocus && shouldRequestFocus()) focusRequester.requestFocus()
        hasHandledInitialFocus = true
    }
    return focusRequester
}
