package com.example.divination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.divination.ui.component.IOSBottomNavigation
import com.example.divination.ui.navigation.AppNavGraph
import com.example.divination.ui.navigation.Routes
import com.example.divination.ui.theme.IOSTheme

/**
 * 主活动 - Compose 入口
 * 
 * 使用 Jetpack Compose 和 iOS 风格 UI 组件
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            IOSTheme {
                MainScreen()
            }
        }
    }
}

/**
 * 主屏幕组件
 * 
 * 集成底部导航和页面导航
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // 确定当前选中的底部导航标签
    val selectedTab = when (currentRoute) {
        Routes.HOME -> 0
        Routes.MOOD_HISTORY -> 1
        Routes.METHODS -> 2
        Routes.PROFILE -> 3
        Routes.SETTINGS -> 4
        else -> 0
    }
    
    // 底部导航可见的路由
    val bottomNavRoutes = setOf(
        Routes.HOME,
        Routes.MOOD_HISTORY,
        Routes.METHODS,
        Routes.PROFILE,
        Routes.SETTINGS
    )
    
    Scaffold(
        bottomBar = {
            // 只在主要页面显示底部导航
            if (currentRoute in bottomNavRoutes) {
                IOSBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        val route = when (index) {
                            0 -> Routes.HOME
                            1 -> Routes.MOOD_HISTORY
                            2 -> Routes.METHODS
                            3 -> Routes.PROFILE
                            4 -> Routes.SETTINGS
                            else -> Routes.HOME
                        }
                        
                        // 导航到选中的路由
                        navController.navigate(route) {
                            // 弹出到起始目的地，避免堆栈过深
                            popUpTo(Routes.HOME) {
                                saveState = true
                            }
                            // 避免重复导航到同一目的地
                            launchSingleTop = true
                            // 恢复状态
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
} 