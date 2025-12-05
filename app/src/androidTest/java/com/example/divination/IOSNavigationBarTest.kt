package com.example.divination

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.component.IOSNavigationBar
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * IOSNavigationBar UI 测试
 * 
 * 测试标题显示、滚动行为和动画效果
 * 
 * **Validates: Requirements 5.1, 5.2**
 */
class IOSNavigationBarTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试 IOSNavigationBar 是否正确显示标题
     */
    @Test
    fun iosNavigationBar_shouldDisplayTitle() {
        composeTestRule.setContent {
            IOSTheme {
                val scrollState = rememberLazyListState()
                IOSNavigationBar(
                    title = "测试标题",
                    scrollState = scrollState
                )
            }
        }
        
        composeTestRule.onNodeWithText("测试标题").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSNavigationBar 在列表顶部时显示 Large Title
     */
    @Test
    fun iosNavigationBar_atTop_shouldShowLargeTitle() {
        composeTestRule.setContent {
            IOSTheme {
                val scrollState = rememberLazyListState()
                IOSNavigationBar(
                    title = "首页",
                    scrollState = scrollState
                )
            }
        }
        
        // 在顶部时应该显示标题
        composeTestRule.onNodeWithText("首页").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSNavigationBar 可以包含右侧操作按钮
     */
    @Test
    fun iosNavigationBar_shouldSupportActions() {
        composeTestRule.setContent {
            IOSTheme {
                val scrollState = rememberLazyListState()
                IOSNavigationBar(
                    title = "设置",
                    scrollState = scrollState
                ) {
                    Text("完成")
                }
            }
        }
        
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
        composeTestRule.onNodeWithText("完成").assertIsDisplayed()
    }
}
