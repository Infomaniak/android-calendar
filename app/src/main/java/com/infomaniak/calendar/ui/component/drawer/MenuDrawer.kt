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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ScaffoldDefaults
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
import com.infomaniak.calendar.ui.model.UserCalendarsUi
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleUser
import com.infomaniak.designsystem.icons.symbols.Cog
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.coroutines.launch

@Composable
fun MenuDrawer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onManageAccounts: () -> Unit = {},
    goToSettings: () -> Unit = {},
    menuDrawerViewModel: MenuDrawerViewModel = viewModel(),
) {
    val calendarDrawerState = LocalDrawerState.current ?: return
    val calendarsUsers by menuDrawerViewModel.calendarsUsers.collectAsStateWithLifecycle()
    val expandedAccountIds by menuDrawerViewModel.expandedAccountIds.collectAsStateWithLifecycle(emptySet())
    val collapsedSectionKeys by menuDrawerViewModel.collapsedSectionKeys.collectAsStateWithLifecycle(emptySet())
    val scope = rememberCoroutineScope()

    BackHandler(enabled = calendarDrawerState.isOpen) { scope.launch { calendarDrawerState.close() } }

    MenuDrawer(
        drawerState = calendarDrawerState,
        calendarsUsers = calendarsUsers,
        onCalendarVisibilityChanged = menuDrawerViewModel::onCalendarVisibilityChanged,
        onCalendarLongClick = menuDrawerViewModel::onCalendarLongClicked,
        content = content,
        onAccountExpandedChange = menuDrawerViewModel::onAccountExpandedChanged,
        isAccountExpanded = { expandedAccountIds.contains(it) },
        onSectionExpandedChange = menuDrawerViewModel::onSectionExpandedChanged,
        isSectionExpanded = { !collapsedSectionKeys.contains(it) },
        onManageAccounts = {
            scope.launch {
                calendarDrawerState.close()
                onManageAccounts()
            }
        },
        onSettingsClick = {
            scope.launch {
                calendarDrawerState.close()
                goToSettings()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun MenuDrawer(
    drawerState: DrawerState,
    calendarsUsers: List<UserCalendarsUi>,
    onCalendarVisibilityChanged: (CalendarId, Boolean) -> Unit,
    onCalendarLongClick: (CalendarId) -> Unit,
    onAccountExpandedChange: (userId: Int, isExpanded: Boolean) -> Unit,
    isAccountExpanded: (Int) -> Boolean,
    onSectionExpandedChange: (sectionKey: String, isExpanded: Boolean) -> Unit,
    isSectionExpanded: (String) -> Boolean,
    onManageAccounts: () -> Unit,
    onSettingsClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuOptions = drawerMenuOptions(
        onManageAccounts = onManageAccounts,
        onSettingsClick = onSettingsClick,
    )

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                windowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    contentPadding = PaddingValues(
                        start = EsdsTheme.spacing.xl,
                        end = EsdsTheme.spacing.xl,
                        bottom = EsdsTheme.spacing.threeXl,
                    ),
                ) {
                    drawerListItems(
                        usersCalendars = calendarsUsers,
                        onCalendarVisibilityChange = onCalendarVisibilityChanged,
                        onCalendarLongClick = onCalendarLongClick,
                        isAccountExpanded = isAccountExpanded,
                        onAccountExpandedChange = onAccountExpandedChange,
                        isSectionExpanded = isSectionExpanded,
                        onSectionExpandedChange = onSectionExpandedChange,
                    )
                    item(key = "menu", contentType = "menu") {
                        MenuList(
                            menuOptions = menuOptions,
                            modifier = Modifier
                                .animateItem()
                                .padding(top = EsdsTheme.spacing.fourXl),
                        )
                    }
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
    onSettingsClick: () -> Unit,
): List<MenuOption> {
    return listOf(
        MenuOption(
            itemNameRes = R.string.accountsTitle,
            itemIcon = EsdsIcons.CircleUser,
            itemAction = onManageAccounts,
        ),
        MenuOption(
            itemNameRes = R.string.settingsTitle,
            itemIcon = EsdsIcons.Cog,
            itemAction = onSettingsClick,
        ),
    )
}

@PreviewLightDark
@Composable
private fun MenuDrawerPreview(
    @PreviewParameter(DrawerPreviewProvider::class) usersCalendars: List<UserCalendarsUi>,
) {
    CalendarThemeForPreview {
        MenuDrawer(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            calendarsUsers = usersCalendars,
            onCalendarVisibilityChanged = { _, _ -> },
            onCalendarLongClick = {},
            onAccountExpandedChange = { _, _ -> },
            isAccountExpanded = { false },
            onSectionExpandedChange = { _, _ -> },
            isSectionExpanded = { true },
            onManageAccounts = {},
            onSettingsClick = {},
            content = {},
            modifier = Modifier,
        )
    }
}
