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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.eventdetail.form.EventFormState.Companion.DESCRIPTION_MAX_CHARACTERS
import com.infomaniak.calendar.components.eventdetail.state.CollapsibleTextState
import com.infomaniak.calendar.components.eventdetail.state.animateCollapse
import com.infomaniak.calendar.components.eventdetail.state.rememberCollapsibleTextState
import com.infomaniak.calendar.components.eventdetail.theme.CustomColors
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ChevronDown
import com.infomaniak.designsystem.icons.symbols.List

private const val COLLAPSED_MAX_LINES = 3

@Composable
internal fun DescriptionCollapsibleButton(
    description: String,
    shapes: ListItemShapes,
    contentPadding: PaddingValues = PaddingValues(),
) {
    var isCollapsed by rememberSaveable { mutableStateOf(true) }
    val descriptionState = rememberCollapsibleTextState(isCollapsed, COLLAPSED_MAX_LINES)

    val descriptionTitle = R.string.descriptionTitle
    val leadingIcon = EsdsIcons.List

    // Shows or hides the button to toggle the description based on if the text is actually overflowing when collapsed or not.
    if (descriptionState.isOverflowing) {
        SegmentedListItem(
            content = { Text(stringResource(descriptionTitle)) },
            supportingContent = { DescriptionContent(description, descriptionState) },
            leadingContent = { ItemIcon(leadingIcon) },
            trailingContent = { AnimatedChevron { isCollapsed } },
            onClick = { isCollapsed = !isCollapsed },
            shapes = shapes,
            colors = CustomColors.EventFormItemColors,
            contentPadding = contentPadding + ListItemDefaults.ContentPadding,
        )
    } else {
        SegmentedListItem(
            content = { Text(stringResource(descriptionTitle)) },
            supportingContent = { DescriptionContent(description, descriptionState) },
            leadingContent = { ItemIcon(leadingIcon) },
            shapes = shapes,
            colors = CustomColors.EventFormItemColors,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
private fun DescriptionContent(description: String, state: CollapsibleTextState) {
    SelectionContainer {
        Text(
            text = description,
            maxLines = state.maxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = state::onTextLayout,
            modifier = Modifier.animateCollapse(state),
        )
    }
}

@Composable
internal fun DescriptionEditable(
    state: TextFieldState,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val isTooLong = state.text.length > DESCRIPTION_MAX_CHARACTERS
    val extraCharacters = DESCRIPTION_MAX_CHARACTERS - state.text.length
    val errorMessage = stringResource(R.string.eventValidationCharacterLimitExceeded) +
            " $extraCharacters / $DESCRIPTION_MAX_CHARACTERS"

    SegmentedListItem(
        shapes = shapes,
        colors = CustomColors.EventFormItemColors,
        content = {
            CompactTextField(
                state = state,
                modifier = Modifier.semantics { if (isTooLong) error(errorMessage) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.descriptionTitle),
                        color = CompactTextFieldDefaults.colors().focusedPlaceholderColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                isError = isTooLong,
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 2), // minHeightInLines has to be 2 so the leading
                // icon doesn't move up when adding the second line
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
                trailingIcon = null,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun AnimatedChevron(isCollapsed: () -> Boolean) {
    val animatedChevronRotation = animateFloatAsState(if (isCollapsed()) 0f else -180f, label = "Chevron rotation")

    Icon(
        imageVector = EsdsIcons.ChevronDown.Small,
        contentDescription = null,
        modifier = Modifier.graphicsLayer { rotationZ = animatedChevronRotation.value },
    )
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            DescriptionCollapsibleButton(
                description = LoremIpsum(15).values.first(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewOverflow() {
    MaterialTheme {
        Surface {
            DescriptionCollapsibleButton(
                description = LoremIpsum(35).values.first(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDescriptionEditable() {
    MaterialTheme {
        Surface {
            DescriptionEditable(
                state = TextFieldState("Description"),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
                leadingIcon = { ItemIcon(EsdsIcons.List.Medium) },
            )
        }
    }
}
