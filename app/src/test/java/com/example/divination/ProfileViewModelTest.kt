package com.example.divination

import android.content.Context
import com.example.divination.model.DivinationResult
import com.example.divination.model.MBTIResult
import com.example.divination.model.ResultSection
import com.example.divination.ui.screen.profile.ProfileUiState
import com.example.divination.ui.screen.profile.ProfileViewModel
import com.example.divination.utils.LocalStorageService
import com.example.divination.utils.MBTIStorageService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * ProfileViewModel 单元测试
 * 
 * 测试状态管理、历史记录加载、删除操作
 * _需求: 22.1, 22.5_
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    
    private lateinit var context: Context
    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        
        // Mock MBTIStorageService
        mockkObject(MBTIStorageService.Companion)
        val mbtiService = mockk<MBTIStorageService>(relaxed = true)
        every { MBTIStorageService.getInstance(any()) } returns mbtiService
        every { mbtiService.getLatestResult() } returns null
        every { mbtiService.getResultCount() } returns 0
        
        // Mock LocalStorageService
        mockkObject(LocalStorageService)
        every { LocalStorageService.getAllResults(any()) } returns emptyList()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun `初始状态应该是 Loading`() {
        viewModel = ProfileViewModel(context)
        
        val initialState = viewModel.uiState.value
        assertTrue(initialState is ProfileUiState.Loading)
    }
    
    @Test
    fun `加载数据成功应该更新状态为 Success`() = runTest {
        // 准备测试数据
        val testResults = listOf(
            createTestResult("1", "zhouyi"),
            createTestResult("2", "tarot")
        )
        every { LocalStorageService.getAllResults(any()) } returns testResults
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals(2, (state as ProfileUiState.Success).historyRecords.size)
    }
    
    @Test
    fun `加载数据时有 MBTI 结果应该包含在状态中`() = runTest {
        // 准备测试数据
        val testMBTI = createTestMBTIResult()
        val mbtiService = mockk<MBTIStorageService>(relaxed = true)
        every { MBTIStorageService.getInstance(any()) } returns mbtiService
        every { mbtiService.getLatestResult() } returns testMBTI
        every { mbtiService.getResultCount() } returns 3
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        val successState = state as ProfileUiState.Success
        assertNotNull(successState.mbtiResult)
        assertEquals("INTJ", successState.mbtiResult?.personalityType)
        assertEquals(3, successState.mbtiTestCount)
    }
    
    @Test
    fun `删除历史记录成功应该重新加载数据`() = runTest {
        // 初始数据
        val initialResults = listOf(
            createTestResult("1", "zhouyi"),
            createTestResult("2", "tarot")
        )
        every { LocalStorageService.getAllResults(any()) } returns initialResults
        every { LocalStorageService.deleteResult(any(), "1") } returns true
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        // 删除后的数据
        val updatedResults = listOf(createTestResult("2", "tarot"))
        every { LocalStorageService.getAllResults(any()) } returns updatedResults
        
        viewModel.deleteHistoryRecord("1")
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals(1, (state as ProfileUiState.Success).historyRecords.size)
        verify { LocalStorageService.deleteResult(any(), "1") }
    }
    
    @Test
    fun `删除历史记录失败应该更新状态为 Error`() = runTest {
        every { LocalStorageService.getAllResults(any()) } returns emptyList()
        every { LocalStorageService.deleteResult(any(), "1") } returns false
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        viewModel.deleteHistoryRecord("1")
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertEquals("删除失败", (state as ProfileUiState.Error).message)
    }
    
    @Test
    fun `清空所有历史记录应该删除所有记录`() = runTest {
        val initialResults = listOf(
            createTestResult("1", "zhouyi"),
            createTestResult("2", "tarot"),
            createTestResult("3", "bazi")
        )
        every { LocalStorageService.getAllResults(any()) } returns initialResults
        every { LocalStorageService.deleteResult(any(), any()) } returns true
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        // 清空后
        every { LocalStorageService.getAllResults(any()) } returns emptyList()
        
        viewModel.clearAllHistory()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals(0, (state as ProfileUiState.Success).historyRecords.size)
        verify(exactly = 3) { LocalStorageService.deleteResult(any(), any()) }
    }
    
    @Test
    fun `加载数据异常应该更新状态为 Error`() = runTest {
        every { LocalStorageService.getAllResults(any()) } throws RuntimeException("测试异常")
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertTrue((state as ProfileUiState.Error).message.contains("测试异常"))
    }
    
    @Test
    fun `空历史记录应该返回空列表`() = runTest {
        every { LocalStorageService.getAllResults(any()) } returns emptyList()
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals(0, (state as ProfileUiState.Success).historyRecords.size)
    }
    
    @Test
    fun `重新加载数据应该更新状态`() = runTest {
        every { LocalStorageService.getAllResults(any()) } returns emptyList()
        
        viewModel = ProfileViewModel(context)
        advanceUntilIdle()
        
        // 更新数据
        val newResults = listOf(createTestResult("1", "zhouyi"))
        every { LocalStorageService.getAllResults(any()) } returns newResults
        
        viewModel.loadData()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals(1, (state as ProfileUiState.Success).historyRecords.size)
    }
    
    // 辅助方法
    
    private fun createTestResult(id: String, methodId: String): DivinationResult {
        return DivinationResult(
            id = id,
            methodId = methodId,
            createTime = Date(),
            inputData = mapOf("test" to "data"),
            resultSections = listOf(
                ResultSection(
                    title = "测试标题",
                    content = "测试内容"
                )
            )
        )
    }
    
    private fun createTestMBTIResult(): MBTIResult {
        return MBTIResult(
            personalityType = "INTJ",
            eiScore = 10,
            snScore = 15,
            tfScore = -5,
            jpScore = -10,
            testDate = System.currentTimeMillis()
        )
    }
}
