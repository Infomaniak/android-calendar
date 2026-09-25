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
package com.infomaniak.calendar.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.infomaniak.calendar.data.CalendarDataValues
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.UpcomingAlarm
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import androidx.core.net.toUri

@SingleIn(AppScope::class)
class AlarmScheduler @Inject constructor(
    private val appContext: Context,
    private val calendarManager: CalendarManager,
    private val calendarDataValues: CalendarDataValues,
) {
    private val alarmManager by lazy { appContext.getSystemService<AlarmManager>() }
    private val refreshTrigger = MutableSharedFlow<Instant>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun refreshUpcomingAlarms(from: Instant = Clock.System.now()) {
        refreshTrigger.tryEmit(from)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startObserving(scope: CoroutineScope) {
        refreshUpcomingAlarms()

        scope.launch {
            refreshTrigger
                .flatMapLatest { from ->
                    calendarManager.observeUpcomingAlarms(
                        limit = MAX_SCHEDULED_ALARMS,
                        horizon = ALARM_HORIZON,
                        from = from,
                    )
                }
                .collectLatest { upcomingAlarms ->
                    syncAlarms(upcomingAlarms)
                }
        }
    }

    suspend fun syncAlarms(upcomingAlarms: List<UpcomingAlarm>) {
        val nowMs = System.currentTimeMillis()
        val previouslyScheduledIds = calendarDataValues.scheduledAlarmIds.flow.first()
        val plan = computeAlarmSyncPlan(upcomingAlarms, previouslyScheduledIds, nowMs)

        for (alarmId in plan.toCancel) {
            cancelAlarm(alarmId)
        }

        for (alarm in plan.toSchedule) {
            scheduleAlarm(alarm)
        }

        calendarDataValues.scheduledAlarmIds.setValue(plan.newScheduledIds)
    }

    internal data class AlarmSyncPlan(
        val toCancel: Set<String>,
        val toSchedule: List<UpcomingAlarm>,
        val newScheduledIds: Set<String>,
    )

    private fun scheduleAlarm(alarm: UpcomingAlarm) {
        val alarmManager = alarmManager ?: return
        val triggerAtMs = alarm.firesAt.toEpochMilliseconds()
        val intent = createAlarmIntent(alarm)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            alarm.id.value.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
        }.onFailure { exception ->
            Log.w(TAG, "Failed to schedule alarm for ${alarm.id.value}", exception)
        }
    }

    private fun cancelAlarm(alarmId: String) {
        val alarmManager = alarmManager ?: return
        val intent = createCancelIntent(alarmId)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun createAlarmIntent(alarm: UpcomingAlarm): Intent {
        val timeZone = TimeZone.currentSystemDefault()
        val occurrenceIdJson = Json.encodeToString(OccurrenceId.serializer(), alarm.event.occurrenceId)
        return Intent(appContext, AlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            data = "calendar://alarm/${alarm.id.value}".toUri()
            putExtra(EXTRA_ALARM_ID, alarm.id.value)
            putExtra(EXTRA_OCCURRENCE_ID_JSON, occurrenceIdJson)
            putExtra(EXTRA_EVENT_TITLE, alarm.event.title)
            putExtra(EXTRA_EVENT_LOCATION, alarm.event.location)
            putExtra(EXTRA_EVENT_START_MS, alarm.event.timing.startInstant(timeZone).toEpochMilliseconds())
            putExtra(EXTRA_EVENT_END_MS, alarm.event.timing.endInstant(timeZone).toEpochMilliseconds())
            putExtra(EXTRA_IS_ALL_DAY, alarm.event.timing.isAllDay)
        }
    }

    private fun createCancelIntent(alarmId: String): Intent {
        return Intent(appContext, AlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            data = "calendar://alarm/$alarmId".toUri()
        }
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        const val MAX_SCHEDULED_ALARMS = 400
        private val ALARM_HORIZON = 30.days

        const val ACTION_EVENT_REMINDER = "com.infomaniak.calendar.ACTION_EVENT_REMINDER"
        const val EXTRA_ALARM_ID = "com.infomaniak.calendar.EXTRA_ALARM_ID"
        const val EXTRA_OCCURRENCE_ID_JSON = "com.infomaniak.calendar.EXTRA_OCCURRENCE_ID_JSON"
        const val EXTRA_EVENT_TITLE = "com.infomaniak.calendar.EXTRA_EVENT_TITLE"
        const val EXTRA_EVENT_LOCATION = "com.infomaniak.calendar.EXTRA_EVENT_LOCATION"
        const val EXTRA_EVENT_START_MS = "com.infomaniak.calendar.EXTRA_EVENT_START_MS"
        const val EXTRA_EVENT_END_MS = "com.infomaniak.calendar.EXTRA_EVENT_END_MS"
        const val EXTRA_IS_ALL_DAY = "com.infomaniak.calendar.EXTRA_IS_ALL_DAY"

        internal fun computeAlarmSyncPlan(
            upcomingAlarms: List<UpcomingAlarm>,
            previouslyScheduledIds: Set<String>,
            nowMs: Long,
            limit: Int = MAX_SCHEDULED_ALARMS,
        ): AlarmSyncPlan {
            val validAlarms = upcomingAlarms
                .filter { it.firesAt.toEpochMilliseconds() > nowMs }
                .take(limit)

            val newAlarmIds = validAlarms.map { it.id.value }.toSet()
            val toCancel = previouslyScheduledIds - newAlarmIds
            return AlarmSyncPlan(
                toCancel = toCancel,
                toSchedule = validAlarms,
                newScheduledIds = newAlarmIds,
            )
        }
    }
}
