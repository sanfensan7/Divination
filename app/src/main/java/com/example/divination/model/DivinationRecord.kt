package com.example.divination.model

import java.util.*

/**
 * 算命行为记录
 */
data class DivinationRecord(
    val id: String = UUID.randomUUID().toString(),
    val methodId: String,                    // 算命方法ID
    val methodName: String,                  // 算命方法名称
    val timestamp: Date,                     // 时间戳
    val questionType: String = "",           // 问题类型（事业/感情/财运/健康/其他）
    val duration: Long = 0                   // 停留时长（毫秒）
)

/**
 * 算命偏好统计
 */
data class DivinationPreferences(
    val totalCount: Int,                     // 总次数
    val favoriteMethod: String,              // 最喜欢的方法
    val favoriteMethodCount: Int,            // 最喜欢方法的次数
    val favoriteMethodPercentage: Float,     // 最喜欢方法的占比
    val methodDistribution: Map<String, Int>, // 各方法使用分布
    val questionTypeDistribution: Map<String, Int>, // 问题类型分布
    val averageDuration: Long,               // 平均停留时长
    val activeTimeSlot: String               // 活跃时段
)

/**
 * 运势趋势
 */
data class FortuneTrend(
    val period: String,                      // 周期（7天/30天）
    val averageOverallLuck: Float,           // 平均综合运势
    val averageLoveLuck: Float,              // 平均爱情运势
    val averageCareerLuck: Float,            // 平均事业运势
    val averageWealthLuck: Float,            // 平均财运
    val averageHealthLuck: Float,            // 平均健康运势
    val luckyDays: Int,                      // 幸运日数量（4星以上）
    val unluckyDays: Int,                    // 不利日数量（2星以下）
    val fortuneHistory: List<DailyFortune>   // 历史运势列表
)

/**
 * 行为统计
 */
data class BehaviorStats(
    val totalDivinationCount: Int,           // 总算命次数
    val continuousLoginDays: Int,            // 连续登录天数
    val totalLoginDays: Int,                 // 总登录天数
    val lastVisitTime: Date?,                // 最后访问时间
    val favoriteCount: Int,                  // 收藏数量
    val shareCount: Int,                     // 分享次数
    val averageSessionDuration: Long         // 平均会话时长
)

/**
 * 用户画像
 */
data class UserPortrait(
    val basicInfo: UserProfile,              // 基础信息
    val preferences: DivinationPreferences?, // 算命偏好
    val fortuneTrend: FortuneTrend?,         // 运势趋势
    val behaviorStats: BehaviorStats,        // 行为统计
    val smartTags: List<String>,             // 智能标签
    val personalityTraits: List<String>,     // 性格特征
    val level: Int,                          // 用户等级
    val experience: Int                      // 经验值
)
