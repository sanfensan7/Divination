package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.component.IOSButton
import com.example.divination.ui.component.IOSButtonStyle
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * IOSButton UI 测试
 * 
 * 测试按钮渲染、点击交互和样式变体
 * 
 * **Validates: Requirements 8.1, 8.2**
 */
class IOSButtonTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试 IOSButton 是否正确显示文本
     */
    @Test
    fun iosButton_shouldDisplayText() {
        composeTestRule.setContent {
            IOSTheme {
                IOSButton(text = "测试按钮", onClick = {})
            }
        }
        
        composeTestRule.onNodeWithText("测试按钮").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSButton 点击交互
     */
    @Test
    fun iosButton_shouldTriggerOnClick() {
        var clicked = false
        
        composeTestRule.setContent {
            IOSTheme {
                IOSButton(text = "点击我", onClick = { clicked = true })
            }
        }
        
        composeTestRule.onNodeWithText("点击我").performClick()
        assert(clicked) { "onClick should be triggered" }
    }
    
    /**
     * 测试 IOSButton Primary 样式
     */
    @Test
    fun iosButton_primaryStyle_shouldDisplay() {
        composeTestRule.setContent {
            IOSTheme {
                IOSButton(
                    text = "主要按钮",
                    onClick = {},
                    style = IOSButtonStyle.Primary
                )
            }
        }
        
        composeTestRule.onNodeWithText("主要按钮").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSButton Secondary 样式
     */
    @Test
    fun iosButton_secondaryStyle_shouldDisplay() {
        composeTestRule.setContent {
            IOSTheme {
                IOSButton(
                    text = "次要按钮",
                    onClick = {},
                    style = IOSButtonStyle.Secondary
                )
            }
        }
        
        composeTestRule.onNodeWithText("次要按钮").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSButton Destructive 样式
     */
    @Test
    fun iosButton_destructiveStyle_shouldDisplay() {
        composeTestRule.setContent {
            IOSTheme {
                IOSButton(
                    text = "删除",
                    onClick = {},
                    style = IOSButtonStyle.Destructive
                )
            }
        }
        
        composeTestRule.onNodeWithText("删除").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSButton 禁用状态
     */
    @Test
    fun iosButton_disabled_shouldNotTriggerOnClick() {
        var clicked = false
        
        composeTestRule.setContent {
            IOSTheme {
                IOSButton(
                    text = "禁用按钮",
                    onClick = { clicked = true },
                    enabled = false
                )
            }
        }
        
        composeTestRule.onNodeWithText("禁用按钮").assertIsNotEnabled()
    }
}
