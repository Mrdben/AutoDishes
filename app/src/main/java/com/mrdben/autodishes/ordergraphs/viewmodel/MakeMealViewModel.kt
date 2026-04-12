package com.mrdben.autodishes.ordergraphs.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

import com.mrdben.autodishes.data.Dish
import com.mrdben.autodishes.data.DishRepository
import com.mrdben.autodishes.data.Elder
import com.mrdben.autodishes.data.FamilyRepository
import com.mrdben.autodishes.data.MealType
import com.mrdben.autodishes.data.Order
import com.mrdben.autodishes.data.OrderRepository
import com.mrdben.autodishes.data.NewOrderRepository

class MakeMealViewModel : ViewModel() {
    private val newOrderRepository = NewOrderRepository.instance
    val newOrder: StateFlow<Order?> = newOrderRepository.newOrder

    private val orderRepository = OrderRepository.instance
    private val familyRepository = FamilyRepository.instance
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

    fun getElderById(id: String): Elder? {
        return familyRepository.getElderById(id)
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