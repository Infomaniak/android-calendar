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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.component.drawer.model.UserCalendarsUi
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.coroutines.launch

@Composable
fun CalendarDrawer(
    content: @Composable () -> Unit,
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = viewModel(),
) {
    val calendarDrawerState = LocalDrawerState.current ?: return
    val calendarsUsers by drawerViewModel.calendarsUsers.collectAsStateWithLifecycle()
    val expandedAccountIds by drawerViewModel.expandedAccountIds.collectAsStateWithLifecycle(emptySet())
    val scope = rememberCoroutineScope()

    BackHandler(enabled = calendarDrawerState.isOpen) { scope.launch { calendarDrawerState.close() } }

    CalendarDrawerContent(
        drawerState = calendarDrawerState,
        calendarsUsers = calendarsUsers,
        onCalendarVisibilityChanged = drawerViewModel::onCalendarVisibilityChanged,
        onAddAccount = onAddAccount,
        content = content,
        onAccountExpandedChange = drawerViewModel::onAccountExpandedChanged,
        expandedAccountIds = expandedAccountIds,
        modifier = modifier,
    )
}

@Composable
private fun CalendarDrawerContent(
    drawerState: DrawerState,
    calendarsUsers: List<UserCalendarsUi>,
    onCalendarVisibilityChanged: (CalendarId, Boolean) -> Unit,
    onAddAccount: () -> Unit,
    onAccountExpandedChange: (userId: Int, isExpanded: Boolean) -> Unit,
    expandedAccountIds: Set<Int>,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuOptions = drawerMenuOptions(
        onManageAccounts = {},
        onSettings = {},
        onHelp = {},
    )

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(),
                ) {
                    drawerListItems(
                        usersCalendars = calendarsUsers,
                        onCalendarVisibilityChange = onCalendarVisibilityChanged,
                        expandedAccountIds = expandedAccountIds,
                        onAccountExpandedChange = onAccountExpandedChange,
                    )
                    item {
                        MenuList(menuOptions = menuOptions)
                    }

                    // item {
                    //     Button(
                    //         onClick = onAddAccount,
                    //         modifier = Modifier
                    //             .fillMaxWidth()
                    //             .padding(horizontal = Margin.Medium, vertical = Margin.Micro),
                    //     ) {
                    //         Text(text = "Add an account")
                    //     }
                    // }
                }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        modifier = modifier,
        content = content,
    )
}

private fun drawerMenuOptions(
    onManageAccounts: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
): List<MenuOption> {
    return listOf(
        MenuOption(
            itemNameRes = R.string.accountManagement,
            itemIcon = R.drawable.ic_circle_user,
            itemAction = onManageAccounts,
        ),
        MenuOption(
            itemNameRes = R.string.settingsTitle,
            itemIcon = R.drawable.ic_cog,
            itemAction = onSettings,
        ),
        MenuOption(
            itemNameRes = R.string.helpTitle,
            itemIcon = R.drawable.ic_headset,
            itemAction = onHelp,
        ),
    )
}

@PreviewLightDark
@Composable
private fun CalendarDrawerPreview(
    @PreviewParameter(DrawerPreviewProvider::class) usersCalendars: List<UserCalendarsUi>,
) {
    CalendarThemeForPreview {
        CalendarDrawerContent(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            calendarsUsers = usersCalendars,
            onCalendarVisibilityChanged = { _, _ -> },
            onAddAccount = {},
            onAccountExpandedChange = { _, _ -> },
            expandedAccountIds = emptySet(),
            content = {},
            modifier = Modifier,
        )
    }
}
