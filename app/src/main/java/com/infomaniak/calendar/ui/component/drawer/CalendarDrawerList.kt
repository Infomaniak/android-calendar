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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId

@Composable
fun CalendarDrawerList(
    usersCalendars: List<UserCalendarsUi>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedAccountIds = rememberExpandedStates()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        usersCalendars.forEach { userCalendars ->
            val userId = userCalendars.user.id
            val isExpanded = expandedAccountIds.contains(userId)

            item(key = userId) {
                DrawerAccountItem(
                    modifier = Modifier.animateItem(),
                    user = userCalendars.user,
                    isExpanded = isExpanded,
                    onClick = {
                        if (isExpanded) expandedAccountIds.remove(userId) else expandedAccountIds.add(userId)
                    },
                )
            }

            userCalendars.calendars.forEach { calendar ->
                if (isExpanded) {
                    item(key = calendar.id.url) {
                        DrawerCalendarItem(
                            modifier = Modifier.animateItem(),
                            calendar = calendar,
                            onCalendarVisibilityChange = onCalendarVisibilityChange,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberExpandedStates(): SnapshotStateList<Int> {
    return rememberSaveable(saver = Saver(save = { it.toIntArray() }, restore = { it.toList().toMutableStateList() })) {
        mutableStateListOf()
    }
}

@Preview
@Composable
private fun CalendarDrawerListPreview(
    @PreviewParameter(CalendarDrawerPreviewProvider::class) usersCalendars: List<UserCalendarsUi>,
) {
    CalendarThemeForPreview {
        CalendarDrawerList(usersCalendars = usersCalendars, onCalendarVisibilityChange = { _, _ -> })
    }
}
