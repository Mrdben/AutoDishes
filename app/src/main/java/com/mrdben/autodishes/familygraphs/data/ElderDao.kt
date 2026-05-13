package com.mrdben.autodishes.familygraphs.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.familygraphs.api.UpdateInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ElderDao {
    @Query("SELECT * FROM elder WHERE userId = :userId AND isDeleted = 0 ORDER BY elderId ASC")
    fun getAllEldersFlow(userId: Int): Flow<ElderList>

    @Query("SELECT elderId, updatedAt FROM elder WHERE userId = :userId AND isDeleted = 0")
    suspend fun getAllEldersInfo(userId: Int): List<UpdateInfo>

    @Upsert
    suspend fun updateElder(elder: Elder)

    @Query("UPDATE elder SET isDeleted = 1, syncStatus = :syncedStatus WHERE elderId = :elderId")
    suspend fun softDeleteElder(elderId: String, syncedStatus: SyncStatus = SyncStatus.TO_DELETE)


    @Query("SELECT * FROM elder WHERE elderId = :elderId")
    suspend fun getElderById(elderId: String): Elder?

    @Query("DELETE FROM elder WHERE elderId = :elderId")
    suspend fun deleteElderById(elderId: String)

    @Query("DELETE FROM elder")
    suspend fun deleteAllElders()

    @Query("DELETE FROM elder WHERE isDeleted = 1 AND syncStatus = :syncedStatus AND userId = :userId")
    suspend fun cleanDeletedElders(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED)
    @Query("SELECT * FROM elder WHERE syncStatus != :syncedStatus AND userId = :userId")
    suspend fun getNeedSyncElders(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED): ElderList
}