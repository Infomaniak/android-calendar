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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.calendar.ui.component.drawer.model.UserCalendarsUi
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.parcelize.Parcelize

fun LazyListScope.drawerListItems(
    usersCalendars: List<UserCalendarsUi>,
    onAccountExpandedChange: (userId: Int, isExpanded: Boolean) -> Unit,
    expandedAccountIds: Set<Int>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
) {
    usersCalendars.forEach { userCalendars ->
        calendarSection(userCalendars, expandedAccountIds, onAccountExpandedChange, onCalendarVisibilityChange)
    }
}

private fun LazyListScope.calendarSection(
    userCalendars: UserCalendarsUi,
    expandedAccountIds: Set<Int>,
    onAccountExpandedChange: (userId: Int, isExpanded: Boolean) -> Unit,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
) {
    val userId = userCalendars.user.id
    val isExpanded = expandedAccountIds.contains(userId)

    item(key = userId) {
        Column(
            modifier = Modifier
                .padding(horizontal = Margin.Medium, vertical = Margin.Mini)
                .clip(EsdsTheme.radius.twoXl)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .animateItem()
                .animateContentSize(),
        ) {
            DrawerAccountItem(
                modifier = Modifier.animateItem(),
                user = userCalendars.user,
                isExpanded = { isExpanded },
                onAccountExpanded = { onAccountExpandedChange(userId, !isExpanded) },
            )
            if (isExpanded) {
                Column {
                    userCalendars.calendars.forEachIndexed { index, calendar ->
                        DrawerCalendarItem(
                            modifier = Modifier
                                .animateItem(),
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
fun DrawerList(
    usersCalendars: List<UserCalendarsUi>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedAccountIds = rememberExpandedUsers()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        drawerListItems(
            usersCalendars = usersCalendars,
            onAccountExpandedChange = { userId, isExpanded ->
                if (isExpanded) expandedAccountIds.add(userId) else expandedAccountIds.remove(userId)
            },
            expandedAccountIds = expandedAccountIds,
            onCalendarVisibilityChange = onCalendarVisibilityChange,
        )
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
