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
package com.infomaniak.calendar.components.event

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.SquaresStacked
import com.infomaniak.designsystem.icons.symbols.SquaresStackedPlus
import com.infomaniak.designsystem.icons.symbols.Symbol
import com.infomaniak.designsystem.icons.symbols.Trash

enum class EventQuickAction(
    @StringRes val titleRes: Int,
    val symbol: Symbol,
    val contentColor: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
) {
    Duplicate(R.string.buttonDuplicate, EsdsIcons.SquaresStackedPlus),
    Copy(R.string.buttonCopy, EsdsIcons.SquaresStacked),
    Delete(R.string.buttonDelete, EsdsIcons.Trash, { MaterialTheme.colorScheme.error }),
}
