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
package com.infomaniak.calendar.ui.component.topAppBar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.BackButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.DrawerIconButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.EditButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.EventDeleteButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.InboxButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.SaveButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.SearchButton
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.basicbutton.BasicButton
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ArrowLeft
import com.infomaniak.designsystem.icons.symbols.Check
import com.infomaniak.designsystem.icons.symbols.Inbox
import com.infomaniak.designsystem.icons.symbols.List
import com.infomaniak.designsystem.icons.symbols.MagnifyingGlass
import com.infomaniak.designsystem.icons.symbols.Pen
import com.infomaniak.designsystem.icons.symbols.Symbol
import com.infomaniak.designsystem.icons.symbols.Trash
import kotlinx.coroutines.launch
import com.infomaniak.calendar.components.resources.R as RComponents
import com.infomaniak.core.common.R as RCommon


object TopAppBarButtons {
    @OptIn(ExperimentalMaterial3Api::class)
    private val TooltipIconButtonAnchorPosition = TooltipAnchorPosition.Below

    @Composable
    fun InboxButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        TooltipIconButton(
            symbol = EsdsIcons.Inbox,
            contentDescription = stringResource(R.string.contentDescriptionInbox),
            onClick = onClick,
            modifier = modifier,
        )
    }

    @Composable
    fun SearchButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        TooltipIconButton(
            symbol = EsdsIcons.MagnifyingGlass,
            contentDescription = stringResource(RComponents.string.contentDescriptionSearch),
            onClick = onClick,
            modifier = modifier,
        )
    }

    @Composable
    fun DrawerIconButton(modifier: Modifier = Modifier) {
        val scope = rememberCoroutineScope()
        val calendarDrawerState = LocalDrawerState.current

        TooltipIconButton(
            symbol = EsdsIcons.List,
            contentDescription = stringResource(R.string.contentDescriptionMenuDrawer),
            onClick = { scope.launch { calendarDrawerState?.open() } },
            modifier = modifier,
        )
    }

    @Composable
    fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
        TooltipIconButton(
            symbol = EsdsIcons.ArrowLeft,
            contentDescription = stringResource(R.string.contentDescriptionBack),
            onClick = onClick,
            modifier = modifier,
        )
    }

    @Composable
    fun EditButton(modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
        TooltipIconButton(
            symbol = EsdsIcons.Pen,
            contentDescription = stringResource(RCommon.string.edit),
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }

    @Composable
    fun EventDeleteButton(modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
        TooltipIconButton(
            symbol = EsdsIcons.Trash,
            contentDescription = stringResource(RCommon.string.deleteButton),
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SaveButton(
        modifier: Modifier = Modifier,
        enabled: () -> Boolean = { true },
        isLoading: () -> Boolean = { false },
        onClick: () -> Unit,
    ) {
        val contentDescription = stringResource(RCommon.string.buttonSave)
        val colors = IconButtonDefaults.filledIconButtonColors()

        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipIconButtonAnchorPosition),
            state = rememberTooltipState(),
            tooltip = { PlainTooltip { Text(contentDescription) } },
            modifier = modifier,
        ) {
            BasicButton(
                onClick = onClick,
                // Same footprint as a FilledIconButton: its container inside a minimum interactive size
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(IconButtonDefaults.smallContainerSize()),
                shape = IconButtonDefaults.filledShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.containerColor,
                    contentColor = colors.contentColor,
                    disabledContainerColor = colors.disabledContainerColor,
                    disabledContentColor = colors.disabledContentColor,
                ),
                elevation = null,
                enabled = enabled,
                showIndeterminateProgress = isLoading,
                contentPadding = PaddingValues(),
            ) {
                Icon(EsdsIcons.Check.Medium, contentDescription)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TooltipIconButton(
        symbol: Symbol,
        contentDescription: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipIconButtonAnchorPosition),
            state = rememberTooltipState(),
            tooltip = { PlainTooltip { Text(contentDescription) } },
            modifier = modifier,
        ) {
            IconButton(onClick, enabled = enabled) {
                Icon(symbol.Medium, contentDescription)
            }
        }
    }
}

@Preview
@Composable
private fun SearchButtonPreview() {
    CalendarThemeForPreview {
        SearchButton(onClick = {})
    }
}

@Preview
@Composable
private fun InboxButtonPreview() {
    CalendarThemeForPreview {
        InboxButton(onClick = {})
    }
}

@Preview
@Composable
private fun DrawerIconButtonPreview() {
    CalendarThemeForPreview {
        DrawerIconButton()
    }
}

@Preview
@Composable
private fun BackButtonPreview() {
    CalendarThemeForPreview {
        BackButton(onClick = {})
    }
}

@Preview
@Composable
private fun EditButtonPreview() {
    CalendarThemeForPreview {
        EditButton(onClick = {})
    }
}

@Preview
@Composable
private fun EventDeleteButtonPreview() {
    CalendarThemeForPreview {
        EventDeleteButton(onClick = {})
    }
}

@Preview
@Composable
private fun SaveButtonPreview() {
    CalendarThemeForPreview {
        Row {
            SaveButton(onClick = {})
            SaveButton(onClick = {}, enabled = { false })
            SaveButton(onClick = {}, isLoading = { true })
        }
    }
}
