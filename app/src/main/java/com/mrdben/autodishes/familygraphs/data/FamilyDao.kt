package com.mrdben.autodishes.familygraphs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM family LIMIT 1")
    fun getFamilyFlow(): Flow<Family?>

    @Query("SELECT * FROM family LIMIT 1")
    suspend fun getFamilyFirst(): Family?

    // 清空表（可选）
    @Query("DELETE FROM family")
    suspend fun deleteAllFamilies()
}