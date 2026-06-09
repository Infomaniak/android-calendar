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
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.BuildConfig
import com.infomaniak.calendar.MainApplication
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.core.network.LOGIN_ENDPOINT_URL
import com.infomaniak.lib.login.InfomaniakLogin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

val ComposeAppGraph: AppGraph
    @Composable get() {
        return (LocalContext.current.applicationContext as MainApplication).appGraph
    }

/**
 * Root dependency graph for the Android application, scoped to [AppScope].
 *
 * The application [Context] is supplied at construction time via the [Factory].
 *
 * `CalendarCoreGraph` (from multiplatform-calendar) is merged automatically via
 * `@ContributesTo(AppScope)` and exposes `accountManager` / `calendarManager`.
 *
 * ViewModels are auto-discovered via multibinding: any class annotated with
 * `@Inject @ContributesIntoMap(AppScope::class) @ViewModelKey(…)` is automatically
 * registered in [viewModelProviders] and resolved by [MetroViewModelFactory].
 */
@DependencyGraph(AppScope::class)
interface AppGraph {
    val viewModelFactory: MetroViewModelFactory
    val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>


    val infomaniakLogin: InfomaniakLogin

    val accountUtils: AccountUtils

    @Provides
    @SingleIn(AppScope::class)
    fun providesInfomaniakLogin(applicationContext: Context): InfomaniakLogin = InfomaniakLogin(
        context = applicationContext,
        loginUrl = "$LOGIN_ENDPOINT_URL/",
        appUID = "com.infomaniak.sync",// TODO[login]: ConfigUtils.safePackage,
        clientID = BuildConfig.CLIENT_ID,
        accessType = null,
        sentryCallback = null,
    )

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides appContext: Context): AppGraph
    }
}
