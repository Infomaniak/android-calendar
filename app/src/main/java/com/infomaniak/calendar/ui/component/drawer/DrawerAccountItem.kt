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
package com.infomaniak.calendar.ui.component.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.CustomColors
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ui.compose.preview.previewparameter.dummyUserOf
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ChevronDown

@Composable
fun DrawerAccountItem(
    user: User,
    isExpanded: () -> Boolean,
    onAccountExpanded: () -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded()) 180f else 0f)

    SegmentedListItem(
        onClick = onAccountExpanded,
        shapes = shapes,
        modifier = modifier.semantics { toggleableState = ToggleableState(isExpanded()) },
        leadingContent = { Avatar(avatarType = AvatarType.fromUser(user), Modifier.size(32.dp)) },
        trailingContent = {
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape),
            ) {
                Icon(
                    modifier = Modifier
                        .padding(vertical = Margin.Mini, horizontal = Margin.Micro)
                        .rotate(rotation),
                    imageVector = EsdsIcons.ChevronDown.Small,
                    contentDescription = null,
                )
            }
        },
        verticalAlignment = Alignment.CenterVertically,
        colors = ListItemDefaults.colors(containerColor = CustomColors.menuDrawerContainerBackground),
    ) {
        Text(
            text = user.email,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun DrawerAccountItemCollapsedPreview() {
    CalendarThemeForPreview {
        DrawerAccountItem(
            user = dummyUserOf(1, "John", "Doe"),
            isExpanded = { false },
            onAccountExpanded = {},
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        )
    }
}
