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
package com.infomaniak.calendar.ui.navigation.state

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

val LocalToolbarScrollableState = staticCompositionLocalOf<ToolbarScrollableState?> { null }

@Composable
fun rememberToolbarScrollableState(): ToolbarScrollableState {
    val isExpanded = rememberSaveable { mutableStateOf(true) }
    return remember { ToolbarScrollableState(isExpanded) }
}

@Stable
class ToolbarScrollableState(private val _isExpanded: MutableState<Boolean>) {
    val isExpanded: Boolean by _isExpanded

    fun expanded() {
        _isExpanded.value = true
    }

    fun unexpanded() {
        _isExpanded.value = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.scrollableToolbar(): Modifier {
    val localToolbarScrollableState: ToolbarScrollableState = LocalToolbarScrollableState.current ?: return this

    return this.then(
        Modifier.floatingToolbarVerticalNestedScroll(
            expanded = localToolbarScrollableState.isExpanded,
            onExpand = { localToolbarScrollableState.expanded() },
            onCollapse = { localToolbarScrollableState.unexpanded() },
        ),
    )
}
