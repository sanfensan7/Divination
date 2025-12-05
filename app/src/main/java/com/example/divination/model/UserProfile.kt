package com.example.divination.model

import java.util.*

/**
 * 用户信息数据模型
 */
data class UserProfile(
    val name: String = "",                    // 姓名
    val gender: String = "",                  // 性别：男/女
    val birthDate: String = "",               // 出生日期：yyyy-MM-dd
    val birthTime: String = "",               // 出生时间：HH:mm
    val birthPlace: String = "",              // 出生地点
    val zodiacSign: String = "",              // 星座
    val chineseZodiac: String = "",           // 生肖
    val bloodType: String = "",               // 血型：A/B/O/AB
    val mbtiType: String = "",                // MBTI人格类型
    val luckyNumber: Int = 0,                 // 幸运数字
    val luckyColor: String = "",              // 幸运颜色
    val phone: String = "",                   // 手机号（可选）
    val email: String = "",                   // 邮箱（可选）
    val createdTime: Date = Date(),           // 创建时间
    val updatedTime: Date = Date()            // 更新时间
) {
    /**
     * 计算年龄
     */
    fun getAge(): Int {
        if (birthDate.isEmpty()) return 0
        
        try {
            val parts = birthDate.split("-")
            if (parts.size != 3) return 0
            
            val birthYear = parts[0].toInt()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            return currentYear - birthYear
        } catch (e: Exception) {
            return 0
        }
    }
    
    /**
     * 检查信息是否完整
     */
    fun isComplete(): Boolean {
        return name.isNotEmpty() && 
               gender.isNotEmpty() && 
               birthDate.isNotEmpty()
    }
    
    /**
     * 获取显示名称
     */
    fun getDisplayName(): String {
        return if (name.isNotEmpty()) name else "未设置"
    }
}
