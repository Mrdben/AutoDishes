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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class MakeMealViewModel (
    private val familyRepository : FamilyRepository
) : ViewModel() {
    private val newOrderRepository = NewOrderRepository.instance
    val newOrder: StateFlow<Order?> = newOrderRepository.newOrder

    private val orderRepository = OrderRepository.instance

    private val dishRepository = DishRepository.instance

    fun createNewOrder() {
        newOrderRepository.createNewOrder()
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
        val orderToSubmit = newOrder.value ?: return
        orderRepository.addOrder(orderToSubmit.copy(createTime = System.currentTimeMillis()))
        clearOrder()
    }

    suspend fun getElderById(elderId: String): Elder? {
        return familyRepository.getElderById(elderId)
    }
    fun getDishById(id: String): Dish? {
        return dishRepository.getDishById(id)
    }

    fun updateFavorite() {
        val orderToSubmit = newOrder.value ?: return
        orderToSubmit.dishIds.forEach {
            dishRepository.like(it)
        }
    }
}