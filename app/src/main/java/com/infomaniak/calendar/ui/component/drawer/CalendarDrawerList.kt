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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId

@Composable
fun CalendarDrawerList(
    usersCalendars: UsersCalendarsList,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedAccountIds = rememberExpandedStates()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        usersCalendars.items.forEach { userCalendars ->
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
fun rememberExpandedStates(): SnapshotStateList<Int> {
    return rememberSaveable(
        saver = Saver(
            save = { it.toIntArray() },
            restore = { it.toList().toMutableStateList() },
        ),
    ) {
        mutableStateListOf()
    }
}

@Composable
private fun DrawerAccountItem(
    user: User,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 90f else 0f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(all = Margin.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        Avatar(avatarType = AvatarType.fromUser(user), Modifier.size(32.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = user.displayName.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
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
            modifier = Modifier.rotate(rotation),
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
        )
    }
}

@Composable
private fun DrawerCalendarItem(
    calendar: Calendar,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isChecked by remember(calendar.id, calendar.isVisible) { mutableStateOf(calendar.isVisible) }

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
        horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(calendar.color.argb),
                checkmarkColor = Color.White,
            ),
        )

        Text(text = calendar.displayName, style = MaterialTheme.typography.bodyMedium)
    }
}
