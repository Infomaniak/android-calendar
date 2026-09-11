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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.resources.R

private const val COLLAPSED_MAX_LINES = 3

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DescriptionCollapsableButton(description: String, contentPadding: PaddingValues) {
    var isDescriptionOverflowing by rememberSaveable { mutableStateOf(false) }
    var isCollapsed by rememberSaveable { mutableStateOf(true) }
    val maxLines = if (isCollapsed) COLLAPSED_MAX_LINES else Int.MAX_VALUE

    val descriptionTitle = R.string.descriptionTitle
    val leadingIconRes = R.drawable.ic_list_left
    val updateOverflow: (TextLayoutResult) -> Unit = {
        val isOverflowingDetectedWhenCollapsed = it.hasVisualOverflow
        val isOverflowingDetectedWhenExpanded = it.lineCount > COLLAPSED_MAX_LINES
        isDescriptionOverflowing = isOverflowingDetectedWhenCollapsed || isOverflowingDetectedWhenExpanded
    }

    // Shows or hides the button to toggle the description based on if the text is actually overflowing when collapsed or not.
    if (isDescriptionOverflowing) {
        ListItem(
            modifier = Modifier.animateContentSize(),
            content = { Text(stringResource(descriptionTitle)) },
            supportingContent = {
                Text(text = description, maxLines = maxLines, overflow = TextOverflow.Ellipsis, onTextLayout = updateOverflow)
            },
            leadingContent = { Icon(painterResource(leadingIconRes), contentDescription = null) },
            trailingContent = { AnimatedChevron({ isCollapsed }) },
            onClick = { isCollapsed = !isCollapsed },
            contentPadding = contentPadding + PaddingValues(LIST_ITEM_HORIZONTAL_PADDING),
        )
    } else {
        ListItem(
            headlineContent = { Text(stringResource(descriptionTitle)) },
            supportingContent = {
                Text(description, maxLines = maxLines, overflow = TextOverflow.Ellipsis, onTextLayout = updateOverflow)
            },
            leadingContent = { Icon(painterResource(leadingIconRes), contentDescription = null) },
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
private fun AnimatedChevron(isCollapsed: () -> Boolean) {
    val animatedChevronRotation = animateFloatAsState(if (isCollapsed()) 0f else -180f, label = "Chevron rotation")

    Icon(
        painterResource(R.drawable.ic_chevron_down),
        contentDescription = null,
        modifier = Modifier.rotate(animatedChevronRotation.value),
    )
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            DescriptionCollapsableButton(
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
