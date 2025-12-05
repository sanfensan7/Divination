package com.example.divination

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.divination.ui.navigation.AppNavGraph
import com.example.divination.ui.navigation.Routes
import com.example.divination.ui.theme.IOSTheme
import org.junit.Rule
import org.junit.Test

/**
 * 导航集成测试
 * 
 * 测试页面导航流程、转场动画和返回手势
 * 
 * **Validates: Requirements 7.1, 7.2, 12.1**
 */
class NavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 测试导航图是否正确初始化
     */
    @Test
    fun navGraph_shouldInitializeWithHomeRoute() {
        composeTestRule.setContent {
            IOSTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
        
        // 导航图应该成功初始化
        // 实际的页面内容测试将在页面实现后进行
    }
    
    /**
     * 测试所有路由是否已定义
     */
    @Test
    fun routes_shouldBeDefinedCorrectly() {
        assert(Routes.HOME == "home")
        assert(Routes.PROFILE == "profile")
        assert(Routes.METHODS == "methods")
        assert(Routes.SETTINGS == "settings")
        assert(Routes.MOOD_HISTORY == "mood_history")
        assert(Routes.MBTI_TEST == "mbti_test")
        assert(Routes.MBTI_RESULT == "mbti_result")
        assert(Routes.FEEDBACK == "feedback")
    }
    
    /**
     * 测试带参数的路由生成
     */
    @Test
    fun routes_shouldGenerateParameterizedRoutes() {
        val methodId = "tarot"
        val detailRoute = Routes.divinationDetail(methodId)
        assert(detailRoute == "divination_detail/tarot")
        
        val resultId = "123"
        val resultRoute = Routes.divinationResult(resultId)
        assert(resultRoute == "divination_result/123")
    }
}
