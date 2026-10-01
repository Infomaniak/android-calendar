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
package com.infomaniak.calendar.components.foundation.models

import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.avatar.computeInitials
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class AttendeeUi(
    val key: String,
    val email: String,
    val displayName: String? = null,
    val status: ParticipationStatus,
    val isOrganizer: Boolean = false,
) : Parcelable {
    fun initials(): String = (displayName ?: email).computeInitials()
}

enum class ParticipationStatus(
    @PluralsRes val countPluralRes: Int,
    @StringRes val labelRes: Int,
    val statusColor: @Composable () -> Color,
    val onStatusColor: @Composable () -> Color,
) {
    Accepted(
        R.plurals.attendeesAcceptedCount,
        R.string.statusAcceptedLabel,
        { MaterialTheme.extendedColorScheme.success },
        { MaterialTheme.extendedColorScheme.onSuccess },
    ),
    Tentative(
        R.plurals.attendeesTentativeCount,
        R.string.statusTentativeLabel,
        { MaterialTheme.colorScheme.onSurfaceVariant },
        { MaterialTheme.colorScheme.surfaceVariant },
    ),
    Declined(
        R.plurals.attendeesDeclinedCount,
        R.string.statusDeclinedLabel,
        { MaterialTheme.colorScheme.error },
        { MaterialTheme.colorScheme.onError },
    ),
    NeedsAction(
        R.plurals.attendeesPendingCount,
        R.string.statusNeedsActionLabel,
        { MaterialTheme.extendedColorScheme.warning },
        { MaterialTheme.extendedColorScheme.onWarning },
    ),
}
