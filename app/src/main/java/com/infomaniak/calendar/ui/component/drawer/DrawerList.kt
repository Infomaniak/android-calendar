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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.component.drawer.model.UserCalendarsUi
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.parcelize.Parcelize

fun LazyListScope.drawerListItems(
    usersCalendars: List<UserCalendarsUi>,
    onAccountExpandedChange: (userId: Int, isExpanded: Boolean) -> Unit,
    isAccountExpanded: (Int) -> Boolean,
    onSectionExpandedChange: (sectionKey: String, isExpanded: Boolean) -> Unit,
    isSectionExpanded: (String) -> Boolean,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    onCalendarLongClick: (CalendarId) -> Unit,
) {
    usersCalendars.forEachIndexed { index, userCalendars ->
        accountItems(
            userCalendars = userCalendars,
            isFirstAccount = index == 0,
            isExpanded = isAccountExpanded(userCalendars.user.id),
            onAccountExpandedChange = onAccountExpandedChange,
            isSectionExpanded = isSectionExpanded,
            onSectionExpandedChange = onSectionExpandedChange,
            onCalendarVisibilityChange = onCalendarVisibilityChange,
            onCalendarLongClick = onCalendarLongClick,
        )
    }
}

private fun LazyListScope.accountItems(
    userCalendars: UserCalendarsUi,
    isFirstAccount: Boolean,
    isExpanded: Boolean,
    onAccountExpandedChange: (userId: Int, isExpanded: Boolean) -> Unit,
    isSectionExpanded: (String) -> Boolean,
    onSectionExpandedChange: (sectionKey: String, isExpanded: Boolean) -> Unit,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    onCalendarLongClick: (CalendarId) -> Unit,
) {
    val userId = userCalendars.user.id
    val sections = userCalendars.sections
    val showsSectionHeaders = sections.size > 1
    val hasRowsBelow = isExpanded && (showsSectionHeaders || sections.any { it.calendars.isNotEmpty() })

    item(key = "account/$userId", contentType = "account") {
        DrawerAccountItem(
            user = userCalendars.user,
            isExpanded = { isExpanded },
            onAccountExpanded = { onAccountExpandedChange(userId, !isExpanded) },
            shapes = manuallyAnimatedSegmentedShapes(
                top = SegmentCorner.Outer,
                bottom = if (hasRowsBelow) SegmentCorner.Inner else SegmentCorner.Outer,
            ),
            modifier = Modifier
                .animateItem()
                .padding(horizontal = Margin.Medium)
                .padding(top = if (isFirstAccount) Margin.Mini else Margin.Medium),
        )
    }

    if (!isExpanded) return

    sections.forEachIndexed { sectionIndex, section ->
        val lastCorner = if (sectionIndex == sections.lastIndex) SegmentCorner.Outer else SegmentCorner.Inner
        val sectionExpanded = !showsSectionHeaders || isSectionExpanded(section.key)
        val calendars = if (sectionExpanded) section.calendars else emptyList()

        if (showsSectionHeaders) {
            item(key = "section/${section.key}", contentType = "section") {
                DrawerSectionHeader(
                    organisationName = section.organisationName,
                    isExpanded = { sectionExpanded },
                    onExpandedChange = { onSectionExpandedChange(section.key, it) },
                    shapes = manuallyAnimatedSegmentedShapes(
                        top = SegmentCorner.Inner,
                        bottom = if (calendars.isEmpty()) lastCorner else SegmentCorner.None,
                    ),
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = Margin.Medium)
                        .padding(top = ListItemDefaults.SegmentedGap),
                )
            }
        }

        calendars.forEachIndexed { calendarIndex, calendar ->
            val isFirstRow = !showsSectionHeaders && calendarIndex == 0

            item(key = "calendar/$userId/${calendar.id.url}", contentType = "calendar") {
                DrawerCalendarItem(
                    calendar = calendar,
                    onCalendarVisibilityChange = onCalendarVisibilityChange,
                    onCalendarLongClick = onCalendarLongClick,
                    shapes = manuallyAnimatedSegmentedShapes(
                        top = if (isFirstRow) SegmentCorner.Inner else SegmentCorner.None,
                        bottom = if (calendarIndex == calendars.lastIndex) lastCorner else SegmentCorner.None,
                    ),
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = Margin.Medium)
                        .padding(top = if (isFirstRow) ListItemDefaults.SegmentedGap else 0.dp),
                )
            }
        }
    }
}

@Composable
private fun DrawerListPreviewContent(
    usersCalendars: List<UserCalendarsUi>,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        drawerListItems(
            usersCalendars = usersCalendars,
            onAccountExpandedChange = { _, _ -> {} },
            isAccountExpanded = { false },
            onSectionExpandedChange = { _, _ -> },
            isSectionExpanded = { true },
            onCalendarVisibilityChange = { _, _ -> },
            onCalendarLongClick = {},
        )
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
        DrawerListPreviewContent(usersCalendars)
    }
}
