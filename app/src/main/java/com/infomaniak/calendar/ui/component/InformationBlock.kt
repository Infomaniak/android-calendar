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
package com.infomaniak.calendar.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleInfo
import com.infomaniak.designsystem.icons.symbols.Symbol

@Composable
fun InformationBlock(
    text: String,
    modifier: Modifier = Modifier,
    symbol: Symbol = InformationBlockDefaults.symbol,
    colors: InformationBlockColors = InformationBlockDefaults.colors(),
) {
    ListItem(
        leadingContent = { Icon(symbol.Small, null) },
        content = { Text(text = text, color = colors.contentColor) },
        modifier = modifier,
        shapes = ListItemDefaults.shapes(shape = MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(
            containerColor = colors.containerColors,
            headlineColor = colors.contentColor,
            leadingIconColor = colors.contentColor,
        ),
    )
}

data class InformationBlockColors(
    val containerColors: Color,
    val contentColor: Color,
)

object InformationBlockDefaults {
    val symbol: Symbol get() = EsdsIcons.CircleInfo

    @Composable
    fun colors(
        containerColors: Color = MaterialTheme.extendedColorScheme.informationContainer,
        contentColor: Color = MaterialTheme.extendedColorScheme.onInformationContainer,
    ) = InformationBlockColors(
        containerColors = containerColors,
        contentColor = contentColor,
    )
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                InformationBlock("Hello this field is not limited in its number of lines")
            }
        }
    }
}
