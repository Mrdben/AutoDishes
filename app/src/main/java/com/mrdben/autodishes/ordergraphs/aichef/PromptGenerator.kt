package com.mrdben.autodishes.ordergraphs.aichef

import android.util.Log
import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.dishgraphs.data.getDisplayName
import com.mrdben.autodishes.familygraphs.data.ElderList
import com.mrdben.autodishes.familygraphs.data.getDisplayName
import com.mrdben.autodishes.ordergraphs.api.AiChefRequest
import com.mrdben.autodishes.ordergraphs.data.MealType
import com.mrdben.autodishes.ordergraphs.data.OrderRepository
import com.mrdben.autodishes.ordergraphs.data.getDisplayName
import com.mrdben.autodishes.utils.getUtcTimeHoursAgo

class AiChef(
    private val dishRepo: DishRepository,
    private val orderRepo: OrderRepository
) {
    suspend fun getAiChefDishes(
        elders: ElderList,
        mealType: MealType,
    ): DishList {
        val request = AiChefRequest(
            elderCount = elders.size,
            mealType = mealType.getDisplayName(),
            elderInfo = generateElderPrompt(elders),
            orderHist = generateHistoryPrompt()
        )
        val response = safeApiCall{ RetrofitClient.orderApiService.getAIDishes(request) }
        when (response) {
            is ResultWrapper.Success -> {
                Log.d("AiChef", "AI菜品推荐获取成功。 模型返回${response.data.dishList.size}道菜品。")
                return response.data.dishList.mapIndexed { index, dto ->
                    Dish(0, dto).copy(
                        dishId = "TEMP_AI_DISH_${index+1}"
                    )
                }
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("AiChef", "AI菜品推荐获取失败。 错误码: $code, 错误信息: $message")
                return emptyList()
            }
        }
    }

    private fun generateHistoryPrompt(
        reviewHistoryLength: Int = 48
    ): String {
        val reviewTimeStamp = getUtcTimeHoursAgo(reviewHistoryLength)
        val reviewOrders = orderRepo.orders.value.filter { it.mealTime > reviewTimeStamp }
        Log.d(
            "AiChef",
            "${reviewOrders.size} orders collected from the past $reviewHistoryLength hours."
        )
        val dishPrompt = reviewOrders.joinToString("\n") { order ->
            "在" + order.mealTime.split(" ")[0] + "的${order.mealType.getDisplayName()}吃了：" +
                generateDishPrompt(
                    order.dishIds.mapNotNull { dishId ->
                        dishRepo.getDishById(dishId)
                    }
                )
        }
        return dishPrompt
    }

    private fun generateElderPrompt(elders: ElderList): String {
        val elderPrompts = List(elders.size) { index ->
            val elder = elders[index]
            """ 用餐者${index+1}，
            性别${elder.gender.getDisplayName()}，
            年龄：${elder.age}岁，
            过敏物：${elder.allergens.joinToString("、").takeIf { it.isNotBlank() } ?: "无"}，
            疾病：${elder.diseases.joinToString("、").takeIf { it.isNotBlank() } ?: "无"}，
            忌口：${elder.diseases.joinToString("、").takeIf { it.isNotBlank() } ?: "无"}，
            口味偏好：${elder.tastePreferences.joinToString("、").takeIf { it.isNotBlank() } ?: "无"}；
            """.replace("\n", "").replace(" ","")
        }
        return elderPrompts.joinToString("\n")
    }

    private fun generateDishPrompt(dishes: DishList): String {
        val dishPrompts = dishes.map {
            """
            ${it.cuisine.getDisplayName()}
            ${it.taste}口味
            ${it.type.getDisplayName()}：
            ${it.name}、
            """.replace("\n", "").replace(" ","")
        }
        return dishPrompts.joinToString("")
    }
}