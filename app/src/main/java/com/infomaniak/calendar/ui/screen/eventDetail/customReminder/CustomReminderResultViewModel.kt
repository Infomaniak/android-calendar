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
package com.infomaniak.calendar.ui.screen.eventDetail.customReminder

import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.ui.navigation.SharedViewModelStoreNavEntryDecorator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

/**
 * Hands the reminder made in the custom reminder screen back to its parent,
 * shared between them by a [SharedViewModelStoreNavEntryDecorator].
 *
 * A [Channel] keeps the reminder until the parent collects it.
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class CustomReminderResultViewModel : ViewModel() {
    private val addedReminders = Channel<EventDetailUi.Notification>(
        capacity = Channel.BUFFERED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun addReminder(notification: EventDetailUi.Notification) {
        addedReminders.trySend(notification)
    }

    suspend fun applyAddedRemindersTo(eventFormState: EventFormState?) {
        eventFormState ?: return

        for (notification in addedReminders) eventFormState.notifications.value += notification
    }
}
