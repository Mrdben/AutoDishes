package com.mrdben.autodishes.dishgraphs.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DishDao {

    // 1. 观察所有菜品（实时更新，返回 Flow）
    @Query("SELECT * FROM dish ORDER BY dishId ASC")
    fun getAllDishesFlow(): Flow<DishList>

    // 2. 插入单个菜品（冲突策略：覆盖）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish)

    // 4. 更新菜品（编辑用）
    @Update
    suspend fun updateDish(dish: Dish)

    // 5. 删除菜品
    @Delete
    suspend fun deleteDish(dish: Dish)

    @Query("DELETE FROM dish WHERE dishId = :dishId")
    suspend fun deleteDishById(dishId: String)

    @Query("DELETE FROM dish")
    suspend fun deleteAllDishes()
}