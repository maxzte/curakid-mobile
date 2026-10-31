package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: UUID,
    val childId: UUID,
    val userText: String,
    val aiText: String,
    val createdAt: String,
    val isStreaming: Boolean = false,
    val hasError: Boolean = false,
    val urgencyLevel: String = "ROUTINE",
    val verifiedSources: List<String> = emptyList(),
)
