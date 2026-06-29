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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.models.user.preferences.OrganizationPreference
import com.infomaniak.core.auth.models.user.preferences.Preferences
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarColor
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId

class CalendarDrawerPreviewProvider : PreviewParameterProvider<List<UserCalendarsUi>> {
    override val values = sequenceOf(Loaded)

    companion object {
        private val Loaded = listOf(
            UserCalendarsUi(
                user = User(
                    id = 1,
                    displayName = "John Doe",
                    firstname = "John",
                    lastname = "Doe",
                    email = "john.doe@infomaniak.com",
                    avatar = null,
                    login = "johndoe",
                    isStaff = false,
                    preferences = Preferences(organizationPreference = OrganizationPreference(currentOrganizationId = 1)),
                ),
                calendars = listOf(
                    Calendar(
                        id = CalendarId("1"),
                        accountId = AccountId(1),
                        displayName = "Private",
                        color = CalendarColor(0xFF2196F3.toInt()),
                        isVisible = true,
                    ),
                    Calendar(
                        id = CalendarId("2"),
                        accountId = AccountId(1),
                        displayName = "Work",
                        color = CalendarColor(0xFFE91E63.toInt()),
                        isVisible = false,
                    ),
                ),
            ),
            UserCalendarsUi(
                user = User(
                    id = 2,
                    displayName = "Jane Smith",
                    firstname = "Jane",
                    lastname = "Smith",
                    email = "jane.smith@infomaniak.com",
                    avatar = null,
                    login = "janesmith",
                    isStaff = false,
                    preferences = Preferences(organizationPreference = OrganizationPreference(currentOrganizationId = 1)),
                ),
                calendars = listOf(
                    Calendar(
                        id = CalendarId("3"),
                        accountId = AccountId(2),
                        displayName = "Team",
                        color = CalendarColor(0xFF4CAF50.toInt()),
                        isVisible = true,
                    ),
                ),
            ),
        )
    }
}
