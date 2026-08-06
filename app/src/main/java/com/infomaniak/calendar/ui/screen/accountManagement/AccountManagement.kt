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
package com.infomaniak.calendar.ui.screen.accountManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.component.drawer.DrawerPreviewProvider
import com.infomaniak.calendar.ui.component.drawer.DrawerViewModel
import com.infomaniak.calendar.ui.component.drawer.model.UserCalendarsUi
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun AccountManagement(
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val calendarsUsers by drawerViewModel.calendarsUsers.collectAsStateWithLifecycle()
    AccountManagementContent(
        calendarsUsers = calendarsUsers,
        modifier = modifier,
        onAddAccount = onAddAccount,
        onBack = onBack,
    )
}

@Composable
private fun AccountManagementContent(
    calendarsUsers: List<UserCalendarsUi>,
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.accountManagement)) },
                navigationIcon = { TopAppBarButtons.BackButton(onClick = onBack) },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(
                items = calendarsUsers,
                key = { it.user.id },
            ) { userCalendars ->
                AccountItem(user = userCalendars.user)
            }

            item {
                TextButton(
                    onClick = onAddAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Margin.Medium, vertical = Margin.Small)
                        .clip(EsdsTheme.radius.full)
                        .background(color = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(stringResource(R.string.addAccount))
                }
            }
        }
    }
}

@Preview
@Composable
private fun AccountManagementPreview(
    @PreviewParameter(DrawerPreviewProvider::class) calendarsUsers: List<UserCalendarsUi>,
) {
    CalendarThemeForPreview {
        AccountManagementContent(calendarsUsers = calendarsUsers, onBack = {}, onAddAccount = {})
    }
}
