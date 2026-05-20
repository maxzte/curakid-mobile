package com.misterio.core.database.dao

import androidx.room.*
import com.misterio.core.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE childId = :childId")
    fun getEvents(childId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE childId = :childId AND date = :date")
    fun getEventsByDate(childId: String, date: LocalDateTime): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM events WHERE childId = :childId AND isSynced = 1 AND id NOT IN (:remainingIds)")
    suspend fun deleteOldSyncedEvents(childId: String, remainingIds: List<String>)

    @Query("SELECT id FROM events WHERE childId = :childId AND isSynced = 1 AND id NOT IN (:remainingIds)")
    suspend fun getDeletedEventIds(childId: String, remainingIds: List<String>): List<String>

    @Transaction
    suspend fun syncWithServer(childId: String, remoteEvents: List<EventEntity>) {
        // 1. Видаляємо ті, яких більше немає на сервері (тільки для вже синхронізованих)
        deleteOldSyncedEvents(childId, remoteEvents.map { it.id })
        
        // 2. Оновлюємо або вставляємо нові
        insertEvents(remoteEvents)
    }

    @Query("UPDATE events SET isSynced = 1 WHERE id = :eventId")
    suspend fun markAsSynced(eventId: String)

    @Query("UPDATE events SET isSynced = 1 WHERE id IN (:eventIds)")
    suspend fun markAsSynced(eventIds: List<String>)

    @Query("SELECT * FROM events WHERE childId = :childId AND isSynced = 0")
    suspend fun getUnsyncedEvents(childId: String): List<EventEntity>
}
