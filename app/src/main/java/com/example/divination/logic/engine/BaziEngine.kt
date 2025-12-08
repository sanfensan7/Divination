package com.example.divination.logic.engine

import android.util.Log
import com.example.divination.logic.engine.model.*
import com.nlf.calendar.Solar
import com.nlf.calendar.Lunar
import com.nlf.calendar.EightChar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 八字排盘引擎
 * 
 * 核心职责：
 * 1. 基于真太阳时计算四柱八字
 * 2. 分析五行分布、纳音、十神
 * 3. 判断格局、用神、大运
 * 4. 输出结构化的 BaziProfile 数据
 * 
 * 技术栈：
 * - lunar-java 1.7.7（6tail 高精度农历库）[待网络正常后集成]
 * - 纯本地算法，无 AI 参与
 * 
 * @author 神机妙算团队
 */
object BaziEngine {
    
    private const val TAG = "BaziEngine"

    /**
     * 在 JVM 单测环境下没有 android.util.Log，这里做安全封装
     */
    private fun logDebug(message: String) {
        runCatching { Log.d(TAG, message) }
    }

    private fun logWarn(message: String, throwable: Throwable? = null) {
        runCatching {
            if (throwable != null) Log.w(TAG, message, throwable) else Log.w(TAG, message)
        }
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        runCatching {
            if (throwable != null) Log.e(TAG, message, throwable) else Log.e(TAG, message)
        }
    }
    
    /**
     * 执行完整的八字排盘
     * 
     * @param inputTime 用户输入的时间
     * @param gender 性别："男" / "女"
     * @param longitude 经度（用于真太阳时校正）
     * @param latitude 纬度
     * @return BaziProfile 完整的八字数据
     */
    fun calculate(
        inputTime: LocalDateTime,
        gender: String,
        longitude: Double = 116.4, // 默认北京经度
        latitude: Double = 39.9    // 默认北京纬度
    ): BaziProfile {
        logDebug("开始八字排盘：$inputTime, 性别=$gender, 经度=$longitude")
        
        try {
            // 1. 计算真太阳时
            val correction = TrueSolarTimeCalculator.getCorrectionDetails(
                inputTime, longitude, latitude
            )
            val trueSolarTime = TrueSolarTimeCalculator.calculateTrueSolarTime(
                inputTime, longitude, latitude
            )
            
            // 2. 使用 lunar-java 进行八字计算
            val solar = Solar.fromYmdHms(
                trueSolarTime.year,
                trueSolarTime.monthValue,
                trueSolarTime.dayOfMonth,
                trueSolarTime.hour,
                trueSolarTime.minute,
                trueSolarTime.second
            )
            val lunar = solar.lunar
            val eightChar = lunar.eightChar
            
            // 3. 构建四柱
            val yearPillar = buildPillar(eightChar.year, eightChar.yearNaYin)
            val monthPillar = buildPillar(eightChar.month, eightChar.monthNaYin)
            val dayPillar = buildPillar(eightChar.day, eightChar.dayNaYin)
            val hourPillar = buildPillar(eightChar.time, eightChar.timeNaYin)
            
            // 4. 分析五行
            val fiveElements = analyzeFiveElements(
                yearPillar, monthPillar, dayPillar, hourPillar
            )
            
            // 5. 计算十神
            val dayMaster = eightChar.dayGan
            val tenGods = calculateTenGods(dayMaster, yearPillar, monthPillar, dayPillar, hourPillar)
            
            // 6. 判断格局和用神
            val pattern = determinePattern(tenGods)
            val strength = determineStrength(dayMaster, fiveElements)
            val usefulGod = determineUsefulGod(dayMaster, strength)
            val joyfulGod = determineJoyfulGod(usefulGod)
            
            // 7. 计算大运
            val currentDayun = calculateCurrentDayun(lunar, gender)
            
            // 8. 获取节气和附加信息
            val currentJieQi = lunar.currentJieQi
            val solarTerm = currentJieQi?.name ?: lunar.prevJieQi?.name ?: "无"
            val jieQiTime = currentJieQi?.solar ?: lunar.prevJieQi?.solar
            val solarTermDistance = if (jieQiTime != null) {
                val days = kotlin.math.abs(solar.day - jieQiTime.day)
                "距${solarTerm}${days}天"
            } else "未知"
            val zodiac = lunar.yearShengXiao
            val constellation = solar.xingZuo
            val lunarDate = "${lunar.yearInChinese}年${lunar.monthInChinese}月${lunar.dayInChinese}"
            
            // 10. 获取当前流年
            val currentYear = LocalDateTime.now().year
            val currentYearGanzhi = getYearGanzhi(currentYear)
            
            logDebug("八字排盘完成：日主=$dayMaster, 格局=$pattern, 五行=${fiveElements.totalSummary}")
            
            return BaziProfile(
                gender = gender,
                inputTime = inputTime,
                trueSolarTime = correction.trueSolarTime,
                longitude = longitude,
                latitude = latitude,
                yearPillar = yearPillar,
                monthPillar = monthPillar,
                dayPillar = dayPillar,
                hourPillar = hourPillar,
                fiveElements = fiveElements,
                naYin = mapOf(
                    "年" to yearPillar.nayin,
                    "月" to monthPillar.nayin,
                    "日" to dayPillar.nayin,
                    "时" to hourPillar.nayin
                ),
                dayMaster = dayMaster,
                dayMasterElement = getGanElement(dayMaster),
                tenGods = tenGods,
                pattern = pattern,
                strength = strength,
                usefulGod = usefulGod,
                joyfulGod = joyfulGod,
                currentDayun = currentDayun,
                currentYear = currentYear,
                currentYearGanzhi = currentYearGanzhi,
                birthSolarTerm = solarTerm,
                solarTermDistance = solarTermDistance,
                zodiac = zodiac,
                constellation = constellation,
                lunarDate = lunarDate
            )
            
        } catch (e: Exception) {
            logError("八字排盘失败", e)
            throw RuntimeException("八字排盘计算失败：${e.message}", e)
        }
    }
    
