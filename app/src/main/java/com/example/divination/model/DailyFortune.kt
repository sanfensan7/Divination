package com.example.divination.model

import java.util.*

/**
 * 每日运势数据模型
 */
data class DailyFortune(
    val date: String,                         // 日期：yyyy-MM-dd
    val overallLuck: Int,                     // 综合运势：1-5星
    val loveLuck: Int,                        // 爱情运势：1-5星
    val careerLuck: Int,                      // 事业运势：1-5星
    val wealthLuck: Int,                      // 财运：1-5星
    val healthLuck: Int,                      // 健康运势：1-5星
    val luckyColor: String,                   // 今日幸运色
    val luckyNumber: Int,                     // 今日幸运数字
    val luckyDirection: String,               // 今日吉方位
    val warnings: List<String>,               // 今日警告/注意事项
    val suggestions: List<String>,            // 今日建议
    val luckyTime: String,                    // 吉时
    val avoidTime: String,                    // 凶时
    val summary: String                       // 运势总结
) {
    /**
     * 获取综合运势描述
     */
    fun getOverallLuckDescription(): String {
        return when (overallLuck) {
            5 -> "大吉"
            4 -> "吉"
            3 -> "平"
            2 -> "小凶"
            1 -> "凶"
            else -> "未知"
        }
    }
    
    /**
     * 获取运势颜色
     */
    fun getOverallLuckColor(): String {
        return when (overallLuck) {
            5 -> "#FFD700"  // 金色
            4 -> "#4CAF50"  // 绿色
            3 -> "#FFC107"  // 黄色
            2 -> "#FF9800"  // 橙色
            1 -> "#F44336"  // 红色
            else -> "#9E9E9E"
        }
    }
    
    /**
     * 获取星级显示
     */
    fun getStarDisplay(luck: Int): String {
        return "★".repeat(luck) + "☆".repeat(5 - luck)
    }
}
