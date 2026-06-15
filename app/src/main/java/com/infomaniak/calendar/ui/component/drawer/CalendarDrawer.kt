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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.accountbottomsheet.R
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId

@Composable
fun CalendarDrawer(
    content: @Composable () -> Unit,
    addAnAccount: () -> Unit,
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = viewModel(),
) {
    val calendarDrawerState = LocalSharedDrawerState.current ?: return
    val calendarsUsers by drawerViewModel.calendarsUsers.collectAsState()

    CalendarDrawerContent(
        drawerState = calendarDrawerState,
        drawerState = calendarDrawerState.drawerState,
        calendarsUsers = calendarsUsers,
        onCalendarVisibilityChange = { calendarId, isVisible ->
            drawerViewModel.changeCalendarVisibility(calendarId, isVisible)
        },
        addAnAccount = addAnAccount,
        content = content,
        modifier = modifier,
    )
}

@Composable
private fun CalendarDrawerContent(
    drawerState: DrawerState,
    calendarsUsers: List<UserCalendarsUiModel>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    addAnAccount: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DrawerList(
                            usersCalendars = calendarsUsers,
                            onCalendarVisibilityChange = onCalendarVisibilityChange,
                        )
                    }
                    Button(
                        onClick = addAnAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Margin.Medium, vertical = Margin.Micro),
                    ) {
                        Text(text = stringResource(R.string.buttonAddAccount))
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

@PreviewLightDark
@Composable
private fun CalendarDrawerPreview(
    @PreviewParameter(CalendarDrawerPreviewProvider::class) calendarsUsers: List<UserCalendarsUiModel>,
) {
    CalendarThemeForPreview {
        CalendarDrawerContent(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            calendarsUsers = calendarsUsers,
            onCalendarVisibilityChange = { _, _ -> },
            addAnAccount = { },
            content = { },
        )
    }
}
