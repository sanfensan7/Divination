package com.example.divination.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.divination.utils.LocalStorageService
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
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
    val appVersion: String = "1.0.2",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 设置页面 ViewModel
 *
 * 管理设置页面的状态和业务逻辑
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadUsageStatistics()
    }
    
    /**
     * 加载使用统计数据
     */
    private fun loadUsageStatistics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val context = getApplication<Application>().applicationContext
                val results = LocalStorageService.getAllResults(context)
                val totalCount = results.size
                
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis
                val todayCount = results.count { it.createTime.time >= startOfDay }
                
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
