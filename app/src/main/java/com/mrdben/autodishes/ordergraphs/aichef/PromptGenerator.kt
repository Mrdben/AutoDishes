package com.mrdben.autodishes.ordergraphs.aichef

import android.util.Log
import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.dishgraphs.data.DishType
import com.mrdben.autodishes.dishgraphs.data.getDisplayName
import com.mrdben.autodishes.familygraphs.data.ElderList
import com.mrdben.autodishes.familygraphs.data.getDisplayName
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
        val userPrompt = generatePrompt(elders, mealType)
        if (userPrompt.isBlank()) return emptyList()
        // return value for debug
        return listOf(
            Dish(
                dishId = "TEMP_AI_DISH_01",
                name = "神秘菜品001",
                taste = "咸辣",
                type = DishType.WARM_VEGGIE,
                ingredients = "番茄、鸡蛋、盐、糖",
                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅"
            ),
            Dish(
                dishId = "TEMP_AI_DISH_02",
                name = "神秘菜品002",
                taste = "腥臭",
                type = DishType.WARM_VEGGIE,
                ingredients = "番茄、鸡蛋、盐、糖",
                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅"
            )
        )

//        val response = safeApiCall{ RetrofitClient.orderApiService.getAIDishes(userPrompt) }
//        when (response) {
//            is ResultWrapper.Success -> {
//                return response.data.dishList.map{ Dish(0, it) }
//            }
//            is ResultWrapper.Error -> {
//                val code = response.apiError.statusCode
//                val message = response.apiError.message
//                Log.e("AiChef", "AI菜品推荐获取失败。 错误码: $code, 错误信息: $message")
//                return emptyList()
//            }
//        }
    }

    private fun generatePrompt(
        elders: ElderList,
        mealType: MealType,
        reviewHistoryLength: Int = 48
    ): String {
        val reviewTimeStamp = getUtcTimeHoursAgo(reviewHistoryLength)
        val reviewOrders = orderRepo.orders.value.filter { it.mealTime > reviewTimeStamp }
        Log.d(
            "AiChef",
            "${reviewOrders.size} orders collected from the past $reviewHistoryLength hours."
        )

        val elderPrompt = generateElderPrompt(elders)
        val dishPrompt = reviewOrders.joinToString("\n") { order ->
            order.mealTime.split(" ")[0] + "吃了：" +
                generateDishPrompt(
                    order.dishIds.mapNotNull { dishId ->
                        dishRepo.getDishById(dishId)
                    }
                )
        }
        val finalPrompt =
            "请为以下用餐者推荐[${mealType.getDisplayName()}]：\n[" +
                    elderPrompt + "]\n他们过去几天吃过：\n[" + dishPrompt + "]"
        Log.d("AiChef", "提示词生成：\n$finalPrompt")
        return finalPrompt
    }

    private fun generateElderPrompt(elders: ElderList): String {
        val elderPrompts = List(elders.size) { index ->
            val elder = elders[index]
            """ 用餐者$index，
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
            在${it.cuisine.getDisplayName()}
            ${it.taste}口味
            ${it.type.getDisplayName()}：
            ${it.name}、
            """.replace("\n", "").replace(" ","")
        }
        return dishPrompts.joinToString("")
    }
}