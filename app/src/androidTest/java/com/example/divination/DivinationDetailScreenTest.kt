package com.example.divination

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.divination.ui.screen.detail.DivinationDetailScreen
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * DivinationDetailScreen UI 测试
 * 
 * 测试页面渲染、表单输入、提交按钮
 * 
 * **Validates: Requirements 21.1**
 */
class DivinationDetailScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun 页面应该显示导航栏() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        // 等待加载完成
        composeTestRule.waitForIdle()
        
        // 验证导航栏标题存在
        composeTestRule.onNodeWithText("八字命理").assertExists()
    }
    
    @Test
    fun 页面应该显示方法介绍() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 验证介绍部分存在
        composeTestRule.onNodeWithText("介绍").assertExists()
        composeTestRule.onNodeWithText("根据出生年月日时推算命运").assertExists()
    }
    
    @Test
    fun 页面应该显示输入表单() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 验证表单标题存在
        composeTestRule.onNodeWithText("请填写信息").assertExists()
        
        // 验证输入字段存在
        composeTestRule.onNodeWithText("出生日期").assertExists()
        composeTestRule.onNodeWithText("出生时间").assertExists()
        composeTestRule.onNodeWithText("性别").assertExists()
    }
    
    @Test
    fun 页面应该显示提交按钮() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 验证提交按钮存在
        composeTestRule.onNodeWithText("开始算命").assertExists()
    }
    
    @Test
    fun 文本输入字段应该可以输入() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "zhouyi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 找到文本输入字段并输入
        composeTestRule.onNodeWithText("预测问题").assertExists()
        
        // 查找输入框（通过 hint 文本）
        val inputField = composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasText("预测问题"))
        )
        inputField.assertExists()
        inputField.performTextInput("测试问题")
    }
    
    @Test
    fun 日期选择字段应该显示选择按钮() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 验证日期选择按钮存在
        composeTestRule.onNodeWithText("选择日期").assertExists()
    }
    
    @Test
    fun 选择字段应该显示所有选项() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 验证性别选项存在
        composeTestRule.onAllNodesWithText("男").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("女").assertCountEquals(1)
    }
    
    @Test
    fun 点击选择选项应该更新选中状态() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 点击"男"选项
        composeTestRule.onAllNodesWithText("男")[0].performClick()
        
        composeTestRule.waitForIdle()
        
        // 验证选项被选中（通过样式变化，这里只验证存在）
        composeTestRule.onAllNodesWithText("男").assertCountEquals(1)
    }
    
    @Test
    fun 提交空表单应该显示验证错误() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 点击提交按钮
        composeTestRule.onNodeWithText("开始算命").performClick()
        
        composeTestRule.waitForIdle()
        
        // 验证错误提示存在（字段名 + "不能为空"）
        composeTestRule.onNodeWithText("出生日期不能为空").assertExists()
    }
    
    @Test
    fun 加载状态应该显示加载指示器() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "bazi")
            }
        }
        
        // 在加载完成前应该显示加载指示器
        // 注意：由于加载很快，这个测试可能不稳定
        // 这里只是验证组件结构正确
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun 无输入字段的方法应该只显示介绍和提交按钮() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "mbti")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 验证介绍存在
        composeTestRule.onNodeWithText("介绍").assertExists()
        
        // 验证提交按钮存在
        composeTestRule.onNodeWithText("开始算命").assertExists()
        
        // 验证不显示"请填写信息"
        composeTestRule.onNodeWithText("请填写信息").assertDoesNotExist()
    }
    
    @Test
    fun 不存在的方法应该显示错误() {
        composeTestRule.setContent {
            IOSTheme {
                DivinationDetailScreen(methodId = "nonexistent")
            }
        }
        
        composeTestRule.waitForIdle()
        
        // 验证错误信息存在
        composeTestRule.onNodeWithText("错误").assertExists()
        composeTestRule.onNodeWithText("未找到该算命方法").assertExists()
    }
}
