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

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animate
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme
import com.infomaniak.core.common.R as RCore

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Stable
internal class PresenceStatusToolbarState(
    internal val scrollBehavior: FloatingToolbarScrollBehavior,
    internal val collapsesWhileScrolling: Boolean,
) {
    val nestedScrollConnection: NestedScrollConnection get() = scrollBehavior

    var height: Dp by mutableStateOf(0.dp)
        private set

    internal fun updateHeight(height: Dp) {
        this.height = height
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun rememberPresenceStatusToolbarState(scrollState: ScrollState): PresenceStatusToolbarState {
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
    )
    val collapsesWhileScrolling = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isScrolledToBottom = !scrollState.canScrollForward
    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            val toolbarState = scrollBehavior.state
            animate(initialValue = toolbarState.offset, targetValue = 0f) { value, _ -> toolbarState.offset = value }
        }
    }

    return remember(scrollBehavior, collapsesWhileScrolling) {
        PresenceStatusToolbarState(scrollBehavior, collapsesWhileScrolling)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PresenceStatusButtons(
    presenceStatus: ParticipationStatus,
    onPresenceStatusChange: (ParticipationStatus) -> Unit,
    toolbarState: PresenceStatusToolbarState,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
) {
    var selectedStatus by remember(presenceStatus) { mutableStateOf(presenceStatus) }
    val density = LocalDensity.current

    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = toolbarState.scrollBehavior.takeIf { toolbarState.collapsesWhileScrolling },
        shape = MaterialTheme.shapes.large,
        expandedShadowElevation = TOOLBAR_ELEVATION,
        collapsedShadowElevation = TOOLBAR_ELEVATION,
        modifier = modifier
            .onSizeChanged { toolbarState.updateHeight(with(density) { it.height.toDp() }) }
            .padding(top = EsdsTheme.spacing.xl, bottom = FloatingToolbarDefaults.ScreenOffset + bottomInset)
            .selectableGroup(),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md)) {
            PresenceStatusButton.entries.forEach { presenceStatusButton ->
                ToggleButton(
                    checked = selectedStatus == presenceStatusButton.status,
                    onCheckedChange = {
                        selectedStatus = presenceStatusButton.status
                        onPresenceStatusChange(presenceStatusButton.status)
                    },
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
}

private val TOOLBAR_ELEVATION = 3.dp

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@PreviewLightDark
@Composable
private fun PresenceStatusButtonsPreview() {
    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
                ParticipationStatus.entries.forEach { participationStatus ->
                    PresenceStatusButtons(
                        presenceStatus = participationStatus,
                        onPresenceStatusChange = {},
                        toolbarState = rememberPresenceStatusToolbarState(rememberScrollState()),
                    )
                }
            }
        }
    }
}