    /**
     * 构建柱（年/月/日/时）
     */
    private fun buildPillar(ganzhi: String, nayin: String): Pillar {
        val gan = ganzhi.substring(0, 1)
        val zhi = ganzhi.substring(1, 2)
        
        return Pillar(
            gan = gan,
            zhi = zhi,
            ganzhi = ganzhi,
            nayin = nayin,
            ganElement = getGanElement(gan),
            zhiElement = getZhiElement(zhi),
            zhiHidden = getZhiHidden(zhi)
        )
    }
    
    /**
     * 分析五行分布
     */
    private fun analyzeFiveElements(
        yearPillar: Pillar,
        monthPillar: Pillar,
        dayPillar: Pillar,
        hourPillar: Pillar
    ): FiveElements {
        // 统计天干五行
        val tianganCount = mutableMapOf<String, Int>()
        listOf(yearPillar.gan, monthPillar.gan, dayPillar.gan, hourPillar.gan).forEach { gan ->
            val element = getGanElement(gan)
            tianganCount[element] = (tianganCount[element] ?: 0) + 1
        }
        
        // 统计地支本气五行
        val dizhiCount = mutableMapOf<String, Int>()
        listOf(yearPillar.zhi, monthPillar.zhi, dayPillar.zhi, hourPillar.zhi).forEach { zhi ->
            val element = getZhiElement(zhi)
            dizhiCount[element] = (dizhiCount[element] ?: 0) + 1
        }
        
        // 统计地支藏干（简化版，只统计本气）
        val zhiHiddenCount = mutableMapOf<String, Int>()
        listOf(yearPillar, monthPillar, dayPillar, hourPillar).forEach { pillar ->
            pillar.zhiHidden.forEach { hiddenGan ->
                val element = getGanElement(hiddenGan)
                zhiHiddenCount[element] = (zhiHiddenCount[element] ?: 0) + 1
            }
        }
        
        return FiveElements.from(
            tianganCount = tianganCount,
            dizhiCount = dizhiCount,
            zhiHiddenCount = zhiHiddenCount,
            monthElement = monthPillar.ganElement
        )
    }
    
    /**
     * 计算十神（相对于日主）
     */
    private fun calculateTenGods(
        dayMaster: String,
        yearPillar: Pillar,
        monthPillar: Pillar,
        dayPillar: Pillar,
        hourPillar: Pillar
    ): Map<String, String> {
        return mapOf(
            "年干" to getTenGod(dayMaster, yearPillar.gan),
            "年支" to getTenGodFromZhi(dayMaster, yearPillar.zhi),
            "月干" to getTenGod(dayMaster, monthPillar.gan),
            "月支" to getTenGodFromZhi(dayMaster, monthPillar.zhi),
            "日支" to getTenGodFromZhi(dayMaster, dayPillar.zhi),
            "时干" to getTenGod(dayMaster, hourPillar.gan),
            "时支" to getTenGodFromZhi(dayMaster, hourPillar.zhi)
        )
    }
    
    // 方法已移动到文件末尾的简化版本
    
