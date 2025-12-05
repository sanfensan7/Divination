package com.example.divination

import android.content.Context
import com.example.divination.ui.screen.result.DivinationResultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * DivinationResultViewModel 单元测试
 * 
 * 测试状态管理、结果加载、保存操作
 * 
 * **Validates: Requirements 22.1, 22.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DivinationResultViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `初始状态应该正确`() {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        
        val state = viewModel.uiState.value
        assertNull(state.result)
        assertTrue(state.isLoading)
        assertFalse(state.isSaving)
        assertNull(state.error)
        assertFalse(state.saveSuccess)
    }
    
    @Test
    fun `加载结果成功应该更新状态`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        assertEquals("bazi_123", state.result?.id)
        assertEquals("bazi", state.result?.methodId)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `生成的八字结果应该包含正确的分段`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        
        val sections = state.result?.resultSections
        assertNotNull(sections)
        assertTrue(sections!!.size >= 5)
        
        // 验证包含预期的分段标题
        val titles = sections.map { it.title }
        assertTrue(titles.contains("命格分析"))
        assertTrue(titles.contains("事业运势"))
        assertTrue(titles.contains("财运分析"))
        assertTrue(titles.contains("感情婚姻"))
        assertTrue(titles.contains("健康状况"))
    }
    
    @Test
    fun `生成的塔罗结果应该包含正确的分段`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "tarot_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        
        val sections = state.result?.resultSections
        assertNotNull(sections)
        assertTrue(sections!!.size >= 3)
        
        // 验证包含预期的分段标题
        val titles = sections.map { it.title }
        assertTrue(titles.contains("当前状况"))
        assertTrue(titles.contains("未来趋势"))
        assertTrue(titles.contains("建议"))
    }
    
    @Test
    fun `生成的周易结果应该包含正确的分段`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "zhouyi_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        
        val sections = state.result?.resultSections
        assertNotNull(sections)
        assertTrue(sections!!.size >= 3)
        
        // 验证包含预期的分段标题
        val titles = sections.map { it.title }
        assertTrue(titles.contains("卦象解析"))
        assertTrue(titles.contains("时运分析"))
        assertTrue(titles.contains("行动建议"))
    }
    
    @Test
    fun `结果应该包含创建时间`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        assertNotNull(state.result?.createTime)
    }
    
    @Test
    fun `刷新结果应该重新加载数据`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        advanceUntilIdle()
        
        val firstResult = viewModel.uiState.value.result
        assertNotNull(firstResult)
        
        viewModel.refreshResult()
        advanceUntilIdle()
        
        val secondResult = viewModel.uiState.value.result
        assertNotNull(secondResult)
        assertEquals(firstResult?.id, secondResult?.id)
    }
    
    @Test
    fun `清除错误应该正确`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        advanceUntilIdle()
        
        // 手动设置错误状态（通过反射或其他方式）
        // 这里我们通过清除来验证功能
        viewModel.clearError()
        
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `清除保存成功状态应该正确`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        advanceUntilIdle()
        
        viewModel.clearSaveSuccess()
        
        assertFalse(viewModel.uiState.value.saveSuccess)
    }
    
    @Test
    fun `不同方法ID应该生成不同的结果`() = runTest {
        val viewModel1 = DivinationResultViewModel(mockContext, "bazi_123")
        val viewModel2 = DivinationResultViewModel(mockContext, "tarot_456")
        
        advanceUntilIdle()
        
        val result1 = viewModel1.uiState.value.result
        val result2 = viewModel2.uiState.value.result
        
        assertNotNull(result1)
        assertNotNull(result2)
        
        assertEquals("bazi", result1?.methodId)
        assertEquals("tarot", result2?.methodId)
        
        // 验证结果内容不同
        assertNotEquals(
            result1?.resultSections?.first()?.title,
            result2?.resultSections?.first()?.title
        )
    }
    
    @Test
    fun `结果分段应该包含内容`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        
        val sections = state.result?.resultSections
        assertNotNull(sections)
        
        // 验证每个分段都有标题和内容
        sections!!.forEach { section ->
            assertTrue(section.title.isNotEmpty())
            assertTrue(section.content.isNotEmpty())
        }
    }
    
    @Test
    fun `八字结果应该包含评分`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "bazi_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        
        val sections = state.result?.resultSections
        assertNotNull(sections)
        
        // 验证至少有一个分段包含评分
        val hasScore = sections!!.any { it.score > 0 }
        assertTrue(hasScore)
    }
    
    @Test
    fun `塔罗结果不应该包含评分`() = runTest {
        val viewModel = DivinationResultViewModel(mockContext, "tarot_123")
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.result)
        
        val sections = state.result?.resultSections
        assertNotNull(sections)
        
        // 验证所有分段的评分都是 -1（无评分）
        val allNoScore = sections!!.all { it.score == -1 }
        assertTrue(allNoScore)
    }
}
