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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    val rotation by animateFloatAsState(targetValue = if (isExpanded()) 90f else 0f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = isExpanded(), onValueChange = { onAccountExpanded() })
            .padding(all = Margin.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        Avatar(avatarType = AvatarType.fromUser(user), Modifier.size(32.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = user.displayName.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.email,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            modifier = Modifier.rotate(rotation),
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun DrawerAccountItemCollapsedPreview() {
    DrawerAccountItem(user = dummyUserOf(1, "John", "Doe"), isExpanded = { false }, onAccountExpanded = {})
}
