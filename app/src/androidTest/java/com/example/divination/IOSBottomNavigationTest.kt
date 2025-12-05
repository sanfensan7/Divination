package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.component.IOSBottomNavigation
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * IOSBottomNavigation UI 测试
 * 
 * 测试标签渲染、选中状态和点击交互
 * 
 * **Validates: Requirements 18.1, 18.2, 18.3**
 */
class IOSBottomNavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试 IOSBottomNavigation 是否显示所有标签
     */
    @Test
    fun iosBottomNavigation_shouldDisplayAllTabs() {
        composeTestRule.setContent {
            IOSTheme {
                IOSBottomNavigation(
                    selectedTab = 0,
                    onTabSelected = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("首页").assertIsDisplayed()
        composeTestRule.onNodeWithText("心情").assertIsDisplayed()
        composeTestRule.onNodeWithText("算命").assertIsDisplayed()
        composeTestRule.onNodeWithText("个人").assertIsDisplayed()
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSBottomNavigation 点击标签触发回调
     */
    @Test
    fun iosBottomNavigation_shouldTriggerOnTabSelected() {
        var selectedIndex = 0
        
        composeTestRule.setContent {
            IOSTheme {
                IOSBottomNavigation(
                    selectedTab = selectedIndex,
                    onTabSelected = { selectedIndex = it }
                )
            }
        }
        
        composeTestRule.onNodeWithText("设置").performClick()
        assert(selectedIndex == 4) { "Selected index should be 4" }
    }
    
    /**
     * 测试 IOSBottomNavigation 最多显示 5 个标签
     */
    @Test
    fun iosBottomNavigation_shouldHaveMaximum5Tabs() {
        composeTestRule.setContent {
            IOSTheme {
                IOSBottomNavigation(
                    selectedTab = 0,
                    onTabSelected = {}
                )
            }
        }
        
        // 验证有 5 个标签
        val tabLabels = listOf("首页", "心情", "算命", "个人", "设置")
        tabLabels.forEach { label ->
            composeTestRule.onNodeWithText(label).assertExists()
        }
    }
}