    /**
     * 确定用神（简化版）
     */
    private fun determineUsefulGod(
        dayMaster: String,
        strength: String
    ): String {
        val dayMasterElement = getGanElement(dayMaster)
        
        return if (strength == "身强") {
            // 身强则取克泄耗
            when (dayMasterElement) {
                "木" -> "金（官杀）"
                "火" -> "水（官杀）"
                "土" -> "木（官杀）"
                "金" -> "火（官杀）"
                "水" -> "土（官杀）"
                else -> "未定"
            }
        } else {
            // 身弱则取生扶
            when (dayMasterElement) {
                "木" -> "水（印星）"
                "火" -> "木（印星）"
                "土" -> "火（印星）"
                "金" -> "土（印星）"
                "水" -> "金（印星）"
                else -> "未定"
            }
        }
    }
    
    /**
     * 确定喜神（简化版：用神的生者）
     */
    private fun determineJoyfulGod(usefulGod: String): String {
        val usefulElement = usefulGod.substringBefore("（")
        
        return when (usefulElement) {
            "木" -> "水"
            "火" -> "木"
            "土" -> "火"
            "金" -> "土"
            "水" -> "金"
            else -> "未定"
        }
    }
    
    // 方法已移动到文件末尾的简化版本
    
    // 方法已移动到文件末尾的简化版本
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取天干五行
     */
    private fun getGanElement(gan: String): String {
        return when (gan) {
            "甲", "乙" -> "木"
            "丙", "丁" -> "火"
            "戊", "己" -> "土"
            "庚", "辛" -> "金"
            "壬", "癸" -> "水"
            else -> "未知"
        }
    }
    
    /**
     * 获取地支五行（本气）
     */
    private fun getZhiElement(zhi: String): String {
        return when (zhi) {
            "寅", "卯" -> "木"
            "巳", "午" -> "火"
            "申", "酉" -> "金"
            "亥", "子" -> "水"
            "辰", "戌", "丑", "未" -> "土"
            else -> "未知"
        }
    }
    
    /**
     * 获取地支藏干（简化版，只返回本气）
     */
    private fun getZhiHidden(zhi: String): List<String> {
        return when (zhi) {
            "子" -> listOf("癸")
            "丑" -> listOf("己", "癸", "辛")
            "寅" -> listOf("甲", "丙", "戊")
            "卯" -> listOf("乙")
            "辰" -> listOf("戊", "乙", "癸")
            "巳" -> listOf("丙", "庚", "戊")
            "午" -> listOf("丁", "己")
            "未" -> listOf("己", "丁", "乙")
            "申" -> listOf("庚", "壬", "戊")
            "酉" -> listOf("辛")
            "戌" -> listOf("戊", "辛", "丁")
            "亥" -> listOf("壬", "甲")
            else -> emptyList()
        }
    }
    
    /**
     * 计算十神（天干）
     */
    private fun getTenGod(dayMaster: String, targetGan: String): String {
        if (dayMaster == targetGan) return "比肩"
        
        val dayElement = getGanElement(dayMaster)
        val targetElement = getGanElement(targetGan)
        val dayYinYang = getYinYang(dayMaster)
        val targetYinYang = getYinYang(targetGan)
        val sameYinYang = dayYinYang == targetYinYang
        
        return when {
            // 生我者为印
            generates(targetElement, dayElement) -> if (sameYinYang) "偏印" else "正印"
            // 我生者为食伤
            generates(dayElement, targetElement) -> if (sameYinYang) "食神" else "伤官"
            // 克我者为官杀
            conquers(targetElement, dayElement) -> if (sameYinYang) "七杀" else "正官"
            // 我克者为财
            conquers(dayElement, targetElement) -> if (sameYinYang) "偏财" else "正财"
            // 同我者为比劫
            dayElement == targetElement -> if (sameYinYang) "比肩" else "劫财"
            else -> "未知"
        }
    }
    
    /**
     * 计算十神（地支，取本气）
     */
    private fun getTenGodFromZhi(dayMaster: String, zhi: String): String {
        val zhiHidden = getZhiHidden(zhi)
        return if (zhiHidden.isNotEmpty()) {
            getTenGod(dayMaster, zhiHidden[0])
        } else {
            "未知"
        }
    }
    
    /**
     * 获取阴阳属性
     */
    private fun getYinYang(gan: String): String {
        return when (gan) {
            "甲", "丙", "戊", "庚", "壬" -> "阳"
            "乙", "丁", "己", "辛", "癸" -> "阴"
            else -> "未知"
        }
    }
    
    /**
     * 五行相生关系
     */
    private fun generates(from: String, to: String): Boolean {
        return when (from) {
            "木" -> to == "火"
            "火" -> to == "土"
            "土" -> to == "金"
            "金" -> to == "水"
            "水" -> to == "木"
            else -> false
        }
    }
    
