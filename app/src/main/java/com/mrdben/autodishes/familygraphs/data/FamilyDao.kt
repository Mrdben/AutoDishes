package com.mrdben.autodishes.familygraphs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    // 插入家庭
    @Insert
    suspend fun insertFamily(family: Family)

    // 更新家庭
    @Update
    suspend fun updateFamily(family: Family)

    // 获取唯一的家庭（单家庭）
    @Query("SELECT * FROM family LIMIT 1")
    fun getFamilyFlow(): Flow<Family?>

    // 清空表（可选）
    @Query("DELETE FROM family")
    suspend fun deleteAllFamilies()
}