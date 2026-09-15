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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun KMeetButton(onClick: () -> Unit, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues()) {
    ClickableItem(
        text = stringResource(id = R.string.participateKMeetTitle),
        leadingIconRes = R.drawable.ic_product_kmeet,
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
                Text(stringResource(R.string.buttonJoin), style = MaterialTheme.typography.labelLarge)
                Icon(painterResource(R.drawable.ic_squares_stacked), contentDescription = null, modifier = Modifier.size(20.dp))
            }
        },
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Preview
@Composable
private fun PreviewKMeetButton() {
    MaterialTheme {
        Surface {
            KMeetButton(onClick = {})
        }
    }
}
