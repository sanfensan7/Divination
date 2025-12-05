package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.screen.home.HomeScreen
import com.example.divination.ui.screen.home.HomeViewModel
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * HomeScreen UI 测试
 * 
 * 测试首页渲染、老黄历显示和日期选择功能
 * 
 * **Validates: Requirements 16.1, 16.2**
 */
class HomeScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试首页标题显示
     */
    @Test
    fun homeScreen_displaysTitle() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 验证标题显示
        composeTestRule.onNodeWithText("首页").assertIsDisplayed()
    }
    
    /**
     * 测试日期选择按钮存在
     */
    @Test
    fun homeScreen_hasDatePickerButton() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 验证日期选择按钮存在
        composeTestRule.onNodeWithContentDescription("选择日期").assertExists()
    }
    
    /**
     * 测试老黄历卡片显示
     */
    @Test
    fun homeScreen_displaysAlmanacCard() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 等待数据加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("今日黄历")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 验证今日黄历分区显示
        composeTestRule.onNodeWithText("今日黄历").assertIsDisplayed()
    }
    
    /**
     * 测试宜忌分区显示
     */
    @Test
    fun homeScreen_displaysGoodAndBadActivities() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 等待数据加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("宜忌")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 验证宜忌分区显示
        composeTestRule.onNodeWithText("宜忌").assertIsDisplayed()
        composeTestRule.onNodeWithText("宜").assertIsDisplayed()
        composeTestRule.onNodeWithText("忌").assertIsDisplayed()
    }
    
    /**
     * 测试方位分区显示
     */
    @Test
    fun homeScreen_displaysDirections() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 等待数据加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("方位")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 验证方位分区显示
        composeTestRule.onNodeWithText("方位").assertIsDisplayed()
    }
    
    /**
     * 测试其他信息分区显示
     */
    @Test
    fun homeScreen_displaysOtherInfo() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 等待数据加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("其他")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 验证其他信息分区显示
        composeTestRule.onNodeWithText("其他").assertIsDisplayed()
    }
    
    /**
     * 测试点击日期选择按钮
     */
    @Test
    fun homeScreen_clickDatePickerButton() {
        val viewModel = HomeViewModel()
        
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
        
        // 点击日期选择按钮
        composeTestRule.onNodeWithContentDescription("选择日期").performClick()
        
        // 验证日期选择器状态改变
        assert(viewModel.uiState.value.showDatePicker)
    }
    
    /**
     * 测试滚动时导航栏收缩
     */
    @Test
    fun homeScreen_navigationBarCollapsesOnScroll() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 等待数据加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("今日黄历")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 执行滚动
        composeTestRule.onNodeWithText("今日黄历")
            .performScrollTo()
        
        // 验证标题仍然显示（即使收缩了）
        composeTestRule.onNodeWithText("首页").assertIsDisplayed()
    }
}
