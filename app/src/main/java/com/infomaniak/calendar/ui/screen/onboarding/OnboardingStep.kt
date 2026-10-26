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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.infomaniak.calendar.R
import com.infomaniak.core.onboarding.OnboardingPage
import com.infomaniak.core.onboarding.components.OnboardingComponents.DefaultTitleAndDescription

internal enum class OnboardingStep(
    @DrawableRes val illustrationRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val permission: String? = null,
) {
    Login(
        illustrationRes = R.drawable.illu_onboarding_start,
        titleRes = R.string.onboardingOneTitle,
        descriptionRes = R.string.onboardingOneSubtitle,
    ),
    Notifications(
        illustrationRes = R.drawable.illu_onboarding_notifications,
        titleRes = R.string.onboardingTwoTitle,
        descriptionRes = R.string.onboardingTwoSubtitle,
        permission = if (SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null,
    ),
    Contacts(
        illustrationRes = R.drawable.illu_onboarding_contact,
        titleRes = R.string.onboardingThreeTitle,
        descriptionRes = R.string.onboardingThreeSubtitle,
        permission = Manifest.permission.READ_CONTACTS,
    ),
    EverythingReady(
        illustrationRes = R.drawable.illu_onboarding_end,
        titleRes = R.string.onboardingFourTitle,
        descriptionRes = R.string.onboardingFourSubtitle,
    );

    /** The next step to show: a permission step only while its permission is missing, the last one only after one of them. */
    fun next(context: Context): OnboardingStep? = entries.drop(ordinal + 1).firstOrNull { step ->
        when (step) {
            Login -> false
            Notifications, Contacts -> {
                step.permission != null && context.checkSelfPermission(step.permission) != PERMISSION_GRANTED
            }
            EverythingReady -> this != Login
        }
    }

    fun toOnboardingPage(): OnboardingPage = OnboardingPage(
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            )
        },
        illustration = {
            Image(painter = painterResource(illustrationRes), contentDescription = null)
        },
        text = {
            DefaultTitleAndDescription(
                title = stringResource(titleRes),
                description = stringResource(descriptionRes),
                titleStyle = MaterialTheme.typography.headlineMedium,
                descriptionStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        },
    )
}
