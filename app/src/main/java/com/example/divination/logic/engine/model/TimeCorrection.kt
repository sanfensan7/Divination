package com.example.divination.logic.engine.model

import java.time.LocalDateTime

/**
 * 真太阳时校正结果
 * 
 * 记录从标准时间到真太阳时的转换过程和结果
 */
data class TimeCorrection(
    // === 输入信息 ===
    val inputTime: LocalDateTime,        // 用户输入的原始时间（北京时间或当地时间）
    val longitude: Double,               // 经度（东经为正，西经为负）
    val latitude: Double,                // 纬度（北纬为正，南纬为负）
    
    // === 校正过程 ===
    val meanSolarOffset: Int,            // 平太阳时偏移（分钟）= (经度 - 120) * 4
    val equationOfTime: Double,          // 均时差（分钟）基于日期计算
    val totalOffset: Int,                // 总偏移（分钟）= 平太阳时偏移 + 均时差
    
    // === 校正结果 ===
    val trueSolarTime: LocalDateTime,    // 真太阳时（最终用于排盘的时间）
    val hourChange: Boolean,             // 是否导致时辰变化
    val originalHour: String,            // 原始时辰（如"午时"）
    val correctedHour: String,           // 校正后时辰（如"未时"）
    
    // === 提示信息 ===
    val correctionNote: String           // 校正说明（用于展示给用户）
) {
    companion object {
        /**
         * 执行真太阳时校正
         * 
         * @param inputTime 用户输入的时间
         * @param longitude 经度（东经为正）
         * @param latitude 纬度（北纬为正）
         * @return 校正结果
         */
        fun calculate(
            inputTime: LocalDateTime,
            longitude: Double,
            latitude: Double
        ): TimeCorrection {
            // 1. 计算平太阳时偏移（经度每差1度，时间差4分钟）
            // 以东经120度（北京时间基准）为参考
            val meanSolarOffset = ((longitude - 120.0) * 4).toInt()
            
            // 2. 计算均时差（Equation of Time）
            // 简化算法：基于儒略日和太阳黄经
            val equationOfTime = calculateEquationOfTime(inputTime)
            
            // 3. 总偏移
            val totalOffset = meanSolarOffset + equationOfTime.toInt()
            
            // 4. 计算真太阳时
            val trueSolarTime = inputTime.plusMinutes(totalOffset.toLong())
            
            // 5. 判断时辰是否变化
            val originalHour = getChineseHour(inputTime.hour)
            val correctedHour = getChineseHour(trueSolarTime.hour)
            val hourChange = originalHour != correctedHour
            
            // 6. 生成校正说明
            val correctionNote = buildCorrectionNote(
                longitude, meanSolarOffset, equationOfTime, totalOffset, hourChange
            )
            
            return TimeCorrection(
                inputTime = inputTime,
                longitude = longitude,
                latitude = latitude,
                meanSolarOffset = meanSolarOffset,
                equationOfTime = equationOfTime,
                totalOffset = totalOffset,
                trueSolarTime = trueSolarTime,
                hourChange = hourChange,
                originalHour = originalHour,
                correctedHour = correctedHour,
                correctionNote = correctionNote
            )
        }
        
        /**
         * 计算均时差（简化版）
         * 
         * 均时差是地球椭圆轨道和地轴倾斜导致的真太阳时与平太阳时的差异
         * 范围约为 -16 到 +14 分钟
         */
        private fun calculateEquationOfTime(time: LocalDateTime): Double {
            val dayOfYear = time.dayOfYear
            
            // 使用简化的傅里叶级数近似（精度约±2分钟）
            val B = 2 * Math.PI * (dayOfYear - 81) / 365.0
            
            val eot = 9.87 * Math.sin(2 * B) - 7.53 * Math.cos(B) - 1.5 * Math.sin(B)
            
            return eot
        }
        
        /**
         * 获取中国传统时辰
         */
        private fun getChineseHour(hour: Int): String {
            return when (hour) {
                23, 0 -> "子时"
                1, 2 -> "丑时"
                3, 4 -> "寅时"
                5, 6 -> "卯时"
                7, 8 -> "辰时"
                9, 10 -> "巳时"
                11, 12 -> "午时"
                13, 14 -> "未时"
                15, 16 -> "申时"
                17, 18 -> "酉时"
                19, 20 -> "戌时"
                21, 22 -> "亥时"
                else -> "未知"
            }
        }
        
        /**
         * 生成校正说明
         */
        private fun buildCorrectionNote(
            longitude: Double,
            meanSolarOffset: Int,
            equationOfTime: Double,
            totalOffset: Int,
            hourChange: Boolean
        ): String {
            val direction = if (longitude > 120) "东" else "西"
            val distance = Math.abs(longitude - 120)
            
            return buildString {
                append("您所在经度为${String.format("%.2f", longitude)}°")
                append("（北京时间基准东经120°${direction}偏${String.format("%.2f", distance)}°），")
                append("平太阳时需${if (meanSolarOffset > 0) "加" else "减"}${Math.abs(meanSolarOffset)}分钟。\n")
                append("考虑地球公转均时差（${String.format("%+.1f", equationOfTime)}分钟），")
                append("真太阳时共${if (totalOffset > 0) "快" else "慢"}${Math.abs(totalOffset)}分钟。\n")
                if (hourChange) {
                    append("⚠️ 校正后导致时辰变化，以真太阳时为准进行排盘。")
                } else {
                    append("✓ 校正后时辰未变化。")
                }
            }
        }
    }
}
