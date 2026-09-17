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
package com.infomaniak.calendar.components.eventdetail.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme
import com.infomaniak.core.common.R as RCore

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PresenceStatusButtons(
    presenceStatus: ParticipationStatus,
    onPresenceStatusChange: (ParticipationStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl, Alignment.CenterHorizontally),
        modifier = modifier.fillMaxWidth().selectableGroup(),
    ) {
        PresenceStatusButton.entries.forEach { presenceStatusButton ->
            ToggleButton(
                checked = presenceStatus == presenceStatusButton.status,
                onCheckedChange = { onPresenceStatusChange(presenceStatusButton.status) },
                colors = presenceStatusButton.colors(),
                modifier = Modifier.semantics { role = Role.RadioButton },
            ) {
                Icon(
                    painter = painterResource(presenceStatusButton.icon),
                    contentDescription = null,
                    modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(stringResource(presenceStatusButton.label))
            }
        }
    }
}

private enum class PresenceStatusButton(
    val status: ParticipationStatus,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
) {
    Yes(ParticipationStatus.Accepted, R.drawable.ic_circle_check, RCore.string.buttonYes),
    No(ParticipationStatus.Declined, R.drawable.ic_circle_cross, RCore.string.buttonNo),
    Maybe(ParticipationStatus.Tentative, R.drawable.ic_circle_question, RCore.string.buttonMaybe),
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PresenceStatusButton.colors(): ToggleButtonColors = when (this) {
    PresenceStatusButton.Yes -> ToggleButtonDefaults.colors(
        checkedContainerColor = MaterialTheme.extendedColorScheme.success,
        checkedContentColor = MaterialTheme.extendedColorScheme.onSuccess,
    )
    PresenceStatusButton.No -> ToggleButtonDefaults.colors(
        checkedContainerColor = MaterialTheme.colorScheme.error,
        checkedContentColor = MaterialTheme.colorScheme.onError,
    )
    PresenceStatusButton.Maybe -> ToggleButtonDefaults.colors(
        checkedContainerColor = MaterialTheme.extendedColorScheme.warning,
        checkedContentColor = MaterialTheme.extendedColorScheme.onWarning,
    )
}

@PreviewLightDark
@Composable
private fun PresenceStatusButtonsPreview() {
    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
                ParticipationStatus.entries.forEach { participationStatus ->
                    PresenceStatusButtons(presenceStatus = participationStatus, onPresenceStatusChange = {})
                }
            }
        }
    }
}
