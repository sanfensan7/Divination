package com.example.divination

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * iOS 辅助组件 UI 测试
 * 
 * 测试各组件的渲染和基本交互
 * 
 * **Validates: Requirements 11.1, 11.2**
 */
class IOSComponentsTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试 IOSListItem 是否正确显示标题
     */
    @Test
    fun iosListItem_shouldDisplayTitle() {
        composeTestRule.setContent {
            IOSTheme {
                IOSListItem(title = "列表项标题")
            }
        }
        
        composeTestRule.onNodeWithText("列表项标题").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSListItem 是否显示副标题
     */
    @Test
    fun iosListItem_shouldDisplaySubtitle() {
        composeTestRule.setContent {
            IOSTheme {
                IOSListItem(
                    title = "标题",
                    subtitle = "副标题"
                )
            }
        }
        
        composeTestRule.onNodeWithText("标题").assertIsDisplayed()
        composeTestRule.onNodeWithText("副标题").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSListItem 点击交互
     */
    @Test
    fun iosListItem_shouldTriggerOnClick() {
        var clicked = false
        
        composeTestRule.setContent {
            IOSTheme {
                IOSListItem(
                    title = "可点击项",
                    onClick = { clicked = true }
                )
            }
        }
        
        composeTestRule.onNodeWithText("可点击项").performClick()
        assert(clicked) { "onClick should be triggered" }
    }
    
    /**
     * 测试 IOSLoadingIndicator 是否显示
     */
    @Test
    fun iosLoadingIndicator_shouldDisplay() {
        composeTestRule.setContent {
            IOSTheme {
                IOSLoadingIndicator()
            }
        }
        
        // 加载指示器应该存在
        composeTestRule.onNode(hasTestTag("loading") or hasContentDescription("加载中"))
            .assertExists()
            .assertIsDisplayed()
    }
    
    /**
     * 测试 IOSSearchBar 是否接受输入
     */
    @Test
    fun iosSearchBar_shouldAcceptInput() {
        var searchText by mutableStateOf("")
        
        composeTestRule.setContent {
            IOSTheme {
                IOSSearchBar(
                    value = searchText,
                    onValueChange = { searchText = it }
                )
            }
        }
        
        // 输入文本
        composeTestRule.onNode(hasSetTextAction()).performTextInput("测试搜索")
        assert(searchText == "测试搜索") { "Search text should be updated" }
    }
    
    /**
     * 测试 IOSSwitch 切换状态
     */
    @Test
    fun iosSwitch_shouldToggleState() {
        var checked by mutableStateOf(false)
        
        composeTestRule.setContent {
            IOSTheme {
                IOSSwitch(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }
        }
        
        // 点击开关
        composeTestRule.onNode(hasClickAction()).performClick()
        assert(checked) { "Switch should be checked" }
    }
    
    /**
     * 测试 IOSEmptyState 是否显示标题和描述
     */
    @Test
    fun iosEmptyState_shouldDisplayTitleAndDescription() {
        composeTestRule.setContent {
            IOSTheme {
                IOSEmptyState(
                    title = "暂无数据",
                    description = "请稍后再试",
                    icon = Icons.Outlined.Info
                )
            }
        }
        
        composeTestRule.onNodeWithText("暂无数据").assertIsDisplayed()
        composeTestRule.onNodeWithText("请稍后再试").assertIsDisplayed()
    }
    
    /**
     * 测试 IOSList 可以包含多个列表项
     */
    @Test
    fun iosList_shouldContainMultipleItems() {
        composeTestRule.setContent {
            IOSTheme {
                IOSList {
                    IOSListItem(title = "项目 1")
                    IOSListItem(title = "项目 2")
                    IOSListItem(title = "项目 3")
                }
            }
        }
        
        composeTestRule.onNodeWithText("项目 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("项目 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("项目 3").assertIsDisplayed()
    }
}
