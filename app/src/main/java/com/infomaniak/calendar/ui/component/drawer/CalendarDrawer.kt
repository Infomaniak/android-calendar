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
package com.infomaniak.calendar.ui.component.drawer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState

@Composable
fun CalendarDrawer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    addAnAccount: () -> Unit,
) {
    val calendarDrawerState = LocalDrawerState.current ?: return
    CalendarDrawer(
        drawerState = calendarDrawerState,
        content = content,
        addAnAccount = addAnAccount,
        modifier = modifier,
    )
}

@Composable
private fun CalendarDrawer(
    drawerState: DrawerState,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DrawerViewModel = viewModel(),
    addAnAccount: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (val state = uiState) {
                            is DrawerUiState.Loading -> {
                                Text(
                                    text = "Chargement...",
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                            is DrawerUiState.Success -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(state.data) { userCalendars ->
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = userCalendars.user.email,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            userCalendars.calendars.forEach { calendar ->
                                                Text(
                                                    text = calendar.displayName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        onClick = addAnAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(text = "Ajouter un compte")
                    }
                }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        modifier = modifier,
    ) {
        content()
    }
}

@Preview
@Composable
private fun CalendarDrawerPreview() {
    CalendarDrawer(content = { }, drawerState = rememberDrawerState(initialValue = DrawerValue.Open), addAnAccount = { })
}
