package com.mrdben.autodishes.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrdben.autodishes.dishgraphs.data.CuisineType
import com.mrdben.autodishes.dishgraphs.data.DishType
import com.mrdben.autodishes.dishgraphs.data.IngredientCategory
import com.mrdben.autodishes.dishgraphs.data.Ingredient
import com.mrdben.autodishes.familygraphs.data.Gender
import com.mrdben.autodishes.ordergraphs.data.MealType


class Converters {
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type

    // 🔥 不可空版本，永远返回 ""，不会为 null
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    // 🔥 不可空版本，永远返回 List，不会为 null
    @TypeConverter
    fun toStringList(json: String): List<String> {
        return try {
            gson.fromJson(json, stringListType)
        } catch (e: Exception) {
            emptyList() // 出错也返回空List，绝不崩
        }
    }

    // Gender 正常转换
    @TypeConverter
    fun fromGender(gender: Gender): String = gender.name

    @TypeConverter
    fun toGender(name: String): Gender = enumValueOf<Gender>(name)

    // DishType
    @TypeConverter
    fun fromDishType(type: DishType): String = type.name

    @TypeConverter
    fun toDishType(value: String): DishType = enumValueOf(value)

    // CuisineType
    @TypeConverter
    fun fromCuisineType(type: CuisineType): String = type.name

    @TypeConverter
    fun toCuisineType(value: String): CuisineType = enumValueOf(value)

    // MealType
    @TypeConverter
    fun fromMealType(type: MealType): String = type.name

    @TypeConverter
    fun toMealType(value: String): MealType = enumValueOf(value)

    @TypeConverter
    fun fromIngredientsList(value: List<Ingredient>): String {
        return Gson().toJson(value) // 将 List 转为 JSON 字符串
    }
    @TypeConverter
    fun toIngredientsList(value: String): List<Ingredient> {
        val listType = object : TypeToken<List<Ingredient>>() {}.type
        return Gson().fromJson(value, listType) // 将 JSON 字符串转回 List
    }
    @TypeConverter
    fun fromIngredientCategory(type: IngredientCategory): String = type.name

    @TypeConverter
    fun toIngredientCategory(value: String): IngredientCategory = enumValueOf(value)
}