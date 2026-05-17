package com.mrdben.autodishes.dishgraphs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.dishgraphs.api.DishDto
import com.mrdben.autodishes.utils.getCurrentUtcTime

@Entity(tableName = "dish")
data class Dish(
    @PrimaryKey
    val dishId: String,
    val userId: Int = 0,
    val name: String,
    val taste: String,
    val type: DishType,
    val cuisine: CuisineType = CuisineType.FAMILY,
    val ingredients: String,
    val recipe: String,
    val favoriteLevel: Int = 0,
    val comments: String,

    val isDeleted: Boolean = false, // 软删除标记
    val syncStatus: SyncStatus = SyncStatus.TO_UPLOAD, // 同步状态
    val createdAt: String = getCurrentUtcTime(), // 创建时间， 服务器时间，非本地时间
    val updatedAt: String = getCurrentUtcTime() // 更新时间，服务器时间，非本地时间
) {
    constructor(
        userId : Int,
        dishDto : DishDto
    ) : this (
        dishId = dishDto.dishId,
        userId = userId,
        name = dishDto.name,
        taste = dishDto.taste,
        type = dishDto.type,
        cuisine = dishDto.cuisine,
        ingredients = dishDto.ingredients,
        recipe = dishDto.recipe,
        favoriteLevel = dishDto.favoriteLevel,
        comments = dishDto.comments,
        isDeleted = dishDto.isDeleted,
        syncStatus = SyncStatus.SYNCED,
        updatedAt = dishDto.updatedAt,
        createdAt = dishDto.createdAt
    )
}

typealias DishList = List<Dish>

enum class DishType {
    //按照中餐分类
    COLD_MEAT,
    COLD_VEGGIE,
    WARM_MEAT,
    WARM_VEGGIE,
    SOUP,
    STAPLE //主食
}

fun DishType.getDisplayName(): String {
    return when (this) {
        DishType.COLD_MEAT -> "冷荤"
        DishType.COLD_VEGGIE -> "冷素"
        DishType.WARM_MEAT -> "热荤"
        DishType.WARM_VEGGIE -> "热素"
        DishType.SOUP -> "汤"
        DishType.STAPLE -> "主食"
    }
}

enum class CuisineType {
    SHANDONG,   // 鲁菜
    SICHUAN,    // 川菜
    CANTONESE,  // 粤菜
    //JIANGSU,    // 苏菜
    //FUJIAN,     // 闽菜
    //ZHEJIANG,   // 浙菜
    HUNAN,      // 湘菜
    ANHUI,       // 徽菜
    BEIJING,
    SHANGHAI,
    DONGBEI,
    FAMILY      //家常菜
}

fun CuisineType.getDisplayName(): String {
    return when (this) {
        CuisineType.SHANDONG -> "鲁菜"
        CuisineType.SICHUAN -> "川菜"
        CuisineType.CANTONESE -> "粤菜"
//        CuisineType.JIANGSU -> "苏菜"
//        CuisineType.FUJIAN -> "闽菜"
//        CuisineType.ZHEJIANG -> "浙菜"
        CuisineType.HUNAN -> "湘菜"
        CuisineType.ANHUI -> "徽菜"
        CuisineType.BEIJING -> "京菜"
        CuisineType.SHANGHAI -> "沪菜"
        CuisineType.DONGBEI -> "东北菜"
        CuisineType.FAMILY -> "家常菜"
    }
}

val DEFAULT_DISH = Dish(
    dishId = "DEFAULT_DISH",
    name = "番茄炒蛋",
    taste = "酸甜",
    type = DishType.WARM_VEGGIE,
    ingredients = "番茄、鸡蛋、盐、糖",
    recipe = "1. 炒蛋\n2. 炒番茄\n3. 混合出锅",
    comments = "这是一个默认菜品模板。"
)