package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<ChildEntity>)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    @Query("DELETE FROM children WHERE isSynced = 1 AND id NOT IN (:remainingIds)")
    suspend fun deleteOldSyncedChildren(remainingIds: List<UUID>)

    @androidx.room.Transaction
    suspend fun syncWithServer(remoteChildren: List<ChildEntity>) {
        if (remoteChildren.isNotEmpty()) {
            deleteOldSyncedChildren(remoteChildren.map { it.id })
            insertChildren(remoteChildren)
        } else {
            deleteOldSyncedChildren(emptyList())
        }
    }

    @Query("SELECT * FROM children WHERE isSynced = 0")
    suspend fun getUnsyncedChildren(): List<ChildEntity>

    @Query("UPDATE children SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: UUID)
}
