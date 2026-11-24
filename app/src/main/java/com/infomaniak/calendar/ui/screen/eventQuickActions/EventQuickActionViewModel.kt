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
package com.infomaniak.calendar.ui.screen.eventQuickActions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.infomaniak.calendar.components.event.EventQuickAction
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.manager.BackgroundEventComposer
import com.infomaniak.calendar.ui.model.isRecurring
import com.infomaniak.calendar.ui.model.occurrenceId
import com.infomaniak.calendar.ui.screen.eventDetail.delete.DeleteEventDialog
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.calendar.utils.serializableSaver
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.serialization.Serializable

/** Shows what the quick actions handled by [viewModel] need from the user, like the delete confirmation. */
@Composable
fun EventQuickActionsHost(viewModel: EventQuickActionViewModel) {
    viewModel.pendingDeletion?.let { pendingDeletion ->
        DeleteEventDialog(
            isRecurring = { pendingDeletion.isRecurring },
            onDelete = viewModel::deleteEvent,
            onDismissRequest = viewModel::dismissDelete,
        )
    }
}

@OptIn(SavedStateHandleSaveableApi::class)
@AssistedInject
class EventQuickActionViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    private val backgroundEventComposer: BackgroundEventComposer,
) : ViewModel() {
    /** The event whose deletion awaits the user's confirmation. */
    var pendingDeletion: PendingDeletion? by savedStateHandle.saveable(stateSaver = serializableSaver()) { mutableStateOf(null) }
        private set

    fun onEventQuickAction(event: EventSummaryUi.Normal, action: EventQuickAction) {
        when (action) {
            EventQuickAction.Duplicate -> Unit // TODO[quickActions]
            EventQuickAction.Copy -> Unit // TODO[quickActions]
            EventQuickAction.Delete -> pendingDeletion = PendingDeletion(event.occurrenceId, event.isRecurring)
        }
    }

    fun deleteEvent(scope: RecurrentEventScope) {
        pendingDeletion?.let { backgroundEventComposer.deleteEvent(it.occurrenceId, scope) }
        dismissDelete()
    }

    fun dismissDelete() {
        pendingDeletion = null
    }

    @Serializable
    data class PendingDeletion(val occurrenceId: OccurrenceId, val isRecurring: Boolean)

    @AssistedFactory
    @ViewModelAssistedFactoryKey(EventQuickActionViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): EventQuickActionViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): EventQuickActionViewModel
    }
}
