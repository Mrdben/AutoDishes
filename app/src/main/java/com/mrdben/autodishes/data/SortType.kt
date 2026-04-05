package com.mrdben.autodishes.data

// 排序类型枚举
enum class SortType {
    NAME, // 按姓名
    AGE,  // 按年龄
    GENDER; // 按性别

    // 排序逻辑：根据枚举类型返回对应的比较器
    fun getComparator(): Comparator<Elder> {
        return when (this) {
            NAME -> Comparator { e1, e2 -> e1.name.compareTo(e2.name) } // 姓名升序
            AGE -> Comparator { e1, e2 -> e1.age.compareTo(e2.age) }     // 年龄升序
            GENDER -> Comparator { e1, e2 ->
                // 性别排序：男→女→未知
                when {
                    e1.gender == e2.gender -> 0
                    e1.gender == com.mrdben.autodishes.data.Gender.MALE -> -1
                    e1.gender == com.mrdben.autodishes.data.Gender.FEMALE ->
                        if (e2.gender == com.mrdben.autodishes.data.Gender.MALE) 1 else -1
                    else -> 1
                }
            }
        }
    }
}
// 排序顺序枚举
enum class SortOrder {
    ASC,  // 升序
    DESC; // 降序

    // 切换排序顺序（升→降，降→升）
    fun toggle(): SortOrder {
        return if (this == ASC) DESC else ASC
    }
}

enum class DishSortType {
    NAME, // 按姓名
    TASTE,  // 按口味
    TYPE,    //按类型
    CUISINE,   //按菜系
    FAVORITE; // 按喜好

    // 排序逻辑：根据枚举类型返回对应的比较器
    fun getComparator(): Comparator<Dish> {
        return when (this) {
            NAME -> Comparator { e1, e2 -> e1.name.compareTo(e2.name) } // 姓名升序
            TASTE -> Comparator { e1, e2 -> e1.taste.compareTo(e2.taste) }     // 年龄升序
            TYPE -> Comparator { e1, e2 -> e1.type.compareTo(e2.type) }     // 年龄升序
            CUISINE -> Comparator { e1, e2 -> e1.cuisine.compareTo(e2.cuisine) }     // 年龄升序
            FAVORITE -> Comparator { e1, e2 -> e1.favoriteLevel.compareTo(e2.favoriteLevel) }     // 年龄升序
        }
    }
}