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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

/**
 * A [ViewModelProvider.Factory] that builds [ViewModel]s using providers contributed to
 * Metro's [AppGraph.viewModelProviders] multibinding map.
 *
 * Wired as the activity's `defaultViewModelProviderFactory` so that the standard
 * `viewModel()` composable (and similar helpers) transparently resolves Metro-built
 * ViewModels.
 */
@Inject
@SingleIn(AppScope::class)
class MetroViewModelFactory(
    private val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val provider = viewModelProviders[modelClass.kotlin]
            ?: error("No Metro ViewModel binding contributed for ${modelClass.name}")

        @Suppress("UNCHECKED_CAST")
        return provider() as T
    }
}
