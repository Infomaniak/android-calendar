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
package com.infomaniak.calendar.crash

import com.infomaniak.multiplatform_calendar.core.data.BreadcrumbType
import com.infomaniak.multiplatform_calendar.core.data.CrashReport
import com.infomaniak.multiplatform_calendar.core.data.CrashReportLevel
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message

internal object AndroidCrashReport : CrashReport {

    override fun addBreadcrumb(
        message: String,
        category: String,
        level: CrashReportLevel,
        type: BreadcrumbType,
        data: Map<String, String>?,
    ) {
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                this.message = message
                this.category = category
                this.level = level.toSentryLevel()
                this.type = type.value
                data?.forEach(::setData)
            },
        )
    }

    override fun capture(message: String, exception: Throwable, data: Map<String, String>?) {
        val sentryEvent = SentryEvent(exception).apply {
            data?.forEach(::setExtra)
            this.message = Message().apply { this.message = message }
        }
        Sentry.captureEvent(sentryEvent)
    }

    override fun capture(message: String, data: Map<String, String>?, level: CrashReportLevel?) {
        Sentry.captureMessage(message, level?.toSentryLevel() ?: SentryLevel.INFO) { scope ->
            data?.forEach(scope::setExtra)
        }
    }

    private fun CrashReportLevel.toSentryLevel(): SentryLevel = when (this) {
        CrashReportLevel.Debug -> SentryLevel.DEBUG
        CrashReportLevel.Info -> SentryLevel.INFO
        CrashReportLevel.Warning -> SentryLevel.WARNING
        CrashReportLevel.Error -> SentryLevel.ERROR
        CrashReportLevel.Fatal -> SentryLevel.FATAL
    }
}
