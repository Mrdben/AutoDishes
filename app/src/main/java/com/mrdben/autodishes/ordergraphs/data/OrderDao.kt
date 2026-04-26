package com.mrdben.autodishes.ordergraphs.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishList
import kotlinx.coroutines.flow.Flow


@Dao
interface OrderDao {

    // 1. 观察所有菜品（实时更新，返回 Flow）
    @Query("SELECT * FROM order_table ORDER BY orderId ASC")
    fun getAllOrdersFlow(): Flow<OrderList>

    // 2. 插入单个菜品（冲突策略：覆盖）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    // 4. 更新菜品（编辑用）
    @Update
    suspend fun updateOrder(order: Order)

    // 5. 删除菜品
    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM order_table WHERE orderId = :orderId")
    suspend fun deleteOrderById(orderId: String)

    @Query("DELETE FROM order_table")
    suspend fun deleteAllOrders()
}