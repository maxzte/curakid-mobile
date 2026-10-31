package com.misterio.core.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.misterio.core.database.dao.EventDao
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.domain.util.EventNotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var eventDao: EventDao

    @Inject
    lateinit var notificationScheduler: EventNotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Reschedule all future events
                val allEvents = eventDao.getAllActiveEvents().first().map { it.toDomain() }
                val now = LocalDateTime.now()
                val futureEvents = allEvents.filter { it.date.isAfter(now) }
                if (futureEvents.isNotEmpty()) {
                    notificationScheduler.schedule(futureEvents)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
