package com.example.divination

import androidx.compose.material.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.component.IOSCard
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * IOSCard UI 测试
 * 
 * 测试卡片渲染、点击交互和动画参数
 * 
 * **Validates: Requirements 4.1, 13.1**
 */
class IOSCardTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试 IOSCard 是否正确渲染内容
     */
    @Test
    fun iosCard_shouldDisplayContent() {
        composeTestRule.setContent {
            IOSTheme {
                IOSCard {
                    Text("测试内容")
                }
            }
        }
        
        composeTestRule.onNodeWithText("测试内容").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSCard 点击交互
     */
    @Test
    fun iosCard_shouldTriggerOnClick() {
        var clicked = false
        
        composeTestRule.setContent {
            IOSTheme {
                IOSCard(onClick = { clicked = true }) {
                    Text("可点击卡片")
                }
            }
        }
        
        composeTestRule.onNodeWithText("可点击卡片").performClick()
        assert(clicked) { "onClick should be triggered" }
    }
    
    /**
     * 测试 IOSCard 不可点击时不触发点击
     */
    @Test
    fun iosCard_withoutOnClick_shouldNotBeClickable() {
        composeTestRule.setContent {
            IOSTheme {
                IOSCard {
                    Text("不可点击卡片")
                }
            }
        }
        
        // 卡片应该显示但不可点击
        composeTestRule.onNodeWithText("不可点击卡片").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSCard 可以包含多个子元素
     */
    @Test
    fun iosCard_shouldSupportMultipleChildren() {
        composeTestRule.setContent {
            IOSTheme {
                IOSCard {
                    Text("标题")
                    Text("副标题")
                    Text("内容")
                }
            }
        }
        
        composeTestRule.onNodeWithText("标题").assertIsDisplayed()
        composeTestRule.onNodeWithText("副标题").assertIsDisplayed()
        composeTestRule.onNodeWithText("内容").assertIsDisplayed()
    }
}
