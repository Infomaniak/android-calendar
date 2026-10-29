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
package com.infomaniak.calendar.ui.screen.onboarding.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.screen.onboarding.onboardingPage
import com.infomaniak.core.onboarding.OnboardingPage
import com.infomaniak.core.common.R as RCore
import com.infomaniak.core.crossapplogin.front.R as RCrossAppLogin

internal enum class PermissionsOnboardingPage(
    @DrawableRes val illustrationRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val buttonRes: Int,
    val permission: String? = null,
) {
    Notifications(
        illustrationRes = R.drawable.illu_onboarding_notifications,
        titleRes = R.string.onboardingTwoTitle,
        descriptionRes = R.string.onboardingTwoSubtitle,
        buttonRes = RCore.string.buttonNext,
        permission = if (SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null,
    ),
    Contacts(
        illustrationRes = R.drawable.illu_onboarding_contact,
        titleRes = R.string.onboardingThreeTitle,
        descriptionRes = R.string.onboardingThreeSubtitle,
        buttonRes = RCore.string.buttonNext,
        permission = Manifest.permission.READ_CONTACTS,
    ),
    EverythingReady(
        illustrationRes = R.drawable.illu_onboarding_end,
        titleRes = R.string.onboardingFourTitle,
        descriptionRes = R.string.onboardingFourSubtitle,
        buttonRes = RCrossAppLogin.string.buttonStart,
    );

    fun toOnboardingPage(): OnboardingPage = onboardingPage(illustrationRes, titleRes, descriptionRes)

    private fun isPermissionMissing(context: Context): Boolean {
        return permission != null && context.checkSelfPermission(permission) != PERMISSION_GRANTED
    }

    companion object {
        fun isAnyPermissionMissing(context: Context): Boolean = entries.any { it.isPermissionMissing(context) }

        /** One page per missing permission, then the last one. */
        fun pagesToShow(context: Context): List<PermissionsOnboardingPage> {
            return entries.filter { it.isPermissionMissing(context) } + EverythingReady
        }
    }
}
