package com.mrdben.autodishes.dishgraphs.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.dishgraphs.api.UpdateInfo

@Dao
interface DishDao {
    @Query("SELECT * FROM dish WHERE userId = :userId AND isDeleted = 0 ORDER BY dishId ASC")
    fun getAllDishesFlow(userId: Int): Flow<DishList>

    @Query("SELECT dishId, updatedAt FROM dish WHERE userId = :userId AND isDeleted = 0")
    suspend fun getAllDishesInfo(userId: Int): List<UpdateInfo>

    @Upsert
    suspend fun updateDish(dish: Dish)

    @Query("UPDATE dish SET isDeleted = 1, syncStatus = :syncedStatus WHERE dishId = :dishId")
    suspend fun softDeleteDish(dishId: String, syncedStatus: SyncStatus = SyncStatus.TO_DELETE)

    @Query("UPDATE dish SET favoriteLevel = :level, syncStatus = :syncedStatus WHERE dishId = :dishId")
    suspend fun updateDishLike(dishId: String, level: Int, syncedStatus: SyncStatus = SyncStatus.TO_UPLOAD)

    @Query("SELECT * FROM dish WHERE dishId = :dishId")
    suspend fun getDishById(dishId: String): Dish?

    @Query("DELETE FROM dish WHERE isDeleted = 1 AND syncStatus = :syncedStatus AND userId = :userId")
    suspend fun cleanDeletedDishes(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED)

    @Query("SELECT * FROM dish WHERE syncStatus != :syncedStatus AND userId = :userId")
    suspend fun getNeedSyncDishes(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED): DishList
}