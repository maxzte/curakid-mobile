package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.misterio.core.database.entity.ChildEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ChildDao {

    @Query("SELECT * FROM children")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChildById(childId: UUID): ChildEntity?

    @Query("SELECT * FROM children WHERE id = :childId")
    fun getChildByIdFlow(childId: UUID): Flow<ChildEntity?>

    @Upsert
    suspend fun insertChild(child: ChildEntity)

    @Upsert
    suspend fun insertChildren(children: List<ChildEntity>)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    @Query("DELETE FROM children WHERE isSynced = 1 AND id NOT IN (:remainingIds)")
    suspend fun deleteOldSyncedChildren(remainingIds: List<UUID>)

    @Query("DELETE FROM children WHERE isSynced = 1")
    suspend fun deleteAllSyncedChildren()

    @androidx.room.Transaction
    suspend fun syncWithServer(remoteChildren: List<ChildEntity>) {
        val unsyncedIds = getUnsyncedChildren().map { it.id }.toSet()
        if (remoteChildren.isNotEmpty()) {
            val remoteIds = remoteChildren.map { it.id }
            deleteOldSyncedChildren(remoteIds)
            val childrenToInsert = remoteChildren.filterNot { it.id in unsyncedIds }
            if (childrenToInsert.isNotEmpty()) {
                insertChildren(childrenToInsert)
            }
        } else {
            deleteAllSyncedChildren()
        }
    }

    @Query("SELECT * FROM children WHERE isSynced = 0")
    suspend fun getUnsyncedChildren(): List<ChildEntity>

    @Query("UPDATE children SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: UUID)
}
