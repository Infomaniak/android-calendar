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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.eventdetail.component.items.SegmentedListItemSwitch
import com.infomaniak.calendar.components.eventdetail.detail.component.CompactTextField
import com.infomaniak.calendar.components.eventdetail.detail.component.CompactTextFieldDefaults
import com.infomaniak.calendar.components.eventdetail.form.EventFormState.Companion.KMEET_MAX_CHARACTERS
import com.infomaniak.calendar.components.eventdetail.theme.CustomColors
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ProductKmeet
import com.infomaniak.designsystem.icons.symbols.SquaresStacked
import com.infomaniak.core.common.R as RCommon

@Composable
internal fun KMeetButton(
    onJoin: () -> Unit,
    onCopy: () -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        content = { Text(stringResource(id = R.string.participateKMeetTitle)) },
        leadingContent = { KMeetIcon() },
        shapes = shapes,
        colors = CustomColors.EventFormItemColors,
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl),
            ) {
                Button(onClick = onJoin) {
                    Text(stringResource(R.string.buttonJoin))
                }

                CopyButton(onClick = onCopy)
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun KMeetButtonSwitch(
    checked: () -> Boolean,
    onCheckedChange: (isChecked: Boolean) -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    SegmentedListItemSwitch(checked, onCheckedChange, shapes = shapes, leadingContent = { KMeetIcon() }, modifier = modifier) {
        Text(stringResource(id = R.string.kMeetMeeting))
    }
}

@Composable
internal fun KMeetEditable(
    state: TextFieldState,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val isTooLong = state.text.length > KMEET_MAX_CHARACTERS
    val extraCharacters = KMEET_MAX_CHARACTERS - state.text.length
    val errorMessage = stringResource(R.string.eventValidationCharacterLimitExceeded) +
            " $extraCharacters / $KMEET_MAX_CHARACTERS"

    SegmentedListItem(
        shapes = shapes,
        colors = CustomColors.EventFormItemColors,
        content = {
            CompactTextField(
                state = state,
                modifier = Modifier.semantics { if (isTooLong) error(errorMessage) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.kMeetMeeting),
                        color = CompactTextFieldDefaults.colors().focusedPlaceholderColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                isError = isTooLong,
                lineLimits = TextFieldLineLimits.SingleLine,
                supportingText = {
                    if (isTooLong) {
                        Text(
                            text = errorMessage,
                            color = CompactTextFieldDefaults.colors().errorSupportingTextColor,
                            style = MaterialTheme.typography.bodySmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun KMeetIcon() {
    ItemIcon(EsdsIcons.ProductKmeet)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CopyButton(onClick: () -> Unit) {
    val contentDescription = stringResource(RCommon.string.buttonCopy)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        state = rememberTooltipState(),
        tooltip = { PlainTooltip { Text(contentDescription) } },
    ) {
        IconButton(onClick = onClick) {
            Icon(EsdsIcons.SquaresStacked.Small, contentDescription = contentDescription)
        }
    }
}

@Preview
@Composable
private fun PreviewKMeetButton() {
    MaterialTheme {
        Surface {
            KMeetButton(onJoin = {}, onCopy = {}, shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1))
        }
    }
}

@Preview
@Composable
private fun PreviewKMeetButtonSwitch() {
    MaterialTheme {
        Surface {
            KMeetButtonSwitch(
                checked = { true },
                onCheckedChange = {},
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}
