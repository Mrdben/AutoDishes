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
    val ingredients: List<Ingredient>,
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
        ingredients = ingredientsString(dishDto.ingredients),
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
    STAPLE, //主食
    SNACK,
    OTHER
}

fun DishType.getDisplayName(): String {
    return when (this) {
        DishType.COLD_MEAT -> "冷荤"
        DishType.COLD_VEGGIE -> "冷素"
        DishType.WARM_MEAT -> "热荤"
        DishType.WARM_VEGGIE -> "热素"
        DishType.SOUP -> "汤"
        DishType.STAPLE -> "主食"
        DishType.SNACK -> "零食"
        DishType.OTHER -> "其他类别"
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
    FAMILY,      //家常菜
    OTHER
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
        CuisineType.OTHER -> "其他菜系"
    }
}

data class Ingredient(
    val name: String,
    val category: IngredientCategory,
    val amount: Double,
    val unit: String
) {
    override fun toString(): String {
        return "$name(${category.getDisplayName()}) ${formattedAmount()}$unit"
    }
    fun toStringShort(): String {
        return "$name ${formattedAmount()}$unit"
    }
    private fun formattedAmount() : String {
        return if (amount == 0.0) "若干"
               else {if (amount == amount.toLong().toDouble())  amount.toLong().toString()
                     else amount.toString()}
    }
}

enum class IngredientCategory {
    CROP,
    MEAT,
    VEGETABLE,
    FRUIT,
    SEASONING,
    OTHER
}

fun IngredientCategory.getDisplayName(): String {
    return when (this) {
        IngredientCategory.CROP -> "粮食"
        IngredientCategory.MEAT -> "肉类"
        IngredientCategory.VEGETABLE -> "蔬菜"
        IngredientCategory.FRUIT -> "水果"
        IngredientCategory.SEASONING -> "佐料"
        IngredientCategory.OTHER -> "其他"
    }
}
private fun fromDisplayName(name: String): IngredientCategory {
    return IngredientCategory.entries.find { it.getDisplayName() == name } ?: IngredientCategory.OTHER
}

fun ingredientsString(ingList: List<Ingredient>, newLine: Boolean = false, showCategory: Boolean = true): String {
    return ingList.joinToString(separator = "、${if (newLine) "\n" else ""}") {
        if (showCategory) it.toString() else it.toStringShort() }
}

fun ingredientsString(rawString: String): List<Ingredient> {
    val items = rawString.split("、", "，", "；", ",", ";","\n")
                         .map { it.trim() }
                         .filter { it.isNotBlank() }
    val regex = Regex("""([^\s（(]+)(?:\s*[（(]([^）)]*)[）)])?(?:\s*(\d+(?:\.\d+)?))?(?:\s*([a-zA-Z\u4e00-\u9fa5]+))?""")
    val ingList = mutableListOf<Ingredient>()
    for (item in items) {
        val match = regex.matchEntire(item)
        if (match != null) {
            val (name, category, amount, unit) = match.destructured
            val amountNum = amount.toDoubleOrNull()
            ingList.add(Ingredient(
                name = name,
                category = fromDisplayName(category),
                amount = amountNum ?: 0.0,
                unit = amountNum?.let { unit } ?: ""
            ))
        } else continue
    }
    return ingList
}

val DEFAULT_DISH = Dish(
    dishId = "DEFAULT_DISH",
    name = "默认菜品",
    taste = "酸甜",
    type = DishType.WARM_VEGGIE,
    ingredients = listOf(
        Ingredient(
            name = "鸡蛋",
            amount = 2.0,
            unit = "个",
            category = IngredientCategory.MEAT
        ),
        Ingredient(
            name = "西红柿",
            amount = 200.0,
            unit = "g",
            category = IngredientCategory.VEGETABLE
        ),
        Ingredient(
            name = "盐",
            amount = 3.0,
            unit = "g",
            category = IngredientCategory.SEASONING
        )
    ),
    recipe = "1. 炒蛋\n2. 炒番茄\n3. 混合出锅",
    comments = "这是一个默认菜品模板。\n可能是出错了。"
)