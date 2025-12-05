package com.example.divination

import com.example.divination.ui.screen.detail.DivinationDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * DivinationDetailViewModel 单元测试
 * 
 * 测试状态管理、详情加载、请求提交逻辑
 * 
 * **Validates: Requirements 22.1, 22.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DivinationDetailViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `初始状态应该正确`() {
        val viewModel = DivinationDetailViewModel("bazi")
        
        val state = viewModel.uiState.value
        assertNull(state.method)
        assertTrue(state.inputValues.isEmpty())
        assertTrue(state.isLoading)
        assertFalse(state.isSubmitting)
        assertNull(state.error)
        assertTrue(state.validationErrors.isEmpty())
    }
    
    @Test
    fun `加载方法详情成功`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.method)
        assertEquals("bazi", state.method?.id)
        assertEquals("八字命理", state.method?.name)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `加载不存在的方法应该显示错误`() = runTest {
        val viewModel = DivinationDetailViewModel("nonexistent")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNull(state.method)
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("未找到该算命方法", state.error)
    }
    
    @Test
    fun `更新输入值应该正确`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        advanceUntilIdle()
        
        viewModel.updateInputValue("birthDate", "2000-01-01")
        
        val state = viewModel.uiState.value
        assertEquals("2000-01-01", state.inputValues["birthDate"])
    }
    
    @Test
    fun `更新多个输入值应该正确`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        advanceUntilIdle()
        
        viewModel.updateInputValue("birthDate", "2000-01-01")
        viewModel.updateInputValue("birthTime", "12:00")
        viewModel.updateInputValue("gender", "男")
        
        val state = viewModel.uiState.value
        assertEquals("2000-01-01", state.inputValues["birthDate"])
        assertEquals("12:00", state.inputValues["birthTime"])
        assertEquals("男", state.inputValues["gender"])
    }
    
    @Test
    fun `提交时缺少必填字段应该显示验证错误`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        advanceUntilIdle()
        
        var successCalled = false
        viewModel.submitDivination { successCalled = true }
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(successCalled)
        assertTrue(state.validationErrors.isNotEmpty())
        assertFalse(state.isSubmitting)
    }
    
    @Test
    fun `提交完整数据应该成功`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        advanceUntilIdle()
        
        viewModel.updateInputValue("birthDate", "2000-01-01")
        viewModel.updateInputValue("birthTime", "12:00")
        viewModel.updateInputValue("gender", "男")
        
        var resultId: String? = null
        viewModel.submitDivination { resultId = it }
        advanceUntilIdle()
        
        assertNotNull(resultId)
        assertTrue(resultId!!.startsWith("bazi_"))
        
        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNull(state.error)
    }
    
    @Test
    fun `提交时应该显示加载状态`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        advanceUntilIdle()
        
        viewModel.updateInputValue("birthDate", "2000-01-01")
        viewModel.updateInputValue("birthTime", "12:00")
        viewModel.updateInputValue("gender", "男")
        
        viewModel.submitDivination { }
        
        // 在提交过程中检查状态
        val stateBeforeCompletion = viewModel.uiState.value
        assertTrue(stateBeforeCompletion.isSubmitting)
        
        advanceUntilIdle()
        
        val stateAfterCompletion = viewModel.uiState.value
        assertFalse(stateAfterCompletion.isSubmitting)
    }
    
    @Test
    fun `清除错误应该正确`() = runTest {
        val viewModel = DivinationDetailViewModel("nonexistent")
        advanceUntilIdle()
        
        assertNotNull(viewModel.uiState.value.error)
        
        viewModel.clearError()
        
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `刷新详情应该重新加载数据`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        advanceUntilIdle()
        
        val firstMethod = viewModel.uiState.value.method
        assertNotNull(firstMethod)
        
        viewModel.refreshDetails()
        advanceUntilIdle()
        
        val secondMethod = viewModel.uiState.value.method
        assertNotNull(secondMethod)
        assertEquals(firstMethod?.id, secondMethod?.id)
    }
    
    @Test
    fun `更新输入值应该清除对应字段的验证错误`() = runTest {
        val viewModel = DivinationDetailViewModel("bazi")
        advanceUntilIdle()
        
        // 先触发验证错误
        viewModel.submitDivination { }
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.validationErrors.containsKey("birthDate"))
        
        // 更新字段值
        viewModel.updateInputValue("birthDate", "2000-01-01")
        
        // 验证错误应该被清除
        assertFalse(viewModel.uiState.value.validationErrors.containsKey("birthDate"))
    }
    
    @Test
    fun `加载无输入字段的方法应该成功`() = runTest {
        val viewModel = DivinationDetailViewModel("mbti")
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.method)
        assertEquals("mbti", state.method?.id)
        assertTrue(state.method?.inputFields?.isEmpty() == true)
        assertFalse(state.isLoading)
    }
}
