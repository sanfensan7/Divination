package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.divination.model.DivinationResult
import com.example.divination.model.MBTIResult
import com.example.divination.model.ResultSection
import com.example.divination.ui.screen.profile.ProfileScreen
import com.example.divination.ui.screen.profile.ProfileViewModel
import com.example.divination.ui.theme.IOSTheme
import com.example.divination.utils.LocalStorageService
import com.example.divination.utils.MBTIStorageService
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * ProfileScreen 集成测试
 * 
 * 测试完整的用户流程（查看历史、删除记录、查看详情）
 * _需求: 17.1, 17.2, 17.3, 17.4_
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenIntegrationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var viewModel: ProfileViewModel
    
    @Before
    fun setup() {
        // 清空测试数据
        clearTestData()
        
        viewModel = ProfileViewModel(context)
    }
    
    @After
    fun tearDown() {
        // 清空测试数据
        clearTestData()
    }
    
    @Test
    fun integrationTest_loadAndDisplayHistory() {
        // 准备测试数据
        val testResult = createTestResult("test1", "zhouyi")
        LocalStorageService.saveResult(context, testResult)
        
        // 重新加载数据
        viewModel.loadData()
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 等待数据加载
        composeTestRule.waitForIdle()
        
        // 验证历史记录显示
        composeTestRule.onNodeWithText("周易占卜").assertIsDisplayed()
    }
    
    @Test
    fun integrationTest_deleteHistoryRecord() {
        // 准备测试数据
        val testResult1 = createTestResult("test1", "zhouyi")
        val testResult2 = createTestResult("test2", "tarot")
        LocalStorageService.saveResult(context, testResult1)
        LocalStorageService.saveResult(context, testResult2)
        
        // 重新加载数据
        viewModel.loadData()
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 等待数据加载
        composeTestRule.waitForIdle()
        
        // 验证两条记录都显示
        composeTestRule.onNodeWithText("周易占卜").assertIsDisplayed()
        composeTestRule.onNodeWithText("塔罗牌").assertIsDisplayed()
        
        // 删除一条记录
        viewModel.deleteHistoryRecord("test1")
        
        // 等待删除完成
        composeTestRule.waitForIdle()
        
        // 验证只剩一条记录
        composeTestRule.onNodeWithText("周易占卜").assertDoesNotExist()
        composeTestRule.onNodeWithText("塔罗牌").assertIsDisplayed()
    }
    
    @Test
    fun integrationTest_clearAllHistory() {
        // 准备测试数据
        val testResult1 = createTestResult("test1", "zhouyi")
        val testResult2 = createTestResult("test2", "tarot")
        LocalStorageService.saveResult(context, testResult1)
        LocalStorageService.saveResult(context, testResult2)
        
        // 重新加载数据
        viewModel.loadData()
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 等待数据加载
        composeTestRule.waitForIdle()
        
        // 点击清空按钮
        composeTestRule.onNodeWithText("清空历史记录").performClick()
        
        // 等待清空完成
        composeTestRule.waitForIdle()
        
        // 验证显示空状态
        composeTestRule.onNodeWithText("暂无历史记录").assertIsDisplayed()
    }
    
    @Test
    fun integrationTest_navigateToResult() {
        // 准备测试数据
        val testResult = createTestResult("test1", "zhouyi")
        LocalStorageService.saveResult(context, testResult)
        
        // 重新加载数据
        viewModel.loadData()
        
        var navigatedResultId: String? = null
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { resultId ->
                        navigatedResultId = resultId
                    }
                )
            }
        }
        
        // 等待数据加载
        composeTestRule.waitForIdle()
        
        // 点击历史记录
        composeTestRule.onNodeWithText("周易占卜").performClick()
        
        // 验证导航被调用
        assert(navigatedResultId == "test1")
    }
    
    @Test
    fun integrationTest_displayMBTIInfo() {
        // 准备 MBTI 测试数据
        val mbtiService = MBTIStorageService.getInstance(context)
        val testMBTI = createTestMBTIResult()
        mbtiService.saveResult(testMBTI)
        
        // 重新加载数据
        viewModel.loadData()
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 等待数据加载
        composeTestRule.waitForIdle()
        
        // 验证 MBTI 信息显示
        composeTestRule.onNodeWithText("INTJ").assertIsDisplayed()
        composeTestRule.onNodeWithText("已测试 1 次").assertIsDisplayed()
    }
    
    @Test
    fun integrationTest_navigateToMBTITest() {
        viewModel.loadData()
        
        var mbtiTestNavigated = false
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToMBTITest = {
                        mbtiTestNavigated = true
                    }
                )
            }
        }
        
        // 等待数据加载
        composeTestRule.waitForIdle()
        
        // 点击 MBTI 卡片
        composeTestRule.onNodeWithText("开始测试").performClick()
        
        // 验证导航被调用
        assert(mbtiTestNavigated)
    }
    
    // 辅助方法
    
    private fun clearTestData() {
        // 清空历史记录
        val allResults = LocalStorageService.getAllResults(context)
        allResults.forEach { result ->
            LocalStorageService.deleteResult(context, result.id)
        }
        
        // 清空 MBTI 数据
        val mbtiService = MBTIStorageService.getInstance(context)
        mbtiService.clearAllResults()
    }
    
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
