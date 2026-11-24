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
package com.infomaniak.calendar.manager

import com.infomaniak.calendar.components.event.EventQuickAction
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.Inject

/** Runs the action picked in an event's quick actions menu, the same way for every calendar view. */
@Inject
class EventQuickActionHandler(private val backgroundEventComposer: BackgroundEventComposer) {
    fun handle(occurrenceId: OccurrenceId, action: EventQuickAction) {
        when (action) {
            EventQuickAction.Duplicate -> Unit // TODO[quickActions]
            EventQuickAction.Copy -> Unit // TODO[quickActions]
            EventQuickAction.Delete -> backgroundEventComposer.deleteEvent(occurrenceId, RecurrentEventScope.ThisEvent)
        }
    }
}
