package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.misterio.core.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_messages WHERE childId = :childId ORDER BY createdAt ASC")
    fun getMessagesForChild(childId: UUID): Flow<List<ChatMessageEntity>>

    @Upsert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Upsert
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE childId = :childId")
    suspend fun clearMessagesForChild(childId: UUID)
}
