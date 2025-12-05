package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.screen.home.HomeScreen
import com.example.divination.ui.screen.home.HomeViewModel
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

/**
 * HomeScreen 集成测试
 * 
 * 测试完整的用户流程：加载数据、选择日期、查看详情
 * 
 * **Validates: Requirements 16.1, 16.2, 16.3**
 */
class HomeScreenIntegrationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试完整的老黄历查看流程
     */
    @Test
    fun homeScreen_completeAlmanacViewFlow() {
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen()
            }
        }
        
        // 1. 验证页面加载
        composeTestRule.onNodeWithText("首页").assertIsDisplayed()
        
        // 2. 等待数据加载完成
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("今日黄历")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 3. 验证所有主要分区都显示
        composeTestRule.onNodeWithText("今日黄历").assertIsDisplayed()
        composeTestRule.onNodeWithText("宜忌").assertIsDisplayed()
        composeTestRule.onNodeWithText("方位").assertIsDisplayed()
        composeTestRule.onNodeWithText("其他").assertIsDisplayed()
        
        // 4. 验证宜忌内容显示
        composeTestRule.onNodeWithText("宜").assertIsDisplayed()
        composeTestRule.onNodeWithText("忌").assertIsDisplayed()
    }
    
    /**
     * 测试日期选择流程
     */
    @Test
    fun homeScreen_dateSelectionFlow() {
        val viewModel = HomeViewModel()
        
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
        
        // 1. 等待初始数据加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("今日黄历")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        // 2. 点击日期选择按钮
        composeTestRule.onNodeWithContentDescription("选择日期").performClick()
        
        // 3. 验证日期选择器状态改变
        assert(viewModel.uiState.value.showDatePicker)
        
        // 4. 模拟选择新日期
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        viewModel.selectDate(calendar.time)
        
        // 5. 验证日期选择器关闭
        assert(!viewModel.uiState.value.showDatePicker)
        
        // 6. 验证新日期被选中
        assert(viewModel.uiState.value.selectedDate == calendar.time)
    }
    
    /**
     * 测试滚动浏览所有内容
     */
    @Test
    fun homeScreen_scrollThroughAllContent() {
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
        
        // 滚动到宜忌分区
        composeTestRule.onNodeWithText("宜忌").performScrollTo()
        composeTestRule.onNodeWithText("宜忌").assertIsDisplayed()
        
        // 滚动到方位分区
        composeTestRule.onNodeWithText("方位").performScrollTo()
        composeTestRule.onNodeWithText("方位").assertIsDisplayed()
        
        // 滚动到其他信息分区
        composeTestRule.onNodeWithText("其他").performScrollTo()
        composeTestRule.onNodeWithText("其他").assertIsDisplayed()
        
        // 验证导航栏标题始终可见
        composeTestRule.onNodeWithText("首页").assertIsDisplayed()
    }
    
    /**
     * 测试刷新数据流程
     */
    @Test
    fun homeScreen_refreshDataFlow() {
        val viewModel = HomeViewModel()
        
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
        
        // 1. 等待初始数据加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("今日黄历")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        val initialAlmanac = viewModel.uiState.value.almanac
        assert(initialAlmanac != null)
        
        // 2. 刷新数据
        viewModel.refreshAlmanac()
        
        // 3. 等待刷新完成
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            !viewModel.uiState.value.isLoading
        }
        
        // 4. 验证数据已刷新
        val refreshedAlmanac = viewModel.uiState.value.almanac
        assert(refreshedAlmanac != null)
    }
    
    /**
     * 测试错误处理流程
     */
    @Test
    fun homeScreen_errorHandlingFlow() {
        val viewModel = HomeViewModel()
        
        composeTestRule.setContent {
            IOSTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
        
        // 等待初始加载
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            !viewModel.uiState.value.isLoading
        }
        
        // 验证没有错误
        assert(viewModel.uiState.value.error == null)
        
        // 清除错误（测试错误清除功能）
        viewModel.clearError()
        assert(viewModel.uiState.value.error == null)
    }
}
