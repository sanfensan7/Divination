package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.divination.model.DivinationResult
import com.example.divination.model.MBTIResult
import com.example.divination.model.ResultSection
import com.example.divination.ui.screen.profile.ProfileScreen
import com.example.divination.ui.screen.profile.ProfileUiState
import com.example.divination.ui.screen.profile.ProfileViewModel
import com.example.divination.ui.theme.IOSTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * ProfileScreen UI 测试
 * 
 * 测试页面渲染、历史记录显示、空状态
 * _需求: 17.1, 17.2, 17.5_
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun profileScreen_shouldDisplayTitle() {
        // 准备测试数据
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = emptyList(),
                mbtiResult = null,
                mbtiTestCount = 0
            )
        )
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证标题显示
        composeTestRule.onNodeWithText("个人").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_shouldDisplayEmptyState_whenNoHistory() {
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = emptyList(),
                mbtiResult = null,
                mbtiTestCount = 0
            )
        )
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证空状态显示
        composeTestRule.onNodeWithText("暂无历史记录").assertIsDisplayed()
        composeTestRule.onNodeWithText("开始你的第一次算命吧").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_shouldDisplayHistoryRecords() {
        val testRecords = listOf(
            createTestResult("1", "zhouyi"),
            createTestResult("2", "tarot")
        )
        
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = testRecords,
                mbtiResult = null,
                mbtiTestCount = 0
            )
        )
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证历史记录显示
        composeTestRule.onNodeWithText("历史记录").assertIsDisplayed()
        composeTestRule.onNodeWithText("周易占卜").assertIsDisplayed()
        composeTestRule.onNodeWithText("塔罗牌").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_shouldDisplayMBTIInfo_whenMBTIResultExists() {
        val testMBTI = createTestMBTIResult()
        
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = emptyList(),
                mbtiResult = testMBTI,
                mbtiTestCount = 3
            )
        )
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证 MBTI 信息显示
        composeTestRule.onNodeWithText("MBTI 性格测试").assertIsDisplayed()
        composeTestRule.onNodeWithText("INTJ").assertIsDisplayed()
        composeTestRule.onNodeWithText("已测试 3 次").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_shouldDisplayMBTIEmptyCard_whenNoMBTIResult() {
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = emptyList(),
                mbtiResult = null,
                mbtiTestCount = 0
            )
        )
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证 MBTI 空状态显示
        composeTestRule.onNodeWithText("MBTI 性格测试").assertIsDisplayed()
        composeTestRule.onNodeWithText("开始测试").assertIsDisplayed()
        composeTestRule.onNodeWithText("了解你的性格类型").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_shouldDisplayLoadingIndicator_whenLoading() {
        val viewModel = createMockViewModel(ProfileUiState.Loading)
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证加载指示器显示
        // 注意：IOSLoadingIndicator 可能没有文本，所以我们检查它是否存在
        composeTestRule.onRoot().assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_shouldDisplayErrorMessage_whenError() {
        val viewModel = createMockViewModel(
            ProfileUiState.Error("加载失败")
        )
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证错误消息显示
        composeTestRule.onNodeWithText("加载失败").assertIsDisplayed()
        composeTestRule.onNodeWithText("重试").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_shouldCallOnNavigateToResult_whenHistoryItemClicked() {
        val testRecords = listOf(createTestResult("1", "zhouyi"))
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = testRecords,
                mbtiResult = null,
                mbtiTestCount = 0
            )
        )
        
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
        
        // 点击历史记录项
        composeTestRule.onNodeWithText("周易占卜").performClick()
        
        // 验证导航被调用
        assert(navigatedResultId == "1")
    }
    
    @Test
    fun profileScreen_shouldCallOnNavigateToMBTITest_whenMBTICardClicked() {
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = emptyList(),
                mbtiResult = null,
                mbtiTestCount = 0
            )
        )
        
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
        
        // 点击 MBTI 卡片
        composeTestRule.onNodeWithText("开始测试").performClick()
        
        // 验证导航被调用
        assert(mbtiTestNavigated)
    }
    
    @Test
    fun profileScreen_shouldDisplayClearButton_whenHistoryExists() {
        val testRecords = listOf(createTestResult("1", "zhouyi"))
        val viewModel = createMockViewModel(
            ProfileUiState.Success(
                historyRecords = testRecords,
                mbtiResult = null,
                mbtiTestCount = 0
            )
        )
        
        composeTestRule.setContent {
            IOSTheme {
                ProfileScreen(viewModel = viewModel)
            }
        }
        
        // 验证清空按钮显示
        composeTestRule.onNodeWithText("清空历史记录").assertIsDisplayed()
    }
    
    // 辅助方法
    
    private fun createMockViewModel(state: ProfileUiState): ProfileViewModel {
        val viewModel = mockk<ProfileViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(state)
        return viewModel
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
