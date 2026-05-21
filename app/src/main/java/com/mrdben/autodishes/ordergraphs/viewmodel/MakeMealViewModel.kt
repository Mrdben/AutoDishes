package com.mrdben.autodishes.ordergraphs.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import com.mrdben.autodishes.data.WorkState
import kotlinx.coroutines.flow.StateFlow

import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.familygraphs.data.ElderList
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.ordergraphs.data.MealType
import com.mrdben.autodishes.ordergraphs.data.Order
import com.mrdben.autodishes.ordergraphs.data.OrderRepository
import com.mrdben.autodishes.ordergraphs.data.NewOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MakeMealViewModel (
    private val familyRepository : FamilyRepository,
    private val dishRepository : DishRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val newOrderRepository = NewOrderRepository.instance
    val newOrder: StateFlow<Order> = newOrderRepository.newOrder
    private val _workState = MutableStateFlow<WorkState>(WorkState.Idle)
    fun initialize(orderId: String) {
        val existingOrder = orderRepository.getOrderById(orderId) ?:
                            orderRepository.orders.value.lastOrNull() ?: return
        val mealType = existingOrder.mealType
        val elderIds = existingOrder.elderIds.filter { familyRepository.getElderById(it) != null }
        val dishIds = if(existingOrder.orderId == orderId) { // created from existing order
                           newOrderRepository.clearOrder()
                           existingOrder.dishIds.filter { dishRepository.getDishById(it) != null }
                      } else emptyList() // created from empty, do not copy dishes, just elder list
        newOrderRepository.initialize(mealType, elderIds, dishIds)
    }

    fun setMealType(type : MealType) {
        newOrderRepository.setMealType(type)
    }

    @Transaction
    fun confirmOrder() {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            mergeTempDishes() // merge selected temp DishList to new order
            newOrderRepository.newOrder.value.dishIds.forEach {
                dishRepository.like(it)
            }
            dishRepository.syncDishWithServer() // upload new dish in repo to back end
            if (orderRepository.addOrder(newOrderRepository.newOrder.value))
                orderRepository.syncOrderWithServer()
            newOrderRepository.cleanupAiChef()
            newOrderRepository.clearOrder()
            _workState.value = WorkState.Idle
        }
    }

    fun getSelectedElderList(): ElderList {
        return newOrderRepository.newOrder.value.elderIds.mapNotNull {
            familyRepository.getElderById(it)
        }
    }
    fun getSelectedDishList(): DishList {
        return newOrderRepository.newOrder.value.dishIds.mapNotNull {
            dishRepository.getDishById(it)
        }
    }
    fun getSelectedTempDishList(): DishList {
        return newOrderRepository.tempDishes.value.filter {
            it.dishId in newOrderRepository.tempDishIdList.value
        }
    }
    fun setupAIChef() {
        newOrderRepository.setupAiChef(familyRepository, dishRepository, orderRepository)
    }
    // merge selected temp-Dishes to dish-repo
    // merge selected temp-Dish-Ids to new-order-dish-id-list
    @Transaction
    private suspend fun mergeTempDishes() {
        val dishList = newOrderRepository.newOrder.value.dishIds.toMutableList()
        Log.d("MakeMealVM","mergeTempDishes")
        val selectedTempDishes = getSelectedTempDishList()
        Log.d("MakeMealVM","selectedTempDishes: ${selectedTempDishes.size}")
        for ((index, tempDish)  in selectedTempDishes.withIndex()) {
            // step 1: merge dish repo
            Log.d("MakeMealVM","Processing : ${tempDish.name} >>> ${tempDish.dishId}")
            val existingDish = dishRepository.getDishByName(tempDish.name) // 直接相同，还是找相似？如何定义相似
            Log.d("MakeMealVM","has existing dish: ${existingDish!=null}")
            // 如果存在同名的菜品，则把dishIdList中的id换成已存在的菜品，否则新建一个菜品
            val newDishId = existingDish?.dishId ?: "dish_${System.currentTimeMillis()}_$index"
            Log.d("MakeMealVM","New Dish ID: $newDishId")
            existingDish ?: run {  // 没有一样名字的菜
                Log.d("MakeMealVM","Add Dish to repo: ${tempDish.dishId} -> $newDishId")
                dishRepository.addDish(dish = tempDish, dishId = newDishId)
            }
            // step 2: merge selection list
            if (newDishId !in dishList) {
                Log.d("MakeMealVM","dishId [$newDishId] added to list")
                dishList.add(newDishId)
            }
        }
        newOrderRepository.setDishIdList(dishList)
    }
}