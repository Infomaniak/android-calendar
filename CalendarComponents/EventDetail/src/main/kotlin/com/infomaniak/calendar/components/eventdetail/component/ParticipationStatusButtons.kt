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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.theme.Dimens
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme
import com.infomaniak.core.common.R as RCore

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ParticipationStatusButtonsToolbar(
    participationStatus: () -> ParticipationStatus,
    onParticipationStatusChange: (ParticipationStatus) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = Dimens.floatingToolbarElevation,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
) {
    var selectedStatus by remember(participationStatus) { mutableStateOf(participationStatus()) }

    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = scrollBehavior,
        shape = MaterialTheme.shapes.large,
        expandedShadowElevation = elevation,
        collapsedShadowElevation = elevation,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .wrapContentWidth()
            .selectableGroup(),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md)) {
            ParticipationStatusButton.entries.forEach { participationStatusButton ->
                ToggleButton(
                    checked = selectedStatus == participationStatusButton.status,
                    onCheckedChange = {
                        selectedStatus = participationStatusButton.status
                        onParticipationStatusChange(participationStatusButton.status)
                    },
                    colors = participationStatusButton.colors(),
                    modifier = Modifier.semantics { role = Role.RadioButton },
                ) {
                    Icon(
                        painter = painterResource(participationStatusButton.icon),
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(stringResource(participationStatusButton.label))
                }
            }
        }
    }
}

private enum class ParticipationStatusButton(
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
private fun ParticipationStatusButton.colors(): ToggleButtonColors = when (this) {
    ParticipationStatusButton.Yes -> ToggleButtonDefaults.colors(
        checkedContainerColor = MaterialTheme.extendedColorScheme.success,
        checkedContentColor = MaterialTheme.extendedColorScheme.onSuccess,
    )
    ParticipationStatusButton.No -> ToggleButtonDefaults.colors(
        checkedContainerColor = MaterialTheme.colorScheme.error,
        checkedContentColor = MaterialTheme.colorScheme.onError,
    )
    ParticipationStatusButton.Maybe -> ToggleButtonDefaults.colors(
        checkedContainerColor = MaterialTheme.extendedColorScheme.warning,
        checkedContentColor = MaterialTheme.extendedColorScheme.onWarning,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun ParticipationStatusButtonsToolbarPreview() {
    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
                ParticipationStatus.entries.forEach { participationStatus ->
                    ParticipationStatusButtonsToolbar(participationStatus = { participationStatus }, onParticipationStatusChange = {})
                }
            }
        }
    }
}
