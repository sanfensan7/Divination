package com.example.divination

import androidx.compose.material.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.component.IOSSection
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * IOSSection UI 测试
 * 
 * 测试分区渲染、标题显示和内容布局
 * 
 * **Validates: Requirements 6.1, 6.2**
 */
class IOSSectionTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试 IOSSection 是否正确显示标题
     */
    @Test
    fun iosSection_shouldDisplayTitle() {
        composeTestRule.setContent {
            IOSTheme {
                IOSSection(title = "个人信息") {
                    Text("内容")
                }
            }
        }
        
        // 标题应该转换为大写
        composeTestRule.onNodeWithText("个人信息").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSSection 是否正确显示内容
     */
    @Test
    fun iosSection_shouldDisplayContent() {
        composeTestRule.setContent {
            IOSTheme {
                IOSSection(title = "设置") {
                    Text("通知")
                    Text("隐私")
                }
            }
        }
        
        composeTestRule.onNodeWithText("通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("隐私").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSSection 可以没有标题
     */
    @Test
    fun iosSection_withoutTitle_shouldDisplayContent() {
        composeTestRule.setContent {
            IOSTheme {
                IOSSection {
                    Text("无标题内容")
                }
            }
        }
        
        composeTestRule.onNodeWithText("无标题内容").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSSection 可以包含多个子元素
     */
    @Test
    fun iosSection_shouldSupportMultipleChildren() {
        composeTestRule.setContent {
            IOSTheme {
                IOSSection(title = "账户") {
                    Text("用户名")
                    Text("邮箱")
                    Text("电话")
                }
            }
        }
        
        composeTestRule.onNodeWithText("用户名").assertIsDisplayed()
        composeTestRule.onNodeWithText("邮箱").assertIsDisplayed()
        composeTestRule.onNodeWithText("电话").assertIsDisplayed()
    }
}
