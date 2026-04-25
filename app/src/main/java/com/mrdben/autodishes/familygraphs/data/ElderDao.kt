package com.mrdben.autodishes.familygraphs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ElderDao {

    @Insert
    suspend fun insertElder(elder: Elder)

    @Update
    suspend fun updateElder(elder: Elder)

    @Query("DELETE FROM elder WHERE elderId = :elderId")
    suspend fun deleteElderById(elderId: String)

    @Query("SELECT * FROM elder ORDER BY name ASC")
    fun getAllEldersFlow(): Flow<ElderList>
}