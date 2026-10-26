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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.basics.ButtonType
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.common.R as RCore
import com.infomaniak.core.crossapplogin.front.R as RCrossAppLogin

@Composable
internal fun NextStepButton(
    step: OnboardingStep,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The height of CrossLoginBottomContent, so the page stays in place after the login step.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = Margin.Medium),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onClick,
            shape = ButtonType.Mail.shape,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .height(ButtonType.Mail.height),
        ) {
            val textRes = when (step) {
                OnboardingStep.EverythingReady -> RCrossAppLogin.string.buttonStart
                else -> RCore.string.buttonNext
            }
            Text(stringResource(textRes), style = Typography.bodyMedium)
        }
    }
}

@Preview
@Composable
private fun NextStepButtonPreview() {
    CalendarThemeForPreview {
        NextStepButton(step = OnboardingStep.Notifications, onClick = {})
    }
}
