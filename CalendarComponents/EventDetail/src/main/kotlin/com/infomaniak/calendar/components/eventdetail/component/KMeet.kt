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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.eventdetail.detail.component.CompactTextField
import com.infomaniak.calendar.components.eventdetail.form.EventFormState.Companion.MEET_MAX_CHARACTERS
import com.infomaniak.calendar.components.eventdetail.theme.CustomColors
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleCross
import com.infomaniak.designsystem.icons.symbols.Pen
import com.infomaniak.designsystem.icons.symbols.ProductKmeet
import com.infomaniak.designsystem.icons.symbols.SquaresStacked
import com.infomaniak.core.common.R as RCommon

@Composable
internal fun KMeet(
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
internal fun KMeetEditable(
    state: TextFieldState,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    var isManualEntry by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var wasFocused by remember { mutableStateOf(false) }
    val text = stringResource(R.string.kMeetMeeting)

    SegmentedListItem(
        colors = CustomColors.EventFormItemColors,
        content = {
            if (isManualEntry || state.text.isNotEmpty()) {
                KMeetTextField(
                    state = state,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (wasFocused && !focusState.isFocused) isManualEntry = false
                            wasFocused = focusState.isFocused
                        },
                )

                LaunchedEffect(isManualEntry) {
                    if (isManualEntry && state.text.isEmpty()) focusRequester.requestFocus()
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Button(onClick = { state.setTextAndPlaceCursorAtEnd("kmeet.infomaniak.com/iu3uerwai") }) {
                        Text("Add")
                    }
                }
            }
        },
        modifier = modifier,
        shapes = shapes,
        leadingContent = { KMeetIcon() },
        trailingContent = {
            if (isManualEntry || state.text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        state.clearText()
                        isManualEntry = false
                    },
                ) {
                    Icon(
                        EsdsIcons.CircleCross.Small,
                        contentDescription = stringResource(RCommon.string.contentDescriptionClear),
                    )
                }
            } else {
                TrailingButtonIcon(
                    EsdsIcons.Pen.Small,
                    contentDescription = stringResource(RCommon.string.edit),
                    onClick = { isManualEntry = true },
                )
            }
        },
    )
}

@Composable
internal fun TrailingButtonIcon(imageVector: ImageVector, contentDescription: String, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Icon(imageVector, contentDescription = contentDescription)
    }
}

@Composable
internal fun KMeetTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
) {
    val isTooLong = state.text.length > MEET_MAX_CHARACTERS
    val extraCharacters = MEET_MAX_CHARACTERS - state.text.length
    val errorMessage = stringResource(R.string.eventValidationCharacterLimitExceeded) +
            " $extraCharacters / $MEET_MAX_CHARACTERS"
    var wasFocused by remember { mutableStateOf(false) }

    CompactTextField(
        state = state,
        modifier = modifier,
        placeholder = {
            Text(text = stringResource(R.string.kMeetMeeting), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        isError = isTooLong,
        lineLimits = TextFieldLineLimits.SingleLine,
        errorText = {
            if (isTooLong) {
                Text(text = errorMessage, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
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
        TrailingButtonIcon(EsdsIcons.SquaresStacked.Small, contentDescription = contentDescription, onClick = onClick)
    }
}

@Preview
@Composable
private fun PreviewKMeet() {
    MaterialTheme {
        Surface {
            KMeet(onJoin = {}, onCopy = {}, shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1))
        }
    }
}

@Preview
@Composable
private fun PreviewKMeetTextField() {
    MaterialTheme {
        Surface {
            KMeetEditable(
                state = TextFieldState(""),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewKMeetTextFieldWithUrl() {
    MaterialTheme {
        Surface {
            KMeetEditable(
                state = TextFieldState("kmeet.infomaniak.com/uid"),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

