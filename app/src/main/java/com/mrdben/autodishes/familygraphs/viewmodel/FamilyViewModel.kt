package com.mrdben.autodishes.familygraphs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.api.UserProfile

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.mrdben.autodishes.familygraphs.data.Elder
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.ordergraphs.data.NewOrderRepository
import com.mrdben.autodishes.data.SortOrder
import com.mrdben.autodishes.data.SortType
import com.mrdben.autodishes.data.WorkState
import com.mrdben.autodishes.familygraphs.data.ElderList

class FamilyViewModel(
    private val repository : FamilyRepository
): ViewModel() {
    private val newOrderRepository = NewOrderRepository.instance
    private val _workState = MutableStateFlow<WorkState>(WorkState.Idle)
    val workState: StateFlow<WorkState> = _workState.asStateFlow()
    //维护当前排序类型（默认按姓名）
    private var currentSortType: SortType = SortType.NAME
    private var currentSortOrder: SortOrder = SortOrder.ASC
    val currentSortState = MutableStateFlow(
        Pair(currentSortType, currentSortOrder)
    )
    var searchKeyword = MutableStateFlow("")
    val userProfile: StateFlow<UserProfile> = TokenManager.userProfile
    private val _elders = MutableStateFlow<ElderList>(emptyList())
    val elders: StateFlow<ElderList> = _elders.asStateFlow()

    init {
        viewModelScope.launch {
            repository.elders.collect {
                //_elders.value = it
                refreshList()
            }
        }
    }
    private fun refreshList() {
        var elders = repository.elders.value

        if (searchKeyword.value.isNotBlank()) {
            elders = elders.filter {
                it.name.lowercase().contains(searchKeyword.value.lowercase())
            }
        }

        val comparator = currentSortType.getComparator()
        val sortedElders = if (currentSortOrder == SortOrder.ASC) {
            elders.sortedWith(comparator)
        } else {
            elders.sortedWith(comparator.reversed()) // 逆序
        }
        _elders.value = sortedElders
    }

    // ====================== 搜索 ======================
    fun search(keyword: String) {
        searchKeyword.value = keyword
        refreshList()
    }

    // ====================== 清空搜索 ======================
    fun clearSearch() {
        searchKeyword.value = ""
        refreshList()
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
        refreshList()
    }

    // 页面调用：添加老人
    fun addElder(elder: Elder) {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            if (repository.addElder(elder))
                repository.syncElderWithServer()
            _workState.value = WorkState.Idle
        }
    }

    // 页面调用：修改老人
    fun updateElder(elder: Elder) {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            repository.updateElder(elder)
            repository.syncElderWithServer()
            _workState.value = WorkState.Idle
        }
    }

    // 页面调用：删除老人
    fun removeElder(elderId: String) {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            if (repository.removeElder(elderId))
                repository.syncElderWithServer()
            _workState.value = WorkState.Idle
        }
    }

    fun getElderById(elderId: String): Elder? {
        return repository.getElderById(elderId)
    }

    fun getElderSelectionList() : List<String> {
        return newOrderRepository.newOrder.value.elderIds
    }

    fun setElderSelectionList(idList : List<String>) {
        newOrderRepository.setElderIdList(idList)

    }
    fun isElderSelected(elderId: String): Boolean {
        return elderId in getElderSelectionList()
    }
    fun changeElderSelection(elderId: String, isSelected: Boolean = false) {
        val oldList = getElderSelectionList()
        val newList = if (isSelected) oldList - elderId
                      else oldList + elderId
        setElderSelectionList(newList.distinct())
    }
    fun refreshElderList() {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            repository.syncElderWithServer()
            _workState.value = WorkState.Idle
        }
    }
}

