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

import android.os.Parcelable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.parcelize.Parcelize

@Composable
fun DrawerList(
    usersCalendars: List<UserCalendarsUi>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedAccountIds = rememberExpandedUsers()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        usersCalendars.forEach { userCalendars ->
            calendarSection(userCalendars, expandedAccountIds, onCalendarVisibilityChange)
        }
    }
}

private fun LazyListScope.calendarSection(
    userCalendars: UserCalendarsUi,
    expandedAccountIds: SnapshotStateSet<Int>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
) {
    val userId = userCalendars.user.id
    val isExpanded = expandedAccountIds.contains(userId)

    item(key = userId) {
        DrawerAccountItem(
            modifier = Modifier.animateItem(),
            user = userCalendars.user,
            isExpanded = { isExpanded },
            onAccountExpanded = {
                if (isExpanded) expandedAccountIds.remove(userId) else expandedAccountIds.add(userId)
            },
        )
    }

    if (isExpanded) {
        items(items = userCalendars.calendars, key = { calendar -> CalendarKey(calendar.id.url, userId) }) { calendar ->
            DrawerCalendarItem(
                modifier = Modifier.animateItem(),
                calendar = calendar,
                onCalendarVisibilityChange = onCalendarVisibilityChange,
            )
        }
    }
}

@Composable
private fun rememberExpandedUsers(): SnapshotStateSet<Int> {
    return rememberSaveable(
        saver = Saver(
            save = { it.toIntArray() },
            restore = { mutableStateSetOf<Int>().apply { addAll(it.toList()) } },
        ),
    ) {
        mutableStateSetOf()
    }
}

@Parcelize
private data class CalendarKey(val calendarUrl: String, val userId: Int) : Parcelable

@Preview
@Composable
private fun DrawerListPreview(
    @PreviewParameter(DrawerPreviewProvider::class) usersCalendars: List<UserCalendarsUi>,
) {
    CalendarThemeForPreview {
        DrawerList(usersCalendars = usersCalendars, onCalendarVisibilityChange = { _, _ -> })
    }
}
