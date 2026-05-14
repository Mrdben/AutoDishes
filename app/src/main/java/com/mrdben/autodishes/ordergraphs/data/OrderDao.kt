package com.mrdben.autodishes.ordergraphs.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.ordergraphs.api.UpdateInfo
import kotlinx.coroutines.flow.Flow


@Dao
interface OrderDao {
    @Query("SELECT * FROM order_table WHERE userId = :userId AND isDeleted = 0 ORDER BY orderId ASC")
    fun getAllOrdersFlow(userId: Int): Flow<OrderList>

    @Query("SELECT orderId, updatedAt FROM order_table WHERE userId = :userId AND isDeleted = 0")
    suspend fun getAllOrdersInfo(userId: Int): List<UpdateInfo>

    @Upsert
    suspend fun updateOrder(order: Order)

    @Query("UPDATE order_table SET isDeleted = 1, syncStatus = :syncedStatus WHERE orderId = :orderId")
    suspend fun softDeleteOrder(orderId: String, syncedStatus: SyncStatus = SyncStatus.TO_DELETE)

    @Query("SELECT * FROM order_table WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): Order?

    @Query("DELETE FROM order_table WHERE isDeleted = 1 AND syncStatus = :syncedStatus AND userId = :userId")
    suspend fun cleanDeletedOrders(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED)

    @Query("SELECT * FROM order_table WHERE syncStatus != :syncedStatus AND userId = :userId")
    suspend fun getNeedSyncOrders(userId: Int, syncedStatus: SyncStatus = SyncStatus.SYNCED): OrderList
}