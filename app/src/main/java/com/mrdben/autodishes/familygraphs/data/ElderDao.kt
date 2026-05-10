package com.mrdben.autodishes.familygraphs.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrdben.autodishes.data.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ElderDao {
    @Query("SELECT * FROM elder WHERE userId = :userId AND isDeleted = 0 ORDER BY elderId ASC")
    fun getAllEldersFlow(userId: Int): Flow<ElderList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElder(elder: Elder)

    @Update
    suspend fun updateElder(elder: Elder)

    @Delete
    suspend fun deleteElder(elder: Elder)

    @Query("SELECT * FROM elder WHERE elderId = :elderId")
    suspend fun getElderById(elderId: Int): Elder?

    @Query("DELETE FROM elder WHERE elderId = :elderId")
    suspend fun deleteElderById(elderId: String)

    @Query("DELETE FROM elder")
    suspend fun deleteAllElders()

    @Query("SELECT * FROM elder WHERE syncStatus != :syncedStatus AND userId == :userId")
    suspend fun getNeedSyncElders(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED): ElderList
}