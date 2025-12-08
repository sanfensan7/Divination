package com.example.divination.logic.engine.model

import java.time.LocalDateTime

/**
 * 八字排盘完整数据结构
 * 
 * 本数据类包含所有通过本地算法计算出的硬数据，
 * 这些数据将被序列化后发送给 AI，确保 AI 只做解读而不参与计算。
 */
data class BaziProfile(
    // === 基础信息 ===
    val gender: String,                          // 性别："男" / "女"
    val inputTime: LocalDateTime,                // 用户输入的原始时间
    val trueSolarTime: LocalDateTime,            // 真太阳时校正后的时间
    val longitude: Double,                       // 经度（用于真太阳时计算）
    val latitude: Double,                        // 纬度（备用，紫微斗数可能需要）
    
    // === 四柱八字 ===
    val yearPillar: Pillar,                      // 年柱（年干支）
    val monthPillar: Pillar,                     // 月柱（月干支，基于节气）
    val dayPillar: Pillar,                       // 日柱（日干支）
    val hourPillar: Pillar,                      // 时柱（时干支）
    
    // === 五行分析 ===
    val fiveElements: FiveElements,              // 五行统计与分析
    
    // === 命理要素 ===
    val naYin: Map<String, String>,              // 纳音五行：{"年":"路旁土", "月":"白蜡金", ...}
    val dayMaster: String,                       // 日主（日干）
    val dayMasterElement: String,                // 日主五行属性
    val tenGods: Map<String, String>,            // 十神关系（相对于日主）
    
    // === 格局判断 ===
    val pattern: String,                         // 命局格局（如"正官格"、"七杀格"）
    val strength: String,                        // 身强/身弱判断
    val usefulGod: String,                       // 用神
    val joyfulGod: String,                       // 喜神
    
    // === 大运流年 ===
    val currentDayun: Dayun?,                    // 当前大运（10年一运）
    val currentYear: Int,                        // 当前年份
    val currentYearGanzhi: String,               // 当前流年干支
    
    // === 节气信息 ===
    val birthSolarTerm: String,                  // 出生时的节气
    val solarTermDistance: String,               // 距离节气的时间（用于判断月柱）
    
    // === 附加信息 ===
    val zodiac: String,                          // 生肖
    val constellation: String,                   // 星座
    val lunarDate: String,                       // 农历日期
    
    // === 计算时间戳 ===
    val calculationTime: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 转换为适合发给 AI 的结构化文本
     */
    fun toPromptString(): String {
        return """
【命主基本信息】
性别：$gender
公历生辰：${inputTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))}
真太阳时：${trueSolarTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))}（经度${longitude}°校正）
农历生辰：$lunarDate
生肖：$zodiac  |  星座：$constellation

【四柱八字】
年柱：${yearPillar.ganzhi}（${yearPillar.nayin}）
月柱：${monthPillar.ganzhi}（${monthPillar.nayin}）
日柱：${dayPillar.ganzhi}（${dayPillar.nayin}）
时柱：${hourPillar.ganzhi}（${hourPillar.nayin}）

【五行分析】
天干：${fiveElements.tianganSummary}
地支：${fiveElements.dizhiSummary}
总计：${fiveElements.totalSummary}
缺失五行：${fiveElements.missing.ifEmpty { "无" }}
旺相休囚死：${fiveElements.seasonalStrength}

【命理要素】
日主：$dayMaster（$dayMasterElement）
身强身弱：$strength
格局：$pattern
用神：$usefulGod  |  喜神：$joyfulGod

【十神配置】
年干：${tenGods["年干"]}  |  年支：${tenGods["年支"]}
月干：${tenGods["月干"]}  |  月支：${tenGods["月支"]}
日支：${tenGods["日支"]}
时干：${tenGods["时干"]}  |  时支：${tenGods["时支"]}

【大运流年】
${if (currentDayun != null) "当前大运：${currentDayun.ganzhi}（${currentDayun.startAge}-${currentDayun.endAge}岁）" else "大运信息未计算"}
当前流年：${currentYear}年（${currentYearGanzhi}）

【节气环境】
出生节气：$birthSolarTerm
节气位置：$solarTermDistance
        """.trimIndent()
    }
    
    /**
     * 转换为 JSON 结构（用于存储或 API 传输）
     */
    fun toJson(): String {
        return com.google.gson.Gson().toJson(this)
    }
}

/**
 * 天干地支柱（年/月/日/时）
 */
data class Pillar(
    val gan: String,              // 天干（如"甲"）
    val zhi: String,              // 地支（如"子"）
    val ganzhi: String,           // 干支组合（如"甲子"）
    val nayin: String,            // 纳音（如"海中金"）
    val ganElement: String,       // 天干五行
    val zhiElement: String,       // 地支本气五行
    val zhiHidden: List<String>   // 地支藏干
)

/**
 * 大运信息（10年一运）
 */
data class Dayun(
    val ganzhi: String,           // 大运干支
    val startAge: Int,            // 起运年龄
    val endAge: Int,              // 结束年龄
    val element: String,          // 大运五行
    val influence: String         // 对命主的影响（吉/凶/平）
)
