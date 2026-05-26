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
package com.infomaniak.calendar.di

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlin.reflect.KClass

/**
 * Root dependency graph for the application, scoped to [AppScope].
 *
 * The application [Context] is supplied at construction time via the [Factory] and is then
 * available to any injected class that declares a [Context] dependency.
 *
 * The [viewModelProviders] multibindings map is populated by every [ViewModel] that is
 * contributed with `@ContributesIntoMap(AppScope::class) @ViewModelKey(MyViewModel::class)`.
 * It is consumed by [MetroViewModelFactory] to build ViewModels.
 */
@DependencyGraph(AppScope::class)
interface AppGraph {
    val viewModelFactory: MetroViewModelFactory
    val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides appContext: Context): AppGraph
    }
}
