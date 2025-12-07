package com.example.divination.ui.screen.result

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import com.example.divination.utils.LocalStorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 算命结果页面 UI 状态
 */
data class DivinationResultUiState(
    val result: DivinationResult? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * 算命结果页面 ViewModel
 * 
 * 管理算命结果数据的状态
 * 
 * **Validates: Requirements 22.1, 22.2, 22.3**
 */
class DivinationResultViewModel(
    private val context: Context,
    private val resultId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DivinationResultUiState())
    val uiState: StateFlow<DivinationResultUiState> = _uiState.asStateFlow()
    private var hasAutoSaved = false
    
    init {
        loadResult()
    }
    
    /**
     * 加载算命结果
     */
    private fun loadResult() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // 尝试从本地存储加载
                val result = LocalStorageService.getResult(context, resultId)
                
                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        result = result,
                        isLoading = false,
                        saveSuccess = true
                    )
                } else {
                    // 如果本地没有，生成模拟结果（实际应用中应该从服务器获取）
                    val mockResult = generateMockResult(resultId)
                    // 自动保存模拟结果
                    LocalStorageService.saveResult(context, mockResult)
                    hasAutoSaved = true
                    
                    _uiState.value = _uiState.value.copy(
                        result = mockResult,
                        isLoading = false,
                        saveSuccess = true  // 自动标记为已保存
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载结果失败"
                )
            }
        }
    }
    
    /**
     * 保存结果到本地
     */
    fun saveResult() {
        viewModelScope.launch {
            try {
                val result = _uiState.value.result ?: return@launch
                
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val success = LocalStorageService.saveResult(context, result)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = success,
                    error = if (!success) "保存失败" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }
    
    /**
     * 刷新结果
     */
    fun refreshResult() {
        loadResult()
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 清除保存成功状态
     */
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
    
    /**
     * 生成模拟结果（用于演示）
     */
    private fun generateMockResult(resultId: String): DivinationResult {
        // 从 resultId 中提取 methodId
        val methodId = resultId.substringBefore("_")
        
        // 根据不同的算命方法生成不同的结果
        val sections = when (methodId) {
            "bazi" -> listOf(
                ResultSection(
                    title = "命格分析",
                    content = "您的八字命格属于偏财格，天生具有经商头脑，善于把握机会。命中带有贵人星，一生多得他人相助。",
                    score = 85
                ),
                ResultSection(
                    title = "事业运势",
                    content = "事业方面，您适合从事金融、贸易、投资等领域。中年后事业运势逐渐上升，有望成就一番事业。",
                    score = 78
                ),
                ResultSection(
                    title = "财运分析",
                    content = "财运较旺，正财偏财皆有。但需注意理财，避免因投资失误而破财。建议多听取专业人士意见。",
                    score = 82
                ),
                ResultSection(
                    title = "感情婚姻",
                    content = "感情运势平稳，适合晚婚。婚后夫妻关系和睦，但需注意沟通，避免因工作忙碌而忽略家庭。",
                    score = 75
                ),
                ResultSection(
                    title = "健康状况",
                    content = "整体健康状况良好，但需注意肠胃和心血管方面的保养。建议保持规律作息，适度运动。",
                    score = 80
                )
            )
            "tarot" -> listOf(
                ResultSection(
                    title = "当前状况",
                    content = "您目前处于一个转折期，过去的努力即将迎来收获。但同时也面临着新的选择和挑战。",
                    score = -1
                ),
                ResultSection(
                    title = "未来趋势",
                    content = "未来三个月内，您将遇到重要的机遇。把握好这个机会，将为您的人生带来积极的改变。",
                    score = -1
                ),
                ResultSection(
                    title = "建议",
                    content = "保持开放的心态，勇于尝试新事物。同时要相信自己的直觉，它会为您指引正确的方向。",
                    score = -1
                )
            )
            "zhouyi" -> listOf(
                ResultSection(
                    title = "卦象解析",
                    content = "您抽到的是乾卦，象征天行健，君子以自强不息。这是一个吉卦，预示着事业顺利，前程似锦。",
                    score = -1
                ),
                ResultSection(
                    title = "时运分析",
                    content = "当前时运亨通，正是大展宏图的好时机。但需谨记，盛极必衰，要保持谦逊谨慎的态度。",
                    score = -1
                ),
                ResultSection(
                    title = "行动建议",
                    content = "宜积极进取，把握机遇。但要注意循序渐进，不可操之过急。同时要注重团队合作，借助他人之力。",
                    score = -1
                )
            )
            else -> listOf(
                ResultSection(
                    title = "综合分析",
                    content = "根据您提供的信息，我们为您进行了详细的分析。整体来看，您的运势较为平稳，有上升的趋势。",
                    score = 75
                ),
                ResultSection(
                    title = "建议",
                    content = "建议您保持积极乐观的心态，勇于面对挑战。同时要注重自我提升，不断学习新知识。",
                    score = -1
                )
            )
        }
        
        return DivinationResult(
            id = resultId,
            methodId = methodId,
            createTime = Date(),
            inputData = emptyMap(),
            resultSections = sections
        )
    }
}

/**
 * DivinationResultViewModel 工厂
 */
class DivinationResultViewModelFactory(
    private val context: Context,
    private val resultId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DivinationResultViewModel::class.java)) {
            return DivinationResultViewModel(context, resultId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
