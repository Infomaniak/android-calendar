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

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.navigation.state.LocalSharedDrawerState

@Composable
fun CalendarDrawer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarDrawerState = LocalSharedDrawerState.current ?: return
    CalendarDrawer(drawerState = calendarDrawerState, content = content, modifier = modifier)
}

@Composable
private fun CalendarDrawer(
    drawerState: DrawerState,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
            }
        },
        drawerState = drawerState,
        modifier = modifier,
        content = content,
    )
}

@Preview
@Composable
private fun CalendarDrawerPreview() {
    CalendarDrawer(content = { }, drawerState = rememberDrawerState(initialValue = DrawerValue.Open))
}
