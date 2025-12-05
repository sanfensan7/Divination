package com.example.divination.utils

import android.content.Context
import com.example.divination.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 用户画像分析器
 */
object UserPortraitAnalyzer {
    
    /**
     * 生成完整的用户画像
     */
    fun generateUserPortrait(context: Context): UserPortrait? {
        val profile = UserProfileService.getUserProfile(context) ?: return null
        
        val records = DivinationRecordService.getAllRecords(context)
        val fortuneHistory = FortuneHistoryService.getAllHistory(context)
        
        return UserPortrait(
            basicInfo = profile,
            preferences = analyzePreferences(records),
            fortuneTrend = analyzeFortuneTrend(fortuneHistory, 7),
            behaviorStats = analyzeBehaviorStats(context, records),
            smartTags = generateSmartTags(profile, records, fortuneHistory),
            personalityTraits = extractPersonalityTraits(profile),
            level = calculateUserLevel(records.size),
            experience = records.size * 10
        )
    }
    
    /**
     * 分析算命偏好
     */
    fun analyzePreferences(records: List<DivinationRecord>): DivinationPreferences? {
        if (records.isEmpty()) return null
        
        // 统计各方法使用次数
        val methodCounts = records.groupingBy { it.methodName }.eachCount()
        val favoriteMethod = methodCounts.maxByOrNull { it.value }
        
        // 统计问题类型分布
        val questionTypeCounts = records
            .filter { it.questionType.isNotEmpty() }
            .groupingBy { it.questionType }
            .eachCount()
        
        // 计算平均停留时长
        val avgDuration = if (records.isNotEmpty()) {
            records.map { it.duration }.average().toLong()
        } else 0L
        
        // 分析活跃时段
        val activeTimeSlot = analyzeActiveTimeSlot(records)
        
        return DivinationPreferences(
            totalCount = records.size,
            favoriteMethod = favoriteMethod?.key ?: "",
            favoriteMethodCount = favoriteMethod?.value ?: 0,
            favoriteMethodPercentage = if (records.isNotEmpty()) {
                (favoriteMethod?.value ?: 0) * 100f / records.size
            } else 0f,
            methodDistribution = methodCounts,
            questionTypeDistribution = questionTypeCounts,
            averageDuration = avgDuration,
            activeTimeSlot = activeTimeSlot
        )
    }
    
    /**
     * 分析运势趋势
     */
    fun analyzeFortuneTrend(history: List<DailyFortune>, days: Int): FortuneTrend? {
        if (history.isEmpty()) return null
        
        val recentHistory = history.take(days)
        
        val avgOverall = recentHistory.map { it.overallLuck }.average().toFloat()
        val avgLove = recentHistory.map { it.loveLuck }.average().toFloat()
        val avgCareer = recentHistory.map { it.careerLuck }.average().toFloat()
        val avgWealth = recentHistory.map { it.wealthLuck }.average().toFloat()
        val avgHealth = recentHistory.map { it.healthLuck }.average().toFloat()
        
        val luckyDays = recentHistory.count { it.overallLuck >= 4 }
        val unluckyDays = recentHistory.count { it.overallLuck <= 2 }
        
        return FortuneTrend(
            period = "${days}天",
            averageOverallLuck = avgOverall,
            averageLoveLuck = avgLove,
            averageCareerLuck = avgCareer,
            averageWealthLuck = avgWealth,
            averageHealthLuck = avgHealth,
            luckyDays = luckyDays,
            unluckyDays = unluckyDays,
            fortuneHistory = recentHistory
        )
    }
    
    /**
     * 分析行为统计
     */
    fun analyzeBehaviorStats(context: Context, records: List<DivinationRecord>): BehaviorStats {
        val lastVisit = records.maxByOrNull { it.timestamp }?.timestamp
        
        // 计算连续登录天数（简化版）
        val continuousDays = calculateContinuousLoginDays(records)
        
        // 计算总登录天数
        val uniqueDays = records.map {
            android.text.format.DateFormat.format("yyyy-MM-dd", it.timestamp).toString()
        }.distinct().size
        
        // 平均会话时长
        val avgSessionDuration = if (records.isNotEmpty()) {
            records.map { it.duration }.average().toLong()
        } else 0L
        
        return BehaviorStats(
            totalDivinationCount = records.size,
            continuousLoginDays = continuousDays,
            totalLoginDays = uniqueDays,
            lastVisitTime = lastVisit,
            favoriteCount = 0,  // TODO: 从收藏服务获取
            shareCount = 0,     // TODO: 从分享服务获取
            averageSessionDuration = avgSessionDuration
        )
    }
    
