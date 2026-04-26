package com.mrdben.autodishes.familygraphs.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrdben.autodishes.dishgraphs.data.Dish
import kotlinx.coroutines.flow.Flow

@Dao
interface ElderDao {
    @Query("SELECT * FROM elder ORDER BY elderId ASC")
    fun getAllEldersFlow(): Flow<ElderList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElder(elder: Elder)

    @Update
    suspend fun updateElder(elder: Elder)

    @Delete
    suspend fun deleteElder(elder: Elder)

    @Query("DELETE FROM elder WHERE elderId = :elderId")
    suspend fun deleteElderById(elderId: String)

    @Query("DELETE FROM elder")
    suspend fun deleteAllElders()
}