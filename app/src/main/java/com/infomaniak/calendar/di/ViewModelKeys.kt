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
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

/** A [MapKey] annotation for binding ViewModels in a multibinding map. */
@MapKey(implicitClassKey = true)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPE,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel> = Nothing::class)

/**
 * A [MapKey] annotation for binding [assisted ViewModel factories][ViewModelAssistedFactory] in a
 * multibinding map.
 */
@MapKey
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPE,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelAssistedFactoryKey(val value: KClass<out ViewModel>)

/**
 * Factory interface for creating [ViewModel] instances with assisted injection using
 * [CreationExtras].
 *
 * Implement this interface in an `@AssistedFactory`-annotated class to create ViewModels that
 * require runtime parameters. The factory receives [CreationExtras] which can be used to access
 * Android-specific ViewModel creation context (such as `SavedStateHandle`).
 *
 * Example:
 * ```kotlin
 * @AssistedInject
 * class DetailsViewModel(@Assisted val id: String) : ViewModel() {
 *   // ...
 *
 *   @AssistedFactory
 *   @ViewModelAssistedFactoryKey(DetailsViewModel::class)
 *   @ContributesIntoMap(AppScope::class)
 *   fun interface Factory : ViewModelAssistedFactory {
 *     override fun create(extras: CreationExtras): DetailsViewModel {
 *       return create(extras.get<String>(KEY_ID))
 *     }
 *
 *     fun create(@Assisted id: String): DetailsViewModel
 *   }
 * }
 * ```
 */
interface ViewModelAssistedFactory {
    fun create(extras: CreationExtras): ViewModel
}
