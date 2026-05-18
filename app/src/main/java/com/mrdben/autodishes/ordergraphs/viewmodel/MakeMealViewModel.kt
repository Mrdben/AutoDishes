package com.mrdben.autodishes.ordergraphs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow

import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.familygraphs.data.ElderList
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.ordergraphs.data.MealType
import com.mrdben.autodishes.ordergraphs.data.Order
import com.mrdben.autodishes.ordergraphs.data.OrderRepository
import com.mrdben.autodishes.ordergraphs.data.NewOrderRepository
import kotlinx.coroutines.launch

class MakeMealViewModel (
    private val familyRepository : FamilyRepository,
    private val dishRepository : DishRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val newOrderRepository = NewOrderRepository.instance
    val newOrder: StateFlow<Order> = newOrderRepository.newOrder

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

    fun confirmOrder() {
        orderRepository.addOrder(newOrder.value)
        newOrderRepository.clearOrder()
    }

    fun getElderList(): ElderList {
        val elderIds = newOrderRepository.getElderIdList()
        return elderIds.mapNotNull { familyRepository.getElderById(it) }
    }
    fun getDishList(): DishList {
        val dishIds = newOrderRepository.getDishIdList()
        return dishIds.mapNotNull { dishRepository.getDishById(it) }
    }

    fun updateFavorite() {
        newOrder.value.dishIds.forEach {
            dishRepository.like(it)
        }
    }

    fun autoDishes() {
         viewModelScope.launch {
             newOrderRepository.setupAiChef(getElderList(),
                                            newOrder.value.mealType,
                                            dishRepository,
                                            orderRepository)
             newOrderRepository.getDishesFromAI()
        }
    }
}