package com.example.divination.ui.screen.methods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divination.model.DivinationMethod
import com.example.divination.utils.DivinationMethodProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 算命方式分类
 */
enum class MethodCategory(val displayName: String) {
    ALL("全部"),
    CHINESE("中国传统"),
    WESTERN("西方传统"),
    PSYCHOLOGICAL("心理测评")
}

/**
 * 算命方式页面 UI 状态
 */
data class MethodsUiState(
    val methods: List<DivinationMethod> = emptyList(),
    val filteredMethods: List<DivinationMethod> = emptyList(),
    val currentCategory: MethodCategory = MethodCategory.ALL,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 算命方式页面 ViewModel
 * 
 * 管理算命方式列表和分类筛选逻辑
 * 
 * **Validates: Requirements 22.1, 22.2, 22.3**
 */
class MethodsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(MethodsUiState())
    val uiState: StateFlow<MethodsUiState> = _uiState.asStateFlow()
    
    init {
        loadMethods()
    }
    
    /**
     * 加载算命方法列表
     */
    private fun loadMethods() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // 从 DivinationMethodProvider 加载所有方法
                val allMethods = DivinationMethodProvider.getAllMethods()
                
                _uiState.value = _uiState.value.copy(
                    methods = allMethods,
                    filteredMethods = allMethods,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载算命方法失败"
                )
            }
        }
    }
    
    /**
     * 选择分类
     */
    fun selectCategory(category: MethodCategory) {
        _uiState.value = _uiState.value.copy(currentCategory = category)
        filterMethods(category)
    }
    
    /**
     * 根据分类筛选方法
     */
    private fun filterMethods(category: MethodCategory) {
        val filtered = when (category) {
            MethodCategory.ALL -> _uiState.value.methods
            MethodCategory.CHINESE -> DivinationMethodProvider.getChineseMethods()
            MethodCategory.WESTERN -> DivinationMethodProvider.getWesternMethods()
            MethodCategory.PSYCHOLOGICAL -> DivinationMethodProvider.getPsychologicalMethods()
        }
        
        _uiState.value = _uiState.value.copy(filteredMethods = filtered)
    }
    
    /**
     * 刷新方法列表
     */
    fun refreshMethods() {
        loadMethods()
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
