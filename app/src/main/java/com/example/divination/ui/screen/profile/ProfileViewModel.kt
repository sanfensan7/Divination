package com.example.divination.ui.screen.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divination.model.DivinationResult
import com.example.divination.model.MBTIResult
import com.example.divination.utils.LocalStorageService
import com.example.divination.utils.MBTIStorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 个人页面 ViewModel
 * 
 * 管理历史记录列表和 MBTI 信息的状态
 */
class ProfileViewModel(
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val mbtiStorageService = MBTIStorageService.getInstance(context)
    
    init {
        loadData()
    }
    
    /**
     * 加载历史记录和 MBTI 信息
     */
    fun loadData() {
        viewModelScope.launch {
            try {
                if (_uiState.value !is ProfileUiState.Success) {
                    _uiState.value = ProfileUiState.Loading
                }
                
                // 加载历史记录
                val historyRecords = LocalStorageService.getAllResults(context)
                
                // 加载 MBTI 信息
                val mbtiResult = mbtiStorageService.getLatestResult()
                val mbtiTestCount = mbtiStorageService.getResultCount()
                
                _uiState.value = ProfileUiState.Success(
                    historyRecords = historyRecords,
                    mbtiResult = mbtiResult,
                    mbtiTestCount = mbtiTestCount
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "加载失败")
            }
        }
    }
    
    /**
     * 删除历史记录
     */
    fun deleteHistoryRecord(resultId: String) {
        viewModelScope.launch {
            try {
                val success = LocalStorageService.deleteResult(context, resultId)
                if (success) {
                    val currentState = _uiState.value
                    if (currentState is ProfileUiState.Success) {
                        val updatedHistory = currentState.historyRecords.filterNot { it.id == resultId }
                        _uiState.value = currentState.copy(historyRecords = updatedHistory)
                    } else {
                        loadData()
                    }
                } else {
                    _uiState.value = ProfileUiState.Error("删除失败")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "删除失败")
            }
        }
    }
    
    /**
     * 清空所有历史记录
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                // 显示加载状态
                _uiState.value = ProfileUiState.Loading
                
                val currentState = _uiState.value
                if (currentState is ProfileUiState.Success) {
                    // 基于当前状态中的历史记录逐条删除，避免重复读取存储层
                    currentState.historyRecords.forEach { result ->
                        LocalStorageService.deleteResult(context, result.id)
                    }
                } else {
                    // 非 Success 状态下仍然保证不会崩溃，但不强制删除
                    val allResults = LocalStorageService.getAllResults(context)
                    allResults.forEach { result ->
                        LocalStorageService.deleteResult(context, result.id)
                    }
                }
                // 重新加载数据，确保 UI 实时更新
                loadData()
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "清空失败")
            }
        }
    }
}

/**
 * 个人页面 UI 状态
 */
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    
    data class Success(
        val historyRecords: List<DivinationResult>,
        val mbtiResult: MBTIResult?,
        val mbtiTestCount: Int
    ) : ProfileUiState()
    
    data class Error(val message: String) : ProfileUiState()
}

/**
 * ProfileViewModel 工厂
 */
class ProfileViewModelFactory(
    private val context: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
