package com.mrdben.autodishes.ordergraphs.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.familygraphs.data.Elder
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.ordergraphs.data.MealType
import com.mrdben.autodishes.ordergraphs.data.Order
import com.mrdben.autodishes.ordergraphs.data.OrderRepository
import com.mrdben.autodishes.ordergraphs.data.NewOrderRepository

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
        val dishIds = existingOrder.dishIds.filter { dishRepository.getDishById(it) != null }
        newOrderRepository.initialize(mealType, elderIds, dishIds)
    }

    fun setMealType(type : MealType) {
        newOrderRepository.setMealType(type)
    }
    fun getElderIdList(): List<String> {
        return newOrderRepository.getElderIdList()
    }
    fun getDishIdList(): List<String> {
        return newOrderRepository.getDishIdList()
    }
    fun clearOrder() {
        newOrderRepository.clearOrder()
    }
    fun confirmOrder() {
        orderRepository.addOrder(newOrder.value)
        clearOrder()
    }

    fun getElderById(elderId: String): Elder? {
        return familyRepository.getElderById(elderId)
    }
    fun getDishById(id: String): Dish? {
        return dishRepository.getDishById(id)
    }

    fun updateFavorite() {
        newOrder.value.dishIds.forEach {
            dishRepository.like(it)
        }
    }
}