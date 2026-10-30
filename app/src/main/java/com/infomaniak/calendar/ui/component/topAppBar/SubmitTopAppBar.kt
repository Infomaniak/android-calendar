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
package com.infomaniak.calendar.ui.component.topAppBar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun SubmitTopAppBar(
    goBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitEnabled: () -> Boolean = { true },
    title: @Composable () -> Unit = {},
) {
    TopAppBar(
        navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
        actions = {
            Row(Modifier.padding(end = EsdsTheme.spacing.lg)) {
                TopAppBarButtons.SaveButton(enabled = isSubmitEnabled, onClick = onSubmit)
            }
        },
        title = title,
        modifier = modifier,
    )
}
