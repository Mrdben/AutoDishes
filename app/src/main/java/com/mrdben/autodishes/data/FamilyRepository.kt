package com.mrdben.autodishes.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FamilyRepository()  {
    // 单例初始化
    companion object {
        val instance: FamilyRepository by lazy {
            FamilyRepository()
        }
    }

    // 核心：家庭数据（用StateFlow让数据可观察）
    private val _currentFamily = MutableStateFlow(createTestFamilyData())
    val currentFamily: StateFlow<Family> = _currentFamily.asStateFlow()

    // 🌟 假测试数据生成方法（单独抽离，代码更整洁）
    private fun createTestFamilyData(): Family {
        return Family(
            familyId = "debug_001",
            familyName = "家",
            elders = listOf(
                // 假老人1：爷爷
                Elder(
                    elderId = "debug_elder_001",
                    name = "张爷爷",
                    gender = Gender.MALE,
                    age = 78,
                    allergens = listOf("花生", "海鲜"),
                    diseases = listOf("高血压", "糖尿病"),
                    dietaryRestrictions = listOf("辛辣", "高盐"),
                    tastePreferences = listOf("清淡", "偏甜")
                ),
                // 假老人2：奶奶
                Elder(
                    elderId = "debug_elder_002",
                    name = "李奶奶",
                    gender = Gender.FEMALE,
                    age = 80,
                    allergens = listOf("芒果"),
                    diseases = listOf("冠心病"),
                    dietaryRestrictions = listOf("生冷", "油腻"),
                    tastePreferences = listOf("软烂", "偏咸")
                ),
                // 可按需添加更多假老人
                Elder(
                    elderId = "debug_elder_003",
                    name = "老王",
                    gender = Gender.MALE,
                    age = 75,
                    allergens = emptyList(),
                    diseases = listOf("关节炎"),
                    dietaryRestrictions = listOf("硬食"),
                    tastePreferences = listOf("偏淡", "易消化")
                ),
                // 可按需添加更多假老人
                Elder(
                    elderId = "debug_elder_004",
                    name = "刘姥姥",
                    gender = Gender.FEMALE,
                    age = 5,
                    allergens = listOf("牛奶","坚果","花生/坚果","象拔蚌"),
                    diseases = listOf("高血压","油腻"),
                    dietaryRestrictions = listOf("猫粮"),
                    tastePreferences = listOf("重口味", "大肥肉", "烧烤","火锅","麻辣烫")
            )
            )
        )
    }
    // 修改家庭名称
    fun updateFamily(updatedFamily: Family) {
        _currentFamily.value = updatedFamily
    }
    // 增：添加老人
    fun addElder(elder: Elder) {
        val newElders = _currentFamily.value.elders + elder
        val newFamily = _currentFamily.value.copy(elders = newElders)
        _currentFamily.value = newFamily
        //runBlocking { familyDataStore.saveFamilyData(newFamily) }
    }

    // 删：删除老人
    fun removeElder(elderId: String) {
        val newElders = _currentFamily.value.elders.filter { it.elderId != elderId }
        val newFamily = _currentFamily.value.copy(elders = newElders)
        _currentFamily.value = newFamily
       //runBlocking { familyDataStore.saveFamilyData(newFamily) }
    }

    // 改：修改老人信息
    fun updateElder(updatedElder: Elder) {
        val newElders = _currentFamily.value.elders.map {
            if (it.elderId == updatedElder.elderId) updatedElder else it
        }
        val newFamily = _currentFamily.value.copy(elders = newElders)
        _currentFamily.value = newFamily
        //runBlocking { familyDataStore.saveFamilyData(newFamily) }
    }

    // 查：获取单个老人
    fun getElderById(elderId: String): Elder? {
        return _currentFamily.value.elders.find { it.elderId == elderId }
    }
}