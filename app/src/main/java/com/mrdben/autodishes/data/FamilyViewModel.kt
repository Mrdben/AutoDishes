package com.mrdben.autodishes.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FamilyViewModel : ViewModel() {
    private val repository = FamilyRepository.instance

    // 暴露给UI的家庭数据（可观察）
    val currentFamily: StateFlow<Family> = repository.currentFamily

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
        viewModelScope.launch {
            repository.addElder(elder)
        }
    }

    // 页面调用：修改老人
    fun updateElder(elder: Elder) {
        viewModelScope.launch {
            repository.updateElder(elder)
        }
    }

    // 页面调用：删除老人
    fun removeElder(elderId: String) {
        viewModelScope.launch {
            repository.removeElder(elderId)
        }
    }
}