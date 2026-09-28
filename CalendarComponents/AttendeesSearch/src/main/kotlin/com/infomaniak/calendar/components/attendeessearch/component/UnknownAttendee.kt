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
package com.infomaniak.calendar.components.attendeessearch.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun UnknownAttendee(email: String, isValidEmail: Boolean, onAddAttendee: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(start = EsdsTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.envelope),
            tint = MaterialTheme.colorScheme.onPrimary,
            contentDescription = null,
            modifier = Modifier
                .padding(start = EsdsTheme.spacing.lg)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                )
                .padding(EsdsTheme.spacing.sm),
        )
        ListItem(
            modifier = Modifier.weight(1f),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            supportingContent = if (isValidEmail) {
                null
            } else {
                {
                    Text(
                        text = "Complétez l'adresse mail pour l'ajouter",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            content = {
                Column {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                    )
                }
            },
        )
        if (isValidEmail) {
            IconButton(
                onClick = { onAddAttendee(email) },
                modifier = Modifier.padding(end = EsdsTheme.spacing.lg),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewUnknownAttendeeCorrectEmail() {
    MaterialTheme {
        Surface {
            UnknownAttendee("sol.rubado@gmail.com", isValidEmail = true, onAddAttendee = {})
        }
    }
}

@Preview
@Composable
private fun PreviewUnknownAttendeeEmailIncomplete() {
    MaterialTheme {
        Surface {
            UnknownAttendee("sol.rub", isValidEmail = false, onAddAttendee = {})
        }
    }

}


