package com.example.divination.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divination.model.ChineseAlmanac
import com.example.divination.utils.ChineseAlmanacService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 首页 UI 状态
 */
data class HomeUiState(
    val almanac: ChineseAlmanac? = null,
    val selectedDate: Date = Date(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false
)

/**
 * 首页 ViewModel
 * 
 * 管理首页的状态和业务逻辑，包括老黄历数据加载和日期选择
 * 
 * **Validates: Requirements 22.1, 22.2, 22.3**
 */
class HomeViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadAlmanacData()
    }
    
    /**
     * 加载老黄历数据
     */
    private fun loadAlmanacData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentDate = _uiState.value.selectedDate
                val almanac = ChineseAlmanacService.getAlmanacForDate(currentDate)
                
                _uiState.value = _uiState.value.copy(
                    almanac = almanac,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载老黄历失败"
                )
            }
        }
    }
    
    /**
     * 选择日期
     */
    fun selectDate(date: Date) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            showDatePicker = false
        )
        loadAlmanacData()
    }
    
    /**
     * 显示日期选择器
     */
    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }
    
    /**
     * 隐藏日期选择器
     */
    fun hideDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }
    
    /**
     * 刷新老黄历数据
     */
    fun refreshAlmanac() {
        loadAlmanacData()
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
