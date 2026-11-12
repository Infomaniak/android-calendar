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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.designsystem.core.theme.EsdsTheme

/** The layout shared by the search screens: their search bar above their results. [contentPadding]'s bottom is left to [content]. */
@Composable
fun SearchLayout(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .padding(contentPadding.onlyHorizontal())
            .padding(top = contentPadding.calculateTopPadding() + EsdsTheme.spacing.xl)
            .padding(horizontal = EsdsTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.threeXl),
        content = content,
    )
}
