package com.example.divination

import com.example.divination.ui.screen.methods.MethodCategory
import com.example.divination.ui.screen.methods.MethodsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * MethodsViewModel 单元测试
 * 
 * 测试状态管理、方法加载和分类筛选逻辑
 * 
 * **Validates: Requirements 22.1, 22.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MethodsViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MethodsViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MethodsViewModel()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    /**
     * 测试初始状态
     */
    @Test
    fun `initial state should have default values`() {
        val state = viewModel.uiState.value
        assertEquals(MethodCategory.ALL, state.currentCategory)
        assertNull(state.error)
    }
    
    /**
     * 测试加载算命方法
     */
    @Test
    fun `loadMethods should update state with methods`() = runTest {
        // 等待初始加载完成
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.methods.isNotEmpty())
        assertTrue(state.filteredMethods.isNotEmpty())
        assertNull(state.error)
    }
    
    /**
     * 测试方法列表不为空
     */
    @Test
    fun `methods list should not be empty after loading`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val methods = viewModel.uiState.value.methods
        assertTrue(methods.isNotEmpty())
    }
    
    /**
     * 测试选择全部分类
     */
    @Test
    fun `selectCategory ALL should show all methods`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val allMethodsCount = viewModel.uiState.value.methods.size
        
        viewModel.selectCategory(MethodCategory.ALL)
        
        val state = viewModel.uiState.value
        assertEquals(MethodCategory.ALL, state.currentCategory)
        assertEquals(allMethodsCount, state.filteredMethods.size)
    }
    
    /**
     * 测试选择中国传统分类
     */
    @Test
    fun `selectCategory CHINESE should filter Chinese methods`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectCategory(MethodCategory.CHINESE)
        
        val state = viewModel.uiState.value
        assertEquals(MethodCategory.CHINESE, state.currentCategory)
        
        // 验证所有筛选的方法都是中国传统类型（type = 1）
        state.filteredMethods.forEach { method ->
            assertEquals(1, method.type)
        }
    }
    
    /**
     * 测试选择西方传统分类
     */
    @Test
    fun `selectCategory WESTERN should filter Western methods`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectCategory(MethodCategory.WESTERN)
        
        val state = viewModel.uiState.value
        assertEquals(MethodCategory.WESTERN, state.currentCategory)
        
        // 验证所有筛选的方法都是西方传统类型（type = 2）
        state.filteredMethods.forEach { method ->
            assertEquals(2, method.type)
        }
    }
    
    /**
     * 测试选择心理测评分类
     */
    @Test
    fun `selectCategory PSYCHOLOGICAL should filter Psychological methods`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectCategory(MethodCategory.PSYCHOLOGICAL)
        
        val state = viewModel.uiState.value
        assertEquals(MethodCategory.PSYCHOLOGICAL, state.currentCategory)
        
        // 验证所有筛选的方法都是心理测评类型（type = 3）
        state.filteredMethods.forEach { method ->
            assertEquals(3, method.type)
        }
    }
    
    /**
     * 测试分类切换
     */
    @Test
    fun `switching categories should update filtered methods`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // 切换到中国传统
        viewModel.selectCategory(MethodCategory.CHINESE)
        val chineseCount = viewModel.uiState.value.filteredMethods.size
        
        // 切换到全部
        viewModel.selectCategory(MethodCategory.ALL)
        val allCount = viewModel.uiState.value.filteredMethods.size
        
        // 验证数量不同
        assertTrue(allCount >= chineseCount)
    }
    
    /**
     * 测试刷新方法列表
     */
    @Test
    fun `refreshMethods should reload data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.refreshMethods()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.methods.isNotEmpty())
    }
    
    /**
     * 测试清除错误
     */
    @Test
    fun `clearError should remove error message`() {
        viewModel.clearError()
        
        val state = viewModel.uiState.value
        assertNull(state.error)
    }
    
    /**
     * 测试初始分类为全部
     */
    @Test
    fun `initial category should be ALL`() {
        val state = viewModel.uiState.value
        assertEquals(MethodCategory.ALL, state.currentCategory)
    }
}
