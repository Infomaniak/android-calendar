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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.infomaniak.core.onboarding.OnboardingPage
import com.infomaniak.core.onboarding.components.OnboardingComponents.DefaultTitleAndDescription

internal fun onboardingPage(
    @DrawableRes illustrationRes: Int,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
): OnboardingPage = OnboardingPage(
    background = {},
    illustration = {
        Image(painter = painterResource(illustrationRes), contentDescription = null)
    },
    text = {
        DefaultTitleAndDescription(
            title = stringResource(titleRes),
            description = stringResource(descriptionRes),
            titleStyle = MaterialTheme.typography.headlineSmallEmphasized,
            descriptionStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    },
)
