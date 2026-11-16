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
package com.infomaniak.calendar.components.foundation.component

import android.icu.text.MessageFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Symbol
import com.infomaniak.designsystem.icons.symbols.UsersStacked
import com.infomaniak.designsystem.icons.symbols.ZigzagArrowUpRight
import java.util.Locale

@Composable
fun RoomDetails(seats: Int?, floor: Int?, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        seats?.let { RoomDetail(EsdsIcons.UsersStacked, pluralStringResource(R.plurals.roomSeatsLabel, it, it)) }
        floor?.let { RoomDetail(EsdsIcons.ZigzagArrowUpRight, floorLabel(it)) }
    }
}

@Composable
private fun RoomDetail(symbol: Symbol, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(symbol.Small, contentDescription = null)
        Text(text)
    }
}

@Composable
private fun floorLabel(floor: Int): String {
    return stringResource(R.string.roomFloorLabel, floor.formatToOrdinal(LocalLocale.current.platformLocale))
}

/** Formats this number as a localized ordinal, e.g. "2nd" in English, "2e" in French or "2." in German. */
private fun Int.formatToOrdinal(locale: Locale): String = MessageFormat("{0, ordinal}", locale).format(arrayOf(this))

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            RoomDetails(seats = 100, floor = 3)
        }
    }
}
