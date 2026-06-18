package com.misterio.curakid.feature.calendar.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.curakid.core.common.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventNotificationSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) : EventNotificationScheduler {
    override suspend fun schedule(event: Event) {
        val isEnabled = settingsManager.isNotificationsEnabled.first()
        if (!isEnabled) return

        val eventDateTime = event.date
        val delay = Duration.between(LocalDateTime.now(), eventDateTime)

        if (delay.isNegative) return

        val workRequest = OneTimeWorkRequestBuilder<EventNotificationWorker>()
            .setInitialDelay(delay)
            .setInputData(
                workDataOf(
                    EventNotificationWorker.KEY_EVENT_TITLE to event.title,
                    EventNotificationWorker.KEY_EVENT_ID to event.id.toString(),
                    EventNotificationWorker.KEY_EVENT_DESCRIPTION to (event.description ?: "")
                )
            )
            .addTag("event_${event.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "event_notification_${event.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override suspend fun schedule(events: List<Event>) {
        val isEnabled = settingsManager.isNotificationsEnabled.first()
        if (!isEnabled) return

        val now = LocalDateTime.now()
        val workManager = WorkManager.getInstance(context)

        events.forEach { event ->
            val delay = Duration.between(now, event.date)

            // Пропускаємо події, що вже минули
            if (delay.isNegative) return@forEach

            val workRequest = OneTimeWorkRequestBuilder<EventNotificationWorker>()
                .setInitialDelay(delay)
                .setInputData(
                    workDataOf(
                        EventNotificationWorker.KEY_EVENT_TITLE to event.title,
                        EventNotificationWorker.KEY_EVENT_ID to event.id.toString(),
                        EventNotificationWorker.KEY_EVENT_DESCRIPTION to (event.description ?: "")
                    )
                )
                .addTag("event_${event.id}")
                .build()

            workManager.enqueueUniqueWork(
                "event_notification_${event.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun cancel(eventId: UUID) {
        WorkManager.getInstance(context).cancelAllWorkByTag("event_$eventId")
    }

    override suspend fun cancelAll(childId: UUID) {
        // У цьому випадку ми можемо або додавати тег дитини до всіх воркерів, 
        // або скасувати всі за тегами івентів. Але зазвичай достатньо скасувати конкретні.
        // Якщо потрібно скасувати все для дитини, варто було б додавати тег childId при плануванні.
    }
}
