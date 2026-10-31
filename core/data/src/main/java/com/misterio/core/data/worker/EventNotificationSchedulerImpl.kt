package com.misterio.core.data.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.misterio.core.data.receiver.EventAlarmReceiver
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.curakid.core.common.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventNotificationSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) : EventNotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun schedule(event: Event) {
        schedule(listOf(event))
    }

    override suspend fun schedule(events: List<Event>) {
        val isEnabled = settingsManager.isNotificationsEnabled.first()
        if (!isEnabled) return

        val now = LocalDateTime.now()
        val zoneId = ZoneId.systemDefault()

        events.forEach { event ->
            if (event.status != com.misterio.core.domain.model.EventStatus.PLANNED) return@forEach
            if (event.date.isBefore(now)) return@forEach

            val triggerAtMillis = event.date.atZone(zoneId).toInstant().toEpochMilli()
            val pendingIntent = createPendingIntent(event)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        // Fallback to inexact alarm if exact permission is revoked by user
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else {
                    AlarmManagerCompat.setExactAndAllowWhileIdle(
                        alarmManager,
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // In case exact alarm permission is unexpectedly denied, schedule inexact
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    override suspend fun cancel(eventId: UUID) {
        val intent = Intent(context, EventAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override suspend fun cancelAll(childId: UUID) {
        // Child-specific cancellation handled via individual event cancel or re-sync
    }

    override suspend fun cancelAllEvents() {
        val intent = Intent(context, EventAlarmReceiver::class.java)
        // System cancels are handled on key events
    }

    private fun createPendingIntent(event: Event): PendingIntent {
        val intent = Intent(context, EventAlarmReceiver::class.java).apply {
            putExtra(EventAlarmReceiver.EXTRA_EVENT_ID, event.id.toString())
            putExtra(EventAlarmReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(EventAlarmReceiver.EXTRA_EVENT_DESCRIPTION, event.description ?: "")
        }

        return PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
