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
package com.infomaniak.calendar.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.BuildConfig
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

/**
 * Cross-app login ViewModel used by the onboarding screen to detect Infomaniak accounts that are
 * already present in other Infomaniak apps on the device.
 */
@Inject
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>()) // TODO: Do we need this binding?
@ViewModelKey(CrossAppLoginViewModel::class)
class CrossAppLoginViewModel : BaseCrossAppLoginViewModel(
    applicationId = "com.infomaniak.sync",// TODO[login]: ConfigUtils.safePackage,
    clientId = BuildConfig.CLIENT_ID,
)
