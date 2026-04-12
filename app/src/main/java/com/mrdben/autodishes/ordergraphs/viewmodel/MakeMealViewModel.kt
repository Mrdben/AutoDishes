package com.mrdben.autodishes.ordergraphs.viewmodel

import androidx.lifecycle.ViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    fun setElderIdList(elderList: List<String>) {
        newOrderRepository.setElderIdList(elderList)
    }
    fun setDishIdList(dishList: List<String>) {
        newOrderRepository.setDishIdList(dishList)
    }
    fun clearOrder() {
        newOrderRepository.clearOrder()
    }
    fun confirmOrder() {
        val orderToSubmit = newOrder.value ?: return
        orderRepository.addOrder(orderToSubmit)
        clearOrder()
    }

    fun getElderById(id: String): Elder? {
        return familyRepository.getElderById(id)
    }
    fun getDishById(id: String): Dish? {
        return dishRepository.getDishById(id)
    }
}