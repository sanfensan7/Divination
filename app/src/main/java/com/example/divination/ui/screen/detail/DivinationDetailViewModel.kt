package com.example.divination.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divination.model.DivinationMethod
import com.example.divination.model.InputField
import com.example.divination.utils.DivinationMethodProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 算命详情页面 UI 状态
 */
data class DivinationDetailUiState(
    val method: DivinationMethod? = null,
    val inputValues: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

/**
 * 算命详情页面 ViewModel
 * 
 * 管理算命方法详情和输入参数的状态
 * 
 * **Validates: Requirements 22.1, 22.2, 22.3**
 */
class DivinationDetailViewModel(
    private val methodId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DivinationDetailUiState())
    val uiState: StateFlow<DivinationDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadMethodDetails()
    }
    
    /**
     * 加载算命方法详情
     */
    private fun loadMethodDetails() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val method = DivinationMethodProvider.getMethodById(methodId)
                
                if (method != null) {
                    _uiState.value = _uiState.value.copy(
                        method = method,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "未找到该算命方法"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
    
    /**
     * 更新输入字段的值
     */
    fun updateInputValue(fieldId: String, value: String) {
        val currentValues = _uiState.value.inputValues.toMutableMap()
        currentValues[fieldId] = value
        
        _uiState.value = _uiState.value.copy(
            inputValues = currentValues,
            validationErrors = _uiState.value.validationErrors - fieldId
        )
    }
    
    /**
     * 验证输入参数
     */
    private fun validateInputs(): Boolean {
        val method = _uiState.value.method ?: return false
        val errors = mutableMapOf<String, String>()
        
        method.inputFields.forEach { field ->
            if (field.required) {
                val value = _uiState.value.inputValues[field.id]
                if (value.isNullOrBlank()) {
                    errors[field.id] = "${field.name}不能为空"
                }
            }
        }
        
        _uiState.value = _uiState.value.copy(validationErrors = errors)
        return errors.isEmpty()
    }
    
    /**
     * 提交算命请求
     */
    fun submitDivination(onSuccess: (String) -> Unit) {
        // 先在当前线程进行输入验证
        if (!validateInputs()) {
            return
        }

        // 立即更新为提交中状态，便于测试和 UI 立刻感知
        _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

        viewModelScope.launch {
            try {
                // 模拟提交延迟
                kotlinx.coroutines.delay(1000)

                // 生成结果ID（实际应用中应该调用后端API）
                val resultId = "${methodId}_${System.currentTimeMillis()}"

                _uiState.value = _uiState.value.copy(isSubmitting = false)

                // 调用成功回调
                onSuccess(resultId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "提交失败"
                )
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 刷新方法详情
     */
    fun refreshDetails() {
        loadMethodDetails()
    }
}

/**
 * DivinationDetailViewModel 工厂
 */
class DivinationDetailViewModelFactory(
    private val methodId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DivinationDetailViewModel::class.java)) {
            return DivinationDetailViewModel(methodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
