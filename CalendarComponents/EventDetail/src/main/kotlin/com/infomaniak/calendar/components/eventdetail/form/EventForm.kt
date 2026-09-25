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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.TitleEditable
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.ProvideEventSharedTransition
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedElement
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.core.ui.compose.basics.onlyHorizontal

/**
 * Reusable component for both the creation and the edition of an event.
 *
 * [sharedTransitionScope] and [animatedVisibilityScope] animate associated components between detail and editing.
 */
@Composable
fun EventForm(
    state: EventFormState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) = ProvideEventSharedTransition(sharedTransitionScope, animatedVisibilityScope) {
    val horizontalContentPadding = contentPadding.onlyHorizontal()

    Column(
        modifier.padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()),
    ) {
        TitleEditable(
            dotColor = state.colorState.value ?: Color.Transparent, // Temporarily hide the dot until we get the actual color
            textFieldState = state.titleTextState,
            modifier = Modifier
                .padding(horizontalContentPadding)
                .eventSharedElement(EventSharedElement.Title),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            EventForm(
                state = rememberSaveableEventFormState(
                    calendars = listOf(previewEventDetailCalendar),
                    initialCalendar = previewEventDetailCalendar,
                    initialText = "Event title",
                ),
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}
