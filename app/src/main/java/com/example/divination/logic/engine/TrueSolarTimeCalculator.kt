package com.example.divination.logic.engine

import com.example.divination.logic.engine.model.TimeCorrection
import java.time.LocalDateTime

/**
 * 真太阳时计算器
 * 
 * 核心职责：
 * 1. 根据用户输入的时间和经纬度，计算真太阳时
 * 2. 将真太阳时转换为 lunar-java 库所需的 Solar 对象
 * 3. 确保所有命理计算都基于真太阳时，而非标准北京时间
 * 
 * 原理：
 * - 平太阳时：根据经度偏移校正（经度每差1度 = 时间差4分钟）
 * - 真太阳时：在平太阳时基础上再加上"均时差"（地球公转导致的时间差）
 * 
 * @author 神机妙算团队
 */
object TrueSolarTimeCalculator {
    
    /**
     * 计算真太阳时并返回 Solar 对象
     * 
     * @param inputTime 用户输入的时间（LocalDateTime）
     * @param longitude 经度（东经为正，西经为负，如北京=116.4，上海=121.5）
     * @param latitude 纬度（北纬为正，南纬为负）
     * @return Solar 对象，包含真太阳时信息
     */
    fun calculateTrueSolarTime(
        inputTime: LocalDateTime,
        longitude: Double,
        latitude: Double
    ): LocalDateTime {
        // 1. 执行真太阳时校正
        val correction = TimeCorrection.calculate(inputTime, longitude, latitude)
        
        // 2. 返回校正后的时间
        return correction.trueSolarTime
    }
    
    /**
     * 计算真太阳时校正详情
     * 
     * @param inputTime 用户输入的时间
     * @param longitude 经度
     * @param latitude 纬度
     * @return 校正详情（包含偏移量、时辰变化等信息）
     */
    fun getCorrectionDetails(
        inputTime: LocalDateTime,
        longitude: Double,
        latitude: Double
    ): TimeCorrection {
        return TimeCorrection.calculate(inputTime, longitude, latitude)
    }
    
    /**
     * 快速判断是否需要校正
     * 
     * @param longitude 经度
     * @return 如果经度接近120°（±0.5°以内），可忽略校正
     */
    fun needsCorrection(longitude: Double): Boolean {
        return Math.abs(longitude - 120.0) > 0.5
    }
    
    /**
     * 批量计算（用于大运排盘等场景）
     * 
     * @param baseTime 基准时间
     * @param longitude 经度
     * @param latitude 纬度
     * @param yearOffsets 年份偏移列表（如[0, 10, 20, 30...]表示大运）
     * @return Solar 对象列表
     */
    fun calculateBatch(
        baseTime: LocalDateTime,
        longitude: Double,
        latitude: Double,
        yearOffsets: List<Int>
    ): List<LocalDateTime> {
        return yearOffsets.map { offset ->
            val targetTime = baseTime.plusYears(offset.toLong())
            calculateTrueSolarTime(targetTime, longitude, latitude)
        }
    }
}
