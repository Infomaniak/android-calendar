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
package com.infomaniak.calendar.components.attendeessearch.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees

@Composable
internal fun AttendeesList(
    attendees: () -> List<AttendeeUi>,
    modifier: Modifier = Modifier,
    onClick: ((AttendeeUi) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        items(
            items = attendees(),
            key = { attendee -> attendee.key },
        ) { attendee ->
            val isDisplayingSearchResults = onClick != null // TODO: Make the input more clear
            EventAttendee(
                attendee = attendee,
                onClick = onClick?.let { { it(attendee) } },
                enabled = attendee.status == null || !isDisplayingSearchResults,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Preview
@Composable
private fun AttendeesListPreview() {
    AttendeesList(
        attendees = { previewAttendees },
    )
}