    /**
     * 五行相克关系
     */
    private fun conquers(from: String, to: String): Boolean {
        return when (from) {
            "木" -> to == "土"
            "火" -> to == "金"
            "土" -> to == "水"
            "金" -> to == "木"
            "水" -> to == "火"
            else -> false
        }
    }
    
    // ==================== 大运计算 ====================

    /**
     * 计算当前大运
     */
    private fun calculateCurrentDayun(lunar: Lunar, gender: String): Dayun? {
        return try {
            val yun = lunar.eightChar.getYun(if (gender == "男") 1 else 0)
            val currentAge = LocalDateTime.now().year - lunar.solar.year

            yun.daYun.firstOrNull { dy ->
                val startAge = dy.startAge
                val endAge = startAge + 9
                currentAge in startAge..endAge
            }?.let { dy ->
                val startAge = dy.startAge
                val endAge = startAge + 9
                Dayun(
                    ganzhi = dy.ganZhi,
                    startAge = startAge,
                    endAge = endAge,
                    element = getGanElement(dy.ganZhi.substring(0, 1)),
                    influence = determineDayunInfluence(dy.ganZhi, lunar.eightChar.dayGan)
                )
            }
        } catch (e: Exception) {
            logWarn("大运计算失败", e)
            null
        }
    }
    
    /**
     * 判断大运对命主的影响
     */
    private fun determineDayunInfluence(dayunGanzhi: String, dayMaster: String): String {
        // 简化判断：根据天干五行关系
        val dayunGan = dayunGanzhi.substring(0, 1)
        val dayMasterElement = getGanElement(dayMaster)
        val dayunElement = getGanElement(dayunGan)
        
        return when {
            generates(dayunElement, dayMasterElement) -> "吉"  // 生我
            generates(dayMasterElement, dayunElement) -> "平"  // 我生
            conquers(dayunElement, dayMasterElement) -> "凶"  // 克我
            conquers(dayMasterElement, dayunElement) -> "平"  // 我克
            dayMasterElement == dayunElement -> "平"          // 同类
            else -> "平"
        }
    }
    
    /**
     * 星座计算（备用，lunar-java 已提供）
     */
    private fun getConstellation(month: Int, day: Int): String {
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "白羊座"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "金牛座"
            (month == 5 && day >= 21) || (month == 6 && day <= 21) -> "双子座"
            (month == 6 && day >= 22) || (month == 7 && day <= 22) -> "巨蟹座"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "狮子座"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "处女座"
            (month == 9 && day >= 23) || (month == 10 && day <= 23) -> "天秤座"
            (month == 10 && day >= 24) || (month == 11 && day <= 22) -> "天蝎座"
            (month == 11 && day >= 23) || (month == 12 && day <= 21) -> "射手座"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "摩羯座"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "水瓶座"
            else -> "双鱼座"
        }
    }
    
    /**
     * 年份干支计算（直接使用 lunar-java）
     */
    private fun getYearGanzhi(year: Int): String {
        val gan = arrayOf("庚", "辛", "壬", "癸", "甲", "乙", "丙", "丁", "戊", "己")
        val zhi = arrayOf("申", "酉", "戌", "亥", "子", "丑", "寅", "卯", "辰", "巳", "午", "未")
        val ganIndex = (year - 4) % 10
        val zhiIndex = (year - 4) % 12
        return "${gan[ganIndex]}${zhi[zhiIndex]}"
    }
    
    /**
     * 格局判断（基于十神）
     */
    private fun determinePattern(
        tenGods: Map<String, String>
    ): String {
        val monthGod = tenGods["月干"] ?: "未知"
        
        return when {
            monthGod.contains("正官") -> "正官格"
            monthGod.contains("偏官") || monthGod.contains("七杀") -> "七杀格"
            monthGod.contains("正财") -> "正财格"
            monthGod.contains("偏财") -> "偏财格"
            monthGod.contains("正印") -> "正印格"
            monthGod.contains("偏印") -> "偏印格"
            monthGod.contains("食神") -> "食神格"
            monthGod.contains("伤官") -> "伤官格"
            monthGod.contains("比肩") -> "建禄格"
            monthGod.contains("劫财") -> "羊刃格"
            else -> "普通格局"
        }
    }
    
    /**
     * 身强身弱判断
     */
    private fun determineStrength(dayMaster: String, fiveElements: FiveElements): String {
        val dayMasterElement = getGanElement(dayMaster)
        val dayMasterCount = fiveElements.totalCount[dayMasterElement] ?: 0
        
        return if (dayMasterCount >= 3) "身强" else "身弱"
    }
}
