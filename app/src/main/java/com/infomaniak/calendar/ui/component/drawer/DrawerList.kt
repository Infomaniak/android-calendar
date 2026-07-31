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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun DrawerList(
    usersCalendars: List<UserCalendarsUi>,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedAccountIds = rememberExpandedUsers()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = Margin.Medium)
            .clip(shape = EsdsTheme.radius.lg)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(all = Margin.Small),
    ) {
        usersCalendars.forEach { userCalendars ->
            val userId = userCalendars.user.id
            val isExpanded = expandedAccountIds.contains(userId)

            DrawerAccountItem(
                user = userCalendars.user,
                isExpanded = { isExpanded },
                onAccountExpanded = {
                    if (isExpanded) expandedAccountIds.remove(userId) else expandedAccountIds.add(userId)
                },
            )

            if (isExpanded) {
                userCalendars.calendars.forEach { calendar ->
                    DrawerCalendarItem(
                        calendar = calendar,
                        onCalendarVisibilityChange = onCalendarVisibilityChange,
                    )
                }
            }
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
