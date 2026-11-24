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
package com.infomaniak.calendar.components.eventdetail.detail.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.theme.Dimens
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleCheck
import com.infomaniak.designsystem.icons.symbols.CircleCross
import com.infomaniak.designsystem.icons.symbols.CircleQuestion
import com.infomaniak.designsystem.icons.symbols.Symbol
import com.infomaniak.core.common.R as RCore

@Composable
fun ParticipationStatusButtonsToolbar(
    participationStatus: () -> ParticipationStatus,
    onParticipationStatusChange: (ParticipationStatus) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = Dimens.FloatingToolbarElevation,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
) {
    var selectedStatus by remember(participationStatus) { mutableStateOf(participationStatus()) }

    FlexibleBottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
                modifier = Modifier
                    .selectableGroup()
                    .width(IntrinsicSize.Min),
            ) {
                ParticipationStatusButton.entries.forEach { participationStatusButton ->
                    val checked = selectedStatus == participationStatusButton.status
                    ToggleButton(
                        checked = checked,
                        onCheckedChange = {
                            selectedStatus = participationStatusButton.status
                            onParticipationStatusChange(participationStatusButton.status)
                        },
                        colors = participationStatusButton.colors(),
                        border = if (checked.not()) ButtonDefaults.outlinedButtonBorder(true) else null,
                        modifier = Modifier
                            .semantics { role = Role.RadioButton }
                            .weight(1f),
                        icon = { Icon(imageVector = participationStatusButton.icon.Small, contentDescription = null) },
                    ) {
                        Text(stringResource(participationStatusButton.label))
                    }
                }
            }
        }
    }
}

private enum class ParticipationStatusButton(
    val status: ParticipationStatus,
    val icon: Symbol,
    @StringRes val label: Int,
) {
    Yes(ParticipationStatus.Accepted, EsdsIcons.CircleCheck, RCore.string.buttonYes),
    Maybe(ParticipationStatus.Tentative, EsdsIcons.CircleQuestion, RCore.string.buttonMaybe),
    No(ParticipationStatus.Declined, EsdsIcons.CircleCross, RCore.string.buttonNo),
}

@Composable
private fun ParticipationStatusButton.colors(): ToggleButtonColors {
    return ToggleButtonDefaults.colors(
        containerColor = Color.Transparent,
        checkedContainerColor = status.statusColor(),
        checkedContentColor = status.onStatusColor(),
    )
}

@Preview
@Composable
private fun ParticipationStatusButtonsToolbarPreview() {
    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl)) {
                ParticipationStatus.entries.forEach { participationStatus ->
                    ParticipationStatusButtonsToolbar(
                        participationStatus = { participationStatus },
                        onParticipationStatusChange = {},
                    )
                }
            }
        }
    }
}
