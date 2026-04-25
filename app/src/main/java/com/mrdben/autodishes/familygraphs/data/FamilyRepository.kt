package com.mrdben.autodishes.familygraphs.data

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyRepository (
    private val familyDao : FamilyDao,
    private val elderDao : ElderDao
){
    private val ioScope = CoroutineScope(Dispatchers.IO)

    val currentFamily: Flow<Family> = familyDao.getFamilyFlow()
        .map { it ?: DEFAULT_FAMILY }
    val elders: StateFlow<ElderList> = elderDao.getAllEldersFlow()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )

    init {
        ioScope.launch {
            if (familyDao.getFamilyFirst() == null) {
                familyDao.insertFamily(DEFAULT_FAMILY)
            }
        }
    }

    fun updateFamily(updatedFamily: Family) {
        ioScope.launch {
            familyDao.updateFamily(updatedFamily)
        }
    }

    fun addElder(elder: Elder) {
        ioScope.launch {
            val existingElder = getElderById(elder.elderId)
            if (existingElder == null) {
                elderDao.insertElder(elder)
            }
        }
    }
    fun addElder2Family(elder : Elder) {
        ioScope.launch {
            val family = familyDao.getFamilyFirst() ?: return@launch
            if (!family.elderIds.contains(elder.elderId)) {
                val newIds = family.elderIds + elder.elderId
                familyDao.updateFamily(family.copy(elderIds = newIds))
            }
        }
    }

    // 删：删除老人
    fun removeElder(elderId: String) {
        ioScope.launch {
            elderDao.deleteElderById(elderId)
        }
     }

    fun removeElderFromFamily(elderId: String) {
        ioScope.launch {
            val family = familyDao.getFamilyFirst() ?: return@launch
            val newIds = family.elderIds.filter { it != elderId }
            familyDao.updateFamily(family.copy(elderIds = newIds))
        }
    }
    // 改：修改老人信息
    fun updateElder(updatedElder: Elder) {
        ioScope.launch {
            elderDao.updateElder(updatedElder)
        }
    }

    // 查：获取单个老人
    fun getElderById(elderId: String) : Elder? {
            return elders.value.find {it.elderId == elderId}
    }

//    private fun createTestElderData(): ElderList {
//        return listOf(
//            // 假老人1：爷爷
//            Elder(
//                elderId = "debug_elder_001",
//                name = "张爷爷",
//                gender = Gender.MALE,
//                age = 78,
//                allergens = listOf("花生", "海鲜"),
//                diseases = listOf("高血压", "糖尿病"),
//                dietaryRestrictions = listOf("辛辣", "高盐"),
//                tastePreferences = listOf("清淡", "偏甜")
//            ),
//            // 假老人2：奶奶
//            Elder(
//                elderId = "debug_elder_002",
//                name = "李奶奶",
//                gender = Gender.FEMALE,
//                age = 80,
//                allergens = listOf("芒果"),
//                diseases = listOf("冠心病"),
//                dietaryRestrictions = listOf("生冷", "油腻"),
//                tastePreferences = listOf("软烂", "偏咸")
//            ),
//            // 可按需添加更多假老人
//            Elder(
//                elderId = "debug_elder_003",
//                name = "老王",
//                gender = Gender.MALE,
//                age = 75,
//                allergens = emptyList(),
//                diseases = listOf("关节炎"),
//                dietaryRestrictions = listOf("硬食"),
//                tastePreferences = listOf("偏淡", "易消化")
//            ),
//            // 可按需添加更多假老人
//            Elder(
//                elderId = "debug_elder_004",
//                name = "刘姥姥",
//                gender = Gender.FEMALE,
//                age = 5,
//                allergens = listOf("牛奶", "坚果", "花生/坚果", "象拔蚌"),
//                diseases = listOf("高血压", "油腻"),
//                dietaryRestrictions = listOf("猫粮"),
//                tastePreferences = listOf("重口味", "大肥肉", "烧烤", "火锅", "麻辣烫")
//            )
//        )
//    }
//    private fun createTestFamilyData(): Family {
//        return Family(
//            familyId = "debug_001",
//            familyName = "我的家庭",
//            elderIds = listOf("debug_elder_001","debug_elder_002","debug_elder_003","debug_elder_004")
//        )
//    }
}
