package com.misterio.core.database.dao

import androidx.room.*
import com.misterio.core.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE childId = :childId")
    fun getEvents(childId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE childId = :childId AND date = :date")
    fun getEventsByDate(childId: String, date: LocalDateTime): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("UPDATE events SET isSynced = 1 WHERE id = :eventId")
    suspend fun markAsSynced(eventId: String)

    @Query("SELECT * FROM events WHERE isSynced = 0")
    suspend fun getUnsyncedEvents(): List<EventEntity>
}
