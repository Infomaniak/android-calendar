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
package com.infomaniak.calendar.ui.component.topAppBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

private val LoadingIndicatorHeight = 4.dp

@Composable
fun LoadingEventsIndicator(isLoading: () -> Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(LoadingIndicatorHeight)) {
        AnimatedVisibility(visible = isLoading(), enter = fadeIn(), exit = fadeOut()) {
            LinearProgressIndicator(Modifier.fillMaxSize())
        }
    }
}

@Composable
@Preview
private fun LoadingEventsIndicatorPreview() {
    CalendarThemeForPreview {
        LoadingEventsIndicator(isLoading = { true }, modifier = Modifier.padding(24.dp))
    }
}
