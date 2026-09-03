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
package com.infomaniak.calendar.ui.screen.accounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.core.common.R as RCore

@Composable
fun AccountActionsScreen(
    userId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    accountsViewModel: AccountsViewModel = viewModel(),
    drawerViewModel: DrawerViewModel = viewModel(),
) {
    val calendarsUsers by drawerViewModel.calendarsUsers.collectAsStateWithLifecycle()
    val user = calendarsUsers.firstOrNull { it.user.id == userId }?.user ?: return

    LaunchedEffect(accountsViewModel) {
        for (event in accountsViewModel.navigateBack) {
            onBack()
        }
    }

    AccountsActionsScreen(
        user = user,
        onBack = onBack,
        removeUser = { accountsViewModel.removeUser(user.id) },
        modifier = modifier,
    )
}

@Composable
private fun AccountsActionsScreen(
    user: User,
    onBack: () -> Unit,
    removeUser: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user.displayName.toString()) },
                navigationIcon = { TopAppBarButtons.BackButton(onClick = onBack) },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxHeight(),
        ) {
            AccountItem(user = user, onClick = null)
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                shape = EsdsTheme.radius.full,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = EsdsTheme.spacing.xl),
            ) {
                Text(
                    stringResource(R.string.buttonLogOut),
                    modifier = Modifier.padding(vertical = EsdsTheme.spacing.md),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        if (showLogoutDialog) {
            LogoutDialog(
                user = user,
                onDismiss = { showLogoutDialog = false },
                onRemoveAccount = removeUser,
            )
        }
    }
}

@Composable
private fun LogoutDialog(
    user: User,
    onDismiss: () -> Unit,
    onRemoveAccount: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(RCore.string.confirmLogoutTitle)) },
        text = { Text(stringResource(RCore.string.confirmLogoutDescription, "${user.firstname} ${user.lastname}")) },
        confirmButton = {
            TextButton(
                onClick = {
                    onRemoveAccount()
                    onDismiss()
                },
            ) {
                Text(stringResource(RCore.string.buttonLoginOut), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(RCore.string.buttonCancel)) }
        },
    )
}

@Preview
@Composable
private fun AccountsPreview(
    @PreviewParameter(DrawerPreviewProvider::class) calendarsUsers: List<UserCalendarsUi>,
) {
    CalendarThemeForPreview {
        AccountsActionsScreen(user = calendarsUsers.first().user, onBack = {}, removeUser = {})
    }
}