    /**
     * 生成智能标签
     */
    fun generateSmartTags(
        profile: UserProfile,
        records: List<DivinationRecord>,
        fortuneHistory: List<DailyFortune>
    ): List<String> {
        val tags = mutableListOf<String>()
        
        // 基于算命次数
        when {
            records.size >= 100 -> tags.add("🏆 命理大师")
            records.size >= 50 -> tags.add("⭐ 命理爱好者")
            records.size >= 20 -> tags.add("🌟 算命达人")
            records.size >= 10 -> tags.add("✨ 初窥门径")
        }
        
        // 基于最常用方法
        if (records.isNotEmpty()) {
            val methodCounts = records.groupingBy { it.methodName }.eachCount()
            val favoriteMethod = methodCounts.maxByOrNull { it.value }
            val percentage = (favoriteMethod?.value ?: 0) * 100f / records.size
            
            if (percentage >= 40) {
                when (favoriteMethod?.key) {
                    "塔罗牌" -> tags.add("🃏 塔罗达人")
                    "八字命理" -> tags.add("📿 八字专家")
                    "占星学" -> tags.add("🌟 占星师")
                    "周易卦象" -> tags.add("☯️ 易经行家")
                    "紫微斗数" -> tags.add("⭐ 紫微高手")
                }
            }
        }
        
        // 基于问题类型
        val questionTypes = records.filter { it.questionType.isNotEmpty() }
        if (questionTypes.isNotEmpty()) {
            val typeCounts = questionTypes.groupingBy { it.questionType }.eachCount()
            val favoriteType = typeCounts.maxByOrNull { it.value }
            val percentage = (favoriteType?.value ?: 0) * 100f / questionTypes.size
            
            if (percentage >= 50) {
                when (favoriteType?.key) {
                    "事业" -> tags.add("💼 事业型")
                    "感情" -> tags.add("💕 感情专注")
                    "财运" -> tags.add("💰 财富追求者")
                    "健康" -> tags.add("🏥 健康关注")
                }
            }
        }
        
        // 基于连续登录
        val continuousDays = calculateContinuousLoginDays(records)
        when {
            continuousDays >= 30 -> tags.add("🔥 月度坚持")
            continuousDays >= 7 -> tags.add("📅 每日必看")
            continuousDays >= 3 -> tags.add("⏰ 常客")
        }
        
        // 基于运势
        if (fortuneHistory.isNotEmpty()) {
            val recentFortune = fortuneHistory.take(7)
            val avgLuck = recentFortune.map { it.overallLuck }.average()
            
            when {
                avgLuck >= 4.0 -> tags.add("🍀 幸运儿")
                avgLuck <= 2.0 -> tags.add("🌧️ 需要转运")
            }
        }
        
        // 基于MBTI
        if (profile.mbtiType.isNotEmpty()) {
            tags.add("🧠 ${profile.mbtiType}人格")
        }
        
        // 基于星座
        if (profile.zodiacSign.isNotEmpty()) {
            tags.add("♈ ${profile.zodiacSign}")
        }
        
        return tags
    }
    
