package com.example.divination

import com.example.divination.ui.screen.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar
import java.util.Date

/**
 * HomeViewModel 单元测试
 * 
 * 测试状态管理、老黄历加载和日期选择逻辑
 * 
 * **Validates: Requirements 22.1, 22.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel()
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
        assertNotNull(state.selectedDate)
        assertFalse(state.showDatePicker)
        assertNull(state.error)
    }
    
    /**
     * 测试老黄历数据加载
     */
    @Test
    fun `loadAlmanacData should update state with almanac`() = runTest {
        // 等待初始加载完成
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.almanac)
        assertNull(state.error)
    }
    
    /**
     * 测试老黄历数据包含必要字段
     */
    @Test
    fun `almanac should contain required fields`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val almanac = viewModel.uiState.value.almanac
        assertNotNull(almanac)
        almanac?.let {
            assertNotNull(it.date)
            assertNotNull(it.lunarDate)
            assertNotNull(it.chineseZodiac)
            assertNotNull(it.goodActivities)
            assertNotNull(it.badActivities)
            assertNotNull(it.fiveElements)
        }
    }
    
    /**
     * 测试日期选择
     */
    @Test
    fun `selectDate should update selectedDate and reload almanac`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val newDate = calendar.time
        
        viewModel.selectDate(newDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(newDate, state.selectedDate)
        assertFalse(state.showDatePicker)
        assertNotNull(state.almanac)
    }
    
    /**
     * 测试显示日期选择器
     */
    @Test
    fun `showDatePicker should set showDatePicker to true`() {
        viewModel.showDatePicker()
        
        val state = viewModel.uiState.value
        assertTrue(state.showDatePicker)
    }
    
    /**
     * 测试隐藏日期选择器
     */
    @Test
    fun `hideDatePicker should set showDatePicker to false`() {
        viewModel.showDatePicker()
        viewModel.hideDatePicker()
        
        val state = viewModel.uiState.value
        assertFalse(state.showDatePicker)
    }
    
    /**
     * 测试刷新老黄历
     */
    @Test
    fun `refreshAlmanac should reload data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.refreshAlmanac()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.almanac)
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
     * 测试选择日期后日期选择器自动关闭
     */
    @Test
    fun `selectDate should close date picker`() = runTest {
        viewModel.showDatePicker()
        assertTrue(viewModel.uiState.value.showDatePicker)
        
        viewModel.selectDate(Date())
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.showDatePicker)
    }
}
