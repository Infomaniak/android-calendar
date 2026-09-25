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
package com.infomaniak.calendar.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * A [Scaffold] whose top bar is drawn over the content instead of above it, so the content can scroll behind it.
 *
 * The top bar owns the top window insets, and the content padding handed to [content] accounts for the measured top bar
 * height, which lets the content stay fully reachable while still being visible under the top bar.
 */
@Composable
fun OverlaidTopBarScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }

    Scaffold(
        contentWindowInsets = contentWindowInsets,
        modifier = modifier,
    ) { scaffoldContentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(scaffoldContentPadding + PaddingValues(top = topBarHeight))

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
            ) {
                topBar()
            }
        }
    }
}
