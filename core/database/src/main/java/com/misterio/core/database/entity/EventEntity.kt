package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val title: String,
    val type: String,
    val date: LocalDateTime,
    val location: String?,
    val description: String?,
    val isSynced: Boolean
)
