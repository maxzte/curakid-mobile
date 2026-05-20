package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import kotlinx.coroutines.flow.Flow

interface ChildRepository {

    fun getChildren(): Flow<List<Child>>

    fun getChild(childId: String): Flow<Child?>

    suspend fun syncChildren(force: Boolean): Result<Unit>

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

    fun getSelectedChild(): Flow<Child?>

    suspend fun setSelectedChildId(id: String?)

}
