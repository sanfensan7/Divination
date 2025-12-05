package com.example.divination.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * 导航路由定义
 */
object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val METHODS = "methods"
    const val SETTINGS = "settings"
    const val MOOD_HISTORY = "mood_history"
    const val DIVINATION_DETAIL = "divination_detail/{methodId}"
    const val DIVINATION_RESULT = "divination_result/{resultId}"
    const val MBTI_TEST = "mbti_test"
    const val MBTI_RESULT = "mbti_result"
    const val FEEDBACK = "feedback"
    
    /**
     * 创建带参数的详情路由
     */
    fun divinationDetail(methodId: String) = "divination_detail/$methodId"
    
    /**
     * 创建带参数的结果路由
     */
    fun divinationResult(resultId: String) = "divination_result/$resultId"
}

/**
 * 应用导航图
 * 
 * 定义所有屏幕路由和 iOS 风格转场动画
 * 
 * @param navController 导航控制器
 * @param modifier 修饰符
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = NavigationAnimations.enterTransition(),
        exitTransition = NavigationAnimations.exitTransition(),
        popEnterTransition = NavigationAnimations.popEnterTransition(),
        popExitTransition = NavigationAnimations.popExitTransition()
    ) {
        // 首页
        composable(Routes.HOME) {
            com.example.divination.ui.screen.home.HomeScreen()
        }
        
        // 个人页面
        composable(Routes.PROFILE) {
            com.example.divination.ui.screen.profile.ProfileScreen(
                onNavigateToResult = { resultId ->
                    navController.navigate(Routes.divinationResult(resultId))
                },
                onNavigateToMBTITest = {
                    navController.navigate(Routes.MBTI_TEST)
                }
            )
        }
        
        // 算命方式页面
        composable(Routes.METHODS) {
            com.example.divination.ui.screen.methods.MethodsScreen(
                onMethodClick = { methodId ->
                    navController.navigate(Routes.divinationDetail(methodId))
                }
            )
        }
        
        // 设置页面
        composable(Routes.SETTINGS) {
            com.example.divination.ui.screen.settings.SettingsScreen(navController = navController)
        }
        
        // 心情历史页面
        composable(Routes.MOOD_HISTORY) {
            com.example.divination.ui.screen.mood.MoodHistoryScreen()
        }
        
        // 算命详情页面
        composable(
            route = Routes.DIVINATION_DETAIL,
            arguments = listOf(
                navArgument("methodId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val methodId = backStackEntry.arguments?.getString("methodId") ?: ""
            com.example.divination.ui.screen.detail.DivinationDetailScreen(
                methodId = methodId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { resultId ->
                    navController.navigate(Routes.divinationResult(resultId))
                },
                onNavigateToMBTITest = {
                    navController.navigate(Routes.MBTI_TEST)
                }
            )
        }
        
        // 算命结果页面
        composable(
            route = Routes.DIVINATION_RESULT,
            arguments = listOf(
                navArgument("resultId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getString("resultId") ?: ""
            com.example.divination.ui.screen.result.DivinationResultScreen(
                resultId = resultId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // MBTI 测试页面
        composable(Routes.MBTI_TEST) {
            com.example.divination.ui.screen.mbti.MBTITestScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { resultId ->
                    navController.navigate(Routes.MBTI_RESULT) {
                        popUpTo(Routes.MBTI_TEST) { inclusive = true }
                    }
                }
            )
        }
        
        // MBTI 结果页面
        composable(Routes.MBTI_RESULT) {
            com.example.divination.ui.screen.mbti.MBTIResultScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 反馈页面
        composable(Routes.FEEDBACK) {
            com.example.divination.ui.screen.feedback.FeedbackScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
