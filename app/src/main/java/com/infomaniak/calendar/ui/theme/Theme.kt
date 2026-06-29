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
package com.infomaniak.calendar.ui.theme

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.infomaniak.calendar.components.foundation.preview.EventColorsUiFactory
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.calendar.ui.screen.planning.toEventColorsUi
import com.infomaniak.core.avatar.AvatarColors
import com.infomaniak.core.avatar.LocalAvatarColors
import com.infomaniak.core.avatar.theme.DarkAvatarColorsScheme
import com.infomaniak.core.avatar.theme.LightAvatarColorsScheme
import com.infomaniak.core.ui.compose.theme.LocalIsThemeDarkMode
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors

@Composable
fun CalendarTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && SDK_INT >= 31 -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val avatarColors = if (isDarkTheme) DarkAvatarColorsScheme else LightAvatarColorsScheme
    val customColors = if (isDarkTheme) CustomDarkColorScheme else CustomLightColorScheme

    CompositionLocalProvider(
        LocalEventColorsUiFactory provides EventColorsUiFactory { EventColors.from(it).toEventColorsUi() },
        LocalIsThemeDarkMode provides isDarkTheme,
        LocalAvatarColors provides AvatarColors(avatarColors.colorList, customColors.avatarInitialsColor),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

@Composable
fun CalendarThemeForPreview(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    CalendarTheme {
        Surface(content = content, modifier = modifier)
    }
}
