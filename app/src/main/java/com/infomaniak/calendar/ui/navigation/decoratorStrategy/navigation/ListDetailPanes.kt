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
package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable

/** Whether this content is drawn in the detail pane while the list pane is shown beside it. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun isDetailBesideList(): Boolean {
    val transitionScope = LocalListDetailSceneScope.current?.scaffoldTransitionScope
    val isInDetailPane = (transitionScope as? ThreePaneScaffoldPaneScope)?.paneRole == ListDetailPaneScaffoldRole.Detail
    val scaffoldValue = transitionScope?.scaffoldStateTransition?.targetState
    return isInDetailPane && scaffoldValue?.get(ListDetailPaneScaffoldRole.List) == PaneAdaptedValue.Expanded
}
