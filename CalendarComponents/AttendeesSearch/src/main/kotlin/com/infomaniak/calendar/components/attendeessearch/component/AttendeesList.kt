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

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.attendeessearch.component.ItemKey.Companion.itemKey
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Envelope
import kotlinx.parcelize.Parcelize

@Composable
internal fun AttendeesList(
    results: () -> List<AttendeesSearchResult>,
    query: () -> String,
    onAddCustomAttendee: () -> Unit,
    onRemove: (AttendeeUi) -> Unit,
    modifier: Modifier = Modifier,
    onClick: ((AttendeeUi) -> Unit)? = null,
    isEnabled: (AttendeeUi) -> Boolean = { true },
    enableDeletion: () -> Boolean = { true },
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        items(
            items = results(),
            key = { item -> item.itemKey },
        ) { item ->
            when (item) {
                is AttendeeUi -> SwipeableEventAttendee(
                    attendee = item,
                    onDismiss = { onRemove(item) },
                    onClick = onClick?.let { { it(item) } },
                    enabled = isEnabled(item),
                    enableDismissFromEndToStart = enableDeletion,
                    modifier = Modifier.animateItem(),
                )
                AttendeesSearchResult.AddCustomAttendee -> AddCustomAttendee(
                    query = query,
                    onClick = onAddCustomAttendee,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
internal fun AddCustomAttendee(query: () -> String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            Box(
                Modifier
                    .size(EsdsTheme.icon.sizeXl)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColorScheme.datavizGray, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = EsdsIcons.Envelope.Medium,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColorScheme.onDatavizGray,
                )
            }
        },
        content = { Text(query()) },
        supportingContent = { Text("TODO") },
    )
}

private sealed interface ItemKey : Parcelable {
    @Parcelize
    data class Attendee(val key: String) : ItemKey
    @Parcelize
    object AddCustomAttendee : ItemKey

    companion object {
        val AttendeesSearchResult.itemKey: ItemKey
            get() = when (this) {
                is AttendeeUi -> Attendee(key)
                AttendeesSearchResult.AddCustomAttendee -> AddCustomAttendee
            }
    }
}

@Preview
@Composable
private fun AttendeesListPreview() {
    AttendeesList(
        results = { previewAttendees + AttendeesSearchResult.AddCustomAttendee },
        query = { "Bob" },
        onAddCustomAttendee = {},
        onRemove = {},
    )
}
