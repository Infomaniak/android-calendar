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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.TitleEditable
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.ProvideEventSharedTransition
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedElement
import com.infomaniak.core.ui.compose.basics.onlyHorizontal

/**
 * Reusable component for both the creation and the edition of an event.
 *
 * [sharedTransitionScope] and [animatedVisibilityScope] are used to animate associated components between detail and creation.
 */
@Composable
fun EventForm(
    eventColor: Color, // TODO: Adapt this when structuring edit/creation and its states correctly
    title: String, // TODO: Adapt this when structuring edit/creation and its states correctly
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
            color = eventColor,
            title = title,
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
                eventColor = MaterialTheme.colorScheme.primary,
                title = "Event title",
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}
