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
package com.infomaniak.calendar.components.eventdetail.component

import android.icu.text.MessageFormat
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.resources.R
import java.util.Locale

@Composable
internal fun RoomButton(
    room: EventDetailUi.Room,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    ClickableItem(
        text = room.title,
        leadingIconRes = R.drawable.ic_door_open,
        supportingContent = {
            val locale = LocalLocale.current.platformLocale

            val roomSeats = pluralStringResource(R.plurals.roomSeatsLabel, room.seats, room.seats)
            val roomFloor = stringResource(R.string.roomFloorLabel, room.floor.formatToOrdinal(locale))

            Text(text = "$roomSeats, $roomFloor")
        },
        onClick = onClick,
        contentPadding = contentPadding,
        modifier = modifier,
    )
}

private fun Int.formatToOrdinal(locale: Locale): String {
    val formatter = MessageFormat("{0, ordinal}", locale)
    return formatter.format(arrayOf(this))
}

@Preview
@Composable
private fun PreviewRoomButton() {
    MaterialTheme {
        Surface {
            RoomButton(room = EventDetailUi.Room("Japan room", seats = 5, floor = 3), onClick = {})
        }
    }
}
