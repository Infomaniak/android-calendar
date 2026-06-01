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
package com.infomaniak.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.ui.navigation.MainNavHost
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.utils.UserLoadState
import com.infomaniak.calendar.utils.rememberUserLoadState

class MainActivity : ComponentActivity() {

    private val mainApplication by lazy { application as MainApplication }
    private val appGraph by lazy { mainApplication.appGraph }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            when (val userLoadState = appGraph.accountUtils.rememberUserLoadState().value) {
                UserLoadState.Loading -> {
                    CalendarTheme {
                        Surface { /* Blank surface while waiting for first result */ }
                    }
                }
                is UserLoadState.Loaded -> {
                    // TODO: Detect empty user list and go back to onboarding
                    val startDestination = if (userLoadState.user == null) NavDestination.Onboarding() else NavDestination.Home

                    CompositionLocalProvider(LocalUser provides userLoadState.user) {
                        CalendarTheme {
                            Surface {
                                MainNavHost(startDestination = startDestination)
                            }
                        }
                    }
                }
            }
        }
    }
}
