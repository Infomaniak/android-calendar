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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.accountbottomsheet.R
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId

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
    addAnAccount: () -> Unit,
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = viewModel(),
) {
    val calendarsUsers by drawerViewModel.calendarsUsers.collectAsState()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DrawerCalendarList(
                            usersCalendars = calendarsUsers,
                            onCalendarVisibilityChange = { calendarId, isVisible ->
                                drawerViewModel.changeCalendarVisibility(calendarId, isVisible)
                            },
                        )
                    }
                    Button(
                        onClick = addAnAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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

@Composable
private fun DrawerCalendarList(
    usersCalendars: List<UserCalendarsUiModel>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(usersCalendars) { userCalendars ->
            var isExpanded by remember { mutableStateOf(true) }

            Column {
                AccountItem(
                    user = userCalendars.user,
                    isExpanded = isExpanded,
                    onClick = { isExpanded = !isExpanded },
                )
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        userCalendars.calendars.forEach { calendar ->
                            CalendarItem(calendar, onCalendarVisibilityChange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarItem(calendar: Calendar, onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit, modifier: Modifier = Modifier) {
    var isChecked by remember { mutableStateOf(calendar.isVisible) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isChecked,
                onValueChange = {
                    isChecked = it
                    onCalendarVisibilityChange(calendar.id, it)
                },
                role = Role.Checkbox,
            )
            .padding(horizontal = Margin.Medium, vertical = Margin.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(calendar.color),
                checkmarkColor = Color.White,
            ),
        )

        Text(
            text = calendar.displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = Margin.Mini),
        )
    }
}

@Composable
fun AccountItem(
    user: User,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "chevron_rotation",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Margin.Medium, horizontal = Margin.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        Avatar(avatarType = AvatarType.fromUser(user), Modifier.size(32.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = user.displayName.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.email,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation),
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun CalendarDrawerPreview() {
    CalendarDrawer(content = { }, drawerState = rememberDrawerState(initialValue = DrawerValue.Open), addAnAccount = { })
}
