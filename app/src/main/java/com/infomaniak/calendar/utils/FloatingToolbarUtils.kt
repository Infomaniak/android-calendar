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

import androidx.compose.animation.core.animate
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberFloatingToolbarExitScrollBehavior(
    canScrollForward: Boolean,
    enabled: Boolean = hasSmallHeightScreen(),
): FloatingToolbarScrollBehavior? {
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.Bottom)

    LaunchedEffect(enabled, canScrollForward) {
        if (enabled && !canScrollForward) {
            val toolbarState = scrollBehavior.state
            animate(initialValue = toolbarState.offset, targetValue = 0f) { value, _ -> toolbarState.offset = value }
        }
    }

    return scrollBehavior.takeIf { enabled }
}

@Composable
private fun hasSmallHeightScreen(): Boolean {
    return !currentWindowAdaptiveInfo().windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun Modifier.nestedScrollToolbar(floatingToolbarScrollBehavior: FloatingToolbarScrollBehavior?): Modifier {
    if (floatingToolbarScrollBehavior == null) return this
    return nestedScroll(floatingToolbarScrollBehavior)
}
