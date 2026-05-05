package com.mrdben.autodishes.familygraphs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mrdben.autodishes.data.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Dao
interface FamilyDao {
    // 插入家庭
    @Insert
    suspend fun insertFamily(family: Family)
    // 更新家庭
    @Update
    suspend fun updateFamily(family: Family)
    @Query("SELECT * FROM family WHERE userId = :userId LIMIT 1")
    fun getFamilyFlow(userId: Int): Flow<Family?>
    @Query("SELECT * FROM family WHERE userId = :userId LIMIT 1")
    suspend fun getFamilyFirst(userId: Int): Family?
    // 清空表（可选）
    @Query("DELETE FROM family")
    suspend fun deleteAllFamilies()

    @Query("SELECT * FROM family WHERE syncStatus != :syncedStatus AND userId == :userId")
    suspend fun getNeedSyncFamilies(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED): List<Family>

    /**
     * 查询：所有 待上传 的数据（新增/修改）
     */
//    @Query("SELECT * FROM family WHERE syncStatus = :toUploadStatus")
//    suspend fun getToUploadFamilies(toUploadStatus: SyncStatus = SyncStatus.TO_UPLOAD): List<Family>

    /**
     * 查询：所有 待删除 的数据
     */
//    @Query("SELECT * FROM family WHERE syncStatus = :toDeleteStatus")
//    suspend fun getToDeleteFamilies(toDeleteStatus: SyncStatus = SyncStatus.TO_DELETE): List<Family>

    /**
     * 根据 serverId 查询本地数据（同步更新用）
     */
//    @Query("SELECT * FROM family WHERE serverId = :serverId LIMIT 1")
//    suspend fun getFamilyByServerId(serverId: String): Family?

    /**
     * 批量更新同步状态（同步成功后批量标记为已同步）
     */
    @Query("UPDATE family SET syncStatus = :syncedStatus, updatedAt = :updatedAt WHERE familyId = :familyId")
    suspend fun markFamiliesAsSynced(familyId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED, updatedAt: String)

    /**
     * 清理：已经同步且标记删除的数据（真删除）
     */
    @Query("DELETE FROM family WHERE isDeleted = 1 AND syncStatus = :syncedStatus AND userId = :userId")
    suspend fun cleanDeletedSyncedFamilies(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED)
}