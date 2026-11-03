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
package com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch

import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.manager.BackgroundEventComposer
import com.infomaniak.calendar.ui.screen.eventDetail.EventComposerUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.runBlocking

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class EditableAttendeesSearchViewModel(private val backgroundEventComposer: BackgroundEventComposer) : ViewModel() {
    private var hasEdited = false

    fun updateAttendees(occurrenceId: OccurrenceId, attendees: List<AttendeeUi>, scope: RecurrentEventScope): Boolean {
        if (hasEdited) return false
        hasEdited = true

        backgroundEventComposer.updateAttendees(occurrenceId, scope, attendees)

        return true
    }
}
