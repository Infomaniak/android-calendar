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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.R
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ui.compose.preview.previewparameter.dummyUserOf

@Composable
fun DrawerAccountItem(user: User, isExpanded: () -> Boolean, onAccountExpanded: () -> Unit, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded()) 180f else 0f)
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .toggleable(
                value = isExpanded(),
                onValueChange = { onAccountExpanded() },
                interactionSource = interactionSource,
                indication = null,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        Avatar(avatarType = AvatarType.fromUser(user), Modifier.size(32.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = user.email,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            modifier = Modifier
                .rotate(rotation)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(vertical = Margin.Medium, horizontal = Margin.Mini),
            painter = painterResource(R.drawable.ic_chevron_down),
            contentDescription = null,
        )
    }
    if (isExpanded()) {
        Spacer(modifier = Modifier.padding(vertical = Margin.Micro))
    }
}

@Composable
@Preview(showBackground = true)
private fun DrawerAccountItemCollapsedPreview() {
    DrawerAccountItem(user = dummyUserOf(1, "John", "Doe"), isExpanded = { false }, onAccountExpanded = {})
}
