package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val birthDate: LocalDate,
    val gender: String,
    val isPremature: Boolean = false,
    val gestationalWeeks: Int? = null,
    val allergies: List<String> = emptyList(),
    val chronicDiseases: List<String> = emptyList(),
    val pastDiseases: List<String> = emptyList(),
    val specialFeatures: List<String> = emptyList(),
    val isSynced: Boolean = true,
)
