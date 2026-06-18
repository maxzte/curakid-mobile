package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.misterio.core.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE childId = :childId AND isDeleted = 0")
    fun getEvents(childId: UUID): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id AND isDeleted = 0")
    fun getEventById(id: UUID): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE childId = :childId AND date = :date AND isDeleted = 0")
    fun getEventsByDate(childId: UUID, date: LocalDateTime): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("UPDATE events SET isDeleted = 1 WHERE id = :eventId")
    suspend fun softDeleteEvent(eventId: UUID)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun hardDeleteEvent(eventId: UUID)

    @Query("DELETE FROM events WHERE childId = :childId AND isSynced = 1 AND isDeleted = 0 AND id NOT IN (:remainingIds)")
    suspend fun deleteOldSyncedEvents(childId: UUID, remainingIds: List<UUID>)

    @Query("SELECT id FROM events WHERE childId = :childId AND isSynced = 1 AND isDeleted = 0 AND id NOT IN (:remainingIds)")
    suspend fun getDeletedEventIds(childId: UUID, remainingIds: List<UUID>): List<UUID>

    @Transaction
    suspend fun syncWithServer(childId: UUID, remoteEvents: List<EventEntity>) {
        // 1. Видаляємо ті, яких більше немає на сервері (тільки для вже синхронізованих)
        deleteOldSyncedEvents(childId, remoteEvents.map { it.id })

        // 2. Оновлюємо або вставляємо нові
        insertEvents(remoteEvents)
    }

    @Query("UPDATE events SET isSynced = 1 WHERE id = :eventId")
    suspend fun markAsSynced(eventId: UUID)

    @Query("UPDATE events SET isSynced = 1 WHERE id IN (:eventIds)")
    suspend fun markAsSynced(eventIds: List<UUID>)

    @Query("SELECT * FROM events WHERE childId = :childId AND isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedEvents(childId: UUID): List<EventEntity>

    @Query("SELECT * FROM events WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getAllUnsyncedEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE isDeleted = 1")
    suspend fun getPendingDeleteEvents(): List<EventEntity>
}
