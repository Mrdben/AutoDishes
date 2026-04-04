package com.mrdben.autodishes.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class FamilyViewModel : ViewModel() {
    private val repository = FamilyRepository.instance
    //维护当前排序类型（默认按姓名）
    private var currentSortType: SortType = SortType.NAME
    private var currentSortOrder: SortOrder = SortOrder.ASC
    val currentSortState = MutableStateFlow(
        Pair(currentSortType, currentSortOrder)
    )
    private val _currentFamily = MutableStateFlow(repository.currentFamily.value)
    val currentFamily: StateFlow<Family> = _currentFamily.asStateFlow()

    init {
        // 3. 监听 Repository 的原始数据变化（名称/老人列表变化）
        viewModelScope.launch {
            repository.currentFamily.collect { originalFamily ->
                updateSortedFamily(originalFamily)
            }
        }
    }

    private fun updateSortedFamily(originalFamily: Family) {
        val comparator = currentSortType.getComparator()
        val sortedElders = if (currentSortOrder == SortOrder.ASC) {
            originalFamily.elders.sortedWith(comparator)
        } else {
            originalFamily.elders.sortedWith(comparator.reversed()) // 逆序
        }
        _currentFamily.value = originalFamily.copy(elders = sortedElders)
    }
    fun toggleSortType(sortType: SortType) {
        if (currentSortType == sortType) {
            // 点击当前激活的排序→切换顺序
            currentSortOrder = currentSortOrder.toggle()
        } else {
            // 点击新的排序→重置为升序
            currentSortType = sortType
            currentSortOrder = SortOrder.ASC
        }
        // 更新排序状态+重新排序
        currentSortState.value = Pair(currentSortType, currentSortOrder)
        updateSortedFamily(repository.currentFamily.value)
    }
    // 更新家庭名称的方法
    fun updateFamilyName(newName: String) {
        // 1. 获取当前家庭数据
        val current = currentFamily.value
        // 2. 用 copy 生成新对象（data class 特性）
        val updatedFamily = current.copy(familyName = newName)
        // 3. 通知 Repository 更新数据（Repository 内部更新 StateFlow）
        repository.updateFamily(updatedFamily)
    }

    // 页面调用：添加老人
    fun addElder(elder: Elder) {
        repository.addElder(elder)
    }

    // 页面调用：修改老人
    fun updateElder(elder: Elder) {
        repository.updateElder(elder)
    }

    // 页面调用：删除老人
    fun removeElder(elderId: String) {
        repository.removeElder(elderId)
    }
}