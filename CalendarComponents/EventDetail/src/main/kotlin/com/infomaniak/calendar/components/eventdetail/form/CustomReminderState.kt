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
package com.infomaniak.calendar.components.eventdetail.form

import androidx.annotation.PluralsRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.NotificationTime
import com.infomaniak.calendar.components.resources.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Stable
class CustomReminderState(
    val amountTextFieldState: TextFieldState,
    val unit: MutableState<ReminderUnit>,
    val type: MutableState<EventDetailUi.Notification.Type>,
) {
    val amount: Int? get() = amountTextFieldState.text.toString().toIntOrNull()?.takeIf { it > 0 }

    fun toNotification(): EventDetailUi.Notification? {
        val amount = amount ?: return null
        return EventDetailUi.Notification(type = type.value, time = NotificationTime.Offset(-unit.value.duration * amount))
    }
}

enum class ReminderUnit(val duration: Duration, @PluralsRes val labelRes: Int) {
    Minutes(1.minutes, R.plurals.unitMinutes),
    Hours(1.hours, R.plurals.unitHours),
    Days(1.days, R.plurals.unitDays),
}

@Composable
fun rememberCustomReminderState(): CustomReminderState = CustomReminderState(
    amountTextFieldState = rememberTextFieldState("30"),
    unit = rememberSaveable { mutableStateOf(ReminderUnit.Minutes) },
    type = rememberSaveable { mutableStateOf(EventDetailUi.Notification.Type.Push) },
)
