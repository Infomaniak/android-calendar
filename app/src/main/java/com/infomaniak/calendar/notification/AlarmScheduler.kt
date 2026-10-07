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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    /**
     * Alarms this process registered, with what they carry. It starts empty so that each process registers every alarm
     * once: the AlarmManager may have dropped them while the app wasn't running (force stop, revoked exact alarm access…).
     */
    private var registeredAlarms = emptyMap<String, AlarmRegistration>()

    fun refreshUpcomingAlarms(from: Instant = Clock.System.now()) {
        refreshTrigger.tryEmit(from)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startObserving(scope: CoroutineScope) {
        refreshUpcomingAlarms()

        scope.launch {
            refreshTrigger
                .onEach { from -> scheduleWindowRefresh(at = from + WINDOW_REFRESH_INTERVAL) }
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

    private suspend fun syncAlarms(upcomingAlarms: List<UpcomingAlarm>) {
        val previouslyScheduledIds = calendarDataValues.scheduledAlarmIds.flow.first()
        val plan = computeAlarmSyncPlan(
            upcomingAlarms = upcomingAlarms,
            previouslyScheduledIds = previouslyScheduledIds,
            registeredAlarms = registeredAlarms,
            nowMs = System.currentTimeMillis(),
        )

        // Not cancellable, so that the AlarmManager, registeredAlarms and the persisted IDs can't drift apart
        withContext(NonCancellable) {
            plan.toCancel.forEach(::cancelAlarm)
            plan.toSchedule.forEach(::scheduleAlarm)
            registeredAlarms = (plan.unchanged + plan.toSchedule).associateBy(AlarmRegistration::alarmId)
            calendarDataValues.scheduledAlarmIds.setValue(registeredAlarms.keys)
        }
    }

    internal data class AlarmSyncPlan(
        val toCancel: Set<String>,
        val toSchedule: List<AlarmRegistration>,
        val unchanged: List<AlarmRegistration>,
    )

    /** Everything an alarm is registered with: it only needs to be registered again when one of these changes. */
    internal data class AlarmRegistration(
        val alarmId: String,
        val triggerAtMs: Long,
        val occurrenceIdJson: String,
        val title: String,
        val location: String?,
        val startMs: Long,
        val endMs: Long,
        val isAllDay: Boolean,
    )

    private fun scheduleAlarm(registration: AlarmRegistration) {
        val alarmManager = alarmManager ?: return
        val triggerAtMs = registration.triggerAtMs
        val intent = createAlarmIntent(registration)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            registration.alarmId.hashCode(),
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
            Log.w(TAG, "Failed to schedule alarm for ${registration.alarmId}", exception)
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

    /**
     * [CalendarManager.observeUpcomingAlarms] doesn't follow the clock: if no alarm fires within its window, nothing else
     * would move it forward. This inexact, non-wakeup alarm refreshes it whenever the device is next awake after [at].
     */
    private fun scheduleWindowRefresh(at: Instant) {
        val alarmManager = alarmManager ?: return
        val intent = Intent(appContext, AlarmReceiver::class.java).setAction(ACTION_REFRESH_ALARMS)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        runCatching {
            alarmManager.set(AlarmManager.RTC, at.toEpochMilliseconds(), pendingIntent)
        }.onFailure { exception ->
            Log.w(TAG, "Failed to schedule the alarm window refresh", exception)
        }
    }

    private fun createAlarmIntent(registration: AlarmRegistration): Intent {
        return Intent(appContext, AlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            data = "calendar://alarm/${registration.alarmId}".toUri()
            putExtra(EXTRA_ALARM_ID, registration.alarmId)
            putExtra(EXTRA_OCCURRENCE_ID_JSON, registration.occurrenceIdJson)
            putExtra(EXTRA_EVENT_TITLE, registration.title)
            putExtra(EXTRA_EVENT_LOCATION, registration.location)
            putExtra(EXTRA_EVENT_START_MS, registration.startMs)
            putExtra(EXTRA_EVENT_END_MS, registration.endMs)
            putExtra(EXTRA_IS_ALL_DAY, registration.isAllDay)
        }
    }

    private fun createCancelIntent(alarmId: String): Intent {
        return Intent(appContext, AlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            data = "calendar://alarm/$alarmId".toUri()
        }
    }

    companion object {
        const val MAX_SCHEDULED_ALARMS = 400

        const val ACTION_EVENT_REMINDER = "com.infomaniak.calendar.ACTION_EVENT_REMINDER"
        const val ACTION_REFRESH_ALARMS = "com.infomaniak.calendar.ACTION_REFRESH_ALARMS"
        const val EXTRA_ALARM_ID = "com.infomaniak.calendar.EXTRA_ALARM_ID"
        const val EXTRA_OCCURRENCE_ID_JSON = "com.infomaniak.calendar.EXTRA_OCCURRENCE_ID_JSON"
        const val EXTRA_EVENT_TITLE = "com.infomaniak.calendar.EXTRA_EVENT_TITLE"
        const val EXTRA_EVENT_LOCATION = "com.infomaniak.calendar.EXTRA_EVENT_LOCATION"
        const val EXTRA_EVENT_START_MS = "com.infomaniak.calendar.EXTRA_EVENT_START_MS"
        const val EXTRA_EVENT_END_MS = "com.infomaniak.calendar.EXTRA_EVENT_END_MS"
        const val EXTRA_IS_ALL_DAY = "com.infomaniak.calendar.EXTRA_IS_ALL_DAY"

        private const val TAG = "AlarmScheduler"
        private val ALARM_HORIZON = 30.days
        private val WINDOW_REFRESH_INTERVAL = 1.days

        internal fun computeAlarmSyncPlan(
            upcomingAlarms: List<UpcomingAlarm>,
            previouslyScheduledIds: Set<String>,
            registeredAlarms: Map<String, AlarmRegistration>,
            nowMs: Long,
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
            limit: Int = MAX_SCHEDULED_ALARMS,
        ): AlarmSyncPlan {
            val wantedAlarms = upcomingAlarms
                .filter { it.firesAt.toEpochMilliseconds() > nowMs }
                .take(limit)
                .map { it.toRegistration(timeZone) }

            val wantedIds = wantedAlarms.mapTo(mutableSetOf(), AlarmRegistration::alarmId)
            val (unchanged, toSchedule) = wantedAlarms.partition { registeredAlarms[it.alarmId] == it }
            return AlarmSyncPlan(
                toCancel = previouslyScheduledIds + registeredAlarms.keys - wantedIds,
                toSchedule = toSchedule,
                unchanged = unchanged,
            )
        }

        private fun UpcomingAlarm.toRegistration(timeZone: TimeZone) = AlarmRegistration(
            alarmId = id.value,
            triggerAtMs = firesAt.toEpochMilliseconds(),
            occurrenceIdJson = Json.encodeToString(OccurrenceId.serializer(), event.occurrenceId),
            title = event.title,
            location = event.location,
            startMs = event.timing.startInstant(timeZone).toEpochMilliseconds(),
            endMs = event.timing.endInstant(timeZone).toEpochMilliseconds(),
            isAllDay = event.timing.isAllDay,
        )
    }
}
