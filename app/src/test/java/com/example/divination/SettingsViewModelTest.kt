package com.example.divination

import android.app.Application
import com.example.divination.ui.screen.settings.SettingsViewModel
import com.example.divination.utils.LocalStorageService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * SettingsViewModel 单元测试
 * 
 * 测试状态管理和数据加载逻辑
 * 
 * **Validates: Requirements 22.1, 22.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var viewModel: SettingsViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // 提供带文件目录的假 Application，避免文件 IO 依赖
        val tempDir = Files.createTempDirectory("settingsVmTest").toFile()
        application = mockk(relaxed = true)
        every { application.applicationContext } returns application
        every { application.filesDir } returns tempDir

        // 避免触发真实存储，返回空列表
        mockkObject(LocalStorageService)
        every { LocalStorageService.getAllResults(any()) } returns emptyList()

        viewModel = SettingsViewModel(application)
    }
    
    @After
    fun tearDown() {
        unmockkObject(LocalStorageService)
        Dispatchers.resetMain()
    }
    
    /**
     * 测试初始状态
     */
    @Test
    fun `initial state should have default values`() {
        val state = viewModel.uiState.value
        assertEquals("1.0.2", state.appVersion)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    /**
     * 测试加载使用统计
     */
    @Test
    fun `loadUsageStatistics should update state`() = runTest {
        // 等待初始加载完成
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.todayUsageCount >= 0)
        assertTrue(state.totalUsageCount >= 0)
    }
    
    /**
     * 测试刷新统计
     */
    @Test
    fun `refreshStatistics should reload data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.refreshStatistics()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
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
}
