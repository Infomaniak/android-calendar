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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.navigation.state.LocalSharedDrawerState

@Composable
fun CalendarScaffoldWithMenuIcon(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    val calendarDrawerState = LocalSharedDrawerState.current
                    IconButton(
                        onClick = {
                            calendarDrawerState?.openDrawer()
                        },
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.contentDescriptionMenuDrawer))
                    }
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Preview
@Composable
private fun CalendarScaffoldWithMenuIconPreview() {
    CalendarScaffoldWithMenuIcon(title = stringResource(R.string.planningTitle), content = { })
}
