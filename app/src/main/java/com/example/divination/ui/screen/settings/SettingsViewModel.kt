package com.example.divination.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 设置页面 UI 状态
 */
data class SettingsUiState(
    val todayUsageCount: Int = 0,
    val totalUsageCount: Int = 0,
    val appVersion: String = "1.1.4",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 设置页面 ViewModel
 * 
 * 管理设置页面的状态和业务逻辑
 */
class SettingsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadUsageStatistics()
    }
    
    /**
     * 加载使用统计数据
     */
    private fun loadUsageStatistics() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // TODO: 从 LocalStorageService 加载实际数据
                // 目前使用模拟数据
                val todayCount = 5
                val totalCount = 42
                
                _uiState.value = _uiState.value.copy(
                    todayUsageCount = todayCount,
                    totalUsageCount = totalCount,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
    
    /**
     * 刷新使用统计
     */
    fun refreshStatistics() {
        loadUsageStatistics()
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
