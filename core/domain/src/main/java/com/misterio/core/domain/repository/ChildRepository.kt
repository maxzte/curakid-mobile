package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import kotlinx.coroutines.flow.Flow

interface ChildRepository {

    fun getChildren(): Flow<List<Child>>

    suspend fun getChild(childId: String): Child?

    suspend fun syncChildren(): Result<Unit>

    suspend fun addChild(
        name: String,
        birthDate: Long,
        gender: Gender,
        isPremature: Boolean = false,
        allergies: List<String> = emptyList(),
        chronicDiseases: List<String> = emptyList(),
        pastDiseases: List<String> = emptyList(),
        specialFeatures: List<String> = emptyList()
    ): Result<String>

    suspend fun updateChild(child: Child): Result<Unit>

    fun getSelectedChildId(): Flow<String?>

    suspend fun setSelectedChildId(id: String?)

}