    /**
     * 提取性格特征
     */
    fun extractPersonalityTraits(profile: UserProfile): List<String> {
        val traits = mutableListOf<String>()
        
        // 基于MBTI
        when (profile.mbtiType) {
            "INTJ", "INTP" -> traits.addAll(listOf("理性", "独立", "追求完美"))
            "ENTJ", "ENTP" -> traits.addAll(listOf("领导力", "创新", "果断"))
            "INFJ", "INFP" -> traits.addAll(listOf("理想主义", "富有同情心", "创造力"))
            "ENFJ", "ENFP" -> traits.addAll(listOf("热情", "善于交际", "富有感染力"))
            "ISTJ", "ISFJ" -> traits.addAll(listOf("可靠", "务实", "有责任感"))
            "ESTJ", "ESFJ" -> traits.addAll(listOf("组织能力强", "传统", "忠诚"))
            "ISTP", "ISFP" -> traits.addAll(listOf("灵活", "实际", "善于观察"))
            "ESTP", "ESFP" -> traits.addAll(listOf("活力充沛", "适应力强", "乐观"))
        }
        
        // 基于星座
        when (profile.zodiacSign) {
            "白羊座" -> traits.addAll(listOf("勇敢", "热情", "冲动"))
            "金牛座" -> traits.addAll(listOf("稳重", "务实", "固执"))
            "双子座" -> traits.addAll(listOf("机智", "善变", "好奇"))
            "巨蟹座" -> traits.addAll(listOf("敏感", "顾家", "情绪化"))
            "狮子座" -> traits.addAll(listOf("自信", "慷慨", "爱面子"))
            "处女座" -> traits.addAll(listOf("细致", "完美主义", "挑剔"))
            "天秤座" -> traits.addAll(listOf("优雅", "犹豫", "追求平衡"))
            "天蝎座" -> traits.addAll(listOf("神秘", "专注", "占有欲强"))
            "射手座" -> traits.addAll(listOf("乐观", "自由", "直率"))
            "摩羯座" -> traits.addAll(listOf("踏实", "有野心", "保守"))
            "水瓶座" -> traits.addAll(listOf("独立", "创新", "理想主义"))
            "双鱼座" -> traits.addAll(listOf("浪漫", "敏感", "富有想象力"))
        }
        
        // 基于生肖
        when (profile.chineseZodiac) {
            "鼠" -> traits.add("机智灵活")
            "牛" -> traits.add("勤劳踏实")
            "虎" -> traits.add("勇猛果敢")
            "兔" -> traits.add("温和谨慎")
            "龙" -> traits.add("气宇轩昂")
            "蛇" -> traits.add("深谋远虑")
            "马" -> traits.add("热情奔放")
            "羊" -> traits.add("温柔善良")
            "猴" -> traits.add("聪明伶俐")
            "鸡" -> traits.add("勤奋认真")
            "狗" -> traits.add("忠诚可靠")
            "猪" -> traits.add("真诚豁达")
        }
        
        return traits.distinct()
    }
    
    /**
     * 计算用户等级
     */
    private fun calculateUserLevel(divinationCount: Int): Int {
        return when {
            divinationCount >= 200 -> 10
            divinationCount >= 150 -> 9
            divinationCount >= 100 -> 8
            divinationCount >= 80 -> 7
            divinationCount >= 60 -> 6
            divinationCount >= 40 -> 5
            divinationCount >= 25 -> 4
            divinationCount >= 15 -> 3
            divinationCount >= 5 -> 2
            else -> 1
        }
    }
    
    /**
     * 分析活跃时段
     */
    private fun analyzeActiveTimeSlot(records: List<DivinationRecord>): String {
        if (records.isEmpty()) return "未知"
        
        val hourCounts = records.groupingBy {
            Calendar.getInstance().apply { time = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }.eachCount()
        
        val mostActiveHour = hourCounts.maxByOrNull { it.value }?.key ?: return "未知"
        
        return when (mostActiveHour) {
            in 6..11 -> "早上 (6:00-12:00)"
            in 12..17 -> "下午 (12:00-18:00)"
            in 18..23 -> "晚上 (18:00-24:00)"
            else -> "凌晨 (0:00-6:00)"
        }
    }
    
    /**
     * 计算连续登录天数
     */
    private fun calculateContinuousLoginDays(records: List<DivinationRecord>): Int {
        if (records.isEmpty()) return 0
        
        val uniqueDays = records
            .map { android.text.format.DateFormat.format("yyyy-MM-dd", it.timestamp).toString() }
            .distinct()
            .sorted()
            .reversed()
        
        if (uniqueDays.isEmpty()) return 0
        
        var continuous = 1
        val today = android.text.format.DateFormat.format("yyyy-MM-dd", Date()).toString()
        
        // 如果今天没有记录，返回0
        if (uniqueDays.first() != today) {
            // 检查是否是昨天
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            val yesterdayStr = android.text.format.DateFormat.format("yyyy-MM-dd", yesterday.time).toString()
            
            if (uniqueDays.first() != yesterdayStr) {
                return 0
            }
        }
        
        // 计算连续天数
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 0 until uniqueDays.size - 1) {
            val currentStr = uniqueDays[i]
            val nextStr = uniqueDays[i + 1]
            
            val currentDate = dateFormat.parse(currentStr) ?: continue
            val nextDate = dateFormat.parse(nextStr) ?: continue
            
            val currentCal = Calendar.getInstance().apply {
                time = currentDate
            }
            
            val nextCal = Calendar.getInstance().apply {
                time = nextDate
            }
            
            val daysDiff = TimeUnit.MILLISECONDS.toDays(currentCal.timeInMillis - nextCal.timeInMillis)
            
            if (daysDiff == 1L) {
                continuous++
            } else {
                break
            }
        }
        
        return continuous
    }
}
