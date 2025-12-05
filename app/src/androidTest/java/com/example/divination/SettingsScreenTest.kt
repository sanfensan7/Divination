package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.divination.ui.screen.settings.SettingsScreen
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * SettingsScreen UI 测试
 * 
 * 测试页面渲染、组件显示和交互行为
 * 
 * **Validates: Requirements 20.1, 20.2**
 */
class SettingsScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试设置页面是否显示标题
     */
    @Test
    fun settingsScreen_shouldDisplayTitle() {
        composeTestRule.setContent {
            IOSTheme {
                val navController = rememberNavController()
                SettingsScreen(navController = navController)
            }
        }
        
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
    }
    
    /**
     * 测试设置页面是否显示使用统计
     */
    @Test
    fun settingsScreen_shouldDisplayUsageStatistics() {
        composeTestRule.setContent {
            IOSTheme {
                val navController = rememberNavController()
                SettingsScreen(navController = navController)
            }
        }
        
        composeTestRule.onNodeWithText("使用统计").assertIsDisplayed()
        composeTestRule.onNodeWithText("今日使用").assertIsDisplayed()
        composeTestRule.onNodeWithText("总使用次数").assertIsDisplayed()
    }
    
    /**
     * 测试设置页面是否显示反馈与支持
     */
    @Test
    fun settingsScreen_shouldDisplayFeedbackSection() {
        composeTestRule.setContent {
            IOSTheme {
                val navController = rememberNavController()
                SettingsScreen(navController = navController)
            }
        }
        
        composeTestRule.onNodeWithText("反馈与支持").assertIsDisplayed()
        composeTestRule.onNodeWithText("意见反馈").assertIsDisplayed()
        composeTestRule.onNodeWithText("隐私政策").assertIsDisplayed()
    }
    
    /**
     * 测试设置页面是否显示关于信息
     */
    @Test
    fun settingsScreen_shouldDisplayAboutSection() {
        composeTestRule.setContent {
            IOSTheme {
                val navController = rememberNavController()
                SettingsScreen(navController = navController)
            }
        }
        
        composeTestRule.onNodeWithText("关于").assertIsDisplayed()
        composeTestRule.onNodeWithText("版本信息").assertIsDisplayed()
        composeTestRule.onNodeWithText("关于我们").assertIsDisplayed()
    }
    
    /**
     * 测试设置页面是否显示版本号
     */
    @Test
    fun settingsScreen_shouldDisplayVersionNumber() {
        composeTestRule.setContent {
            IOSTheme {
                val navController = rememberNavController()
                SettingsScreen(navController = navController)
            }
        }
        
        composeTestRule.onNodeWithText("1.1.4").assertIsDisplayed()
    }
}
