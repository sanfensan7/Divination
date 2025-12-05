package com.example.divination.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.divination.model.UserProfile
import com.google.gson.Gson
import java.util.*

/**
 * 用户信息存储服务
 */
object UserProfileService {
    
    private const val PREFS_NAME = "user_profile_prefs"
    private const val KEY_USER_PROFILE = "user_profile"
    
    private val gson = Gson()
    
    /**
     * 保存用户信息
     */
    fun saveUserProfile(context: Context, profile: UserProfile) {
        val prefs = getPrefs(context)
        val updatedProfile = profile.copy(updatedTime = Date())
        val json = gson.toJson(updatedProfile)
        prefs.edit().putString(KEY_USER_PROFILE, json).apply()
    }
    
    /**
     * 获取用户信息
     */
    fun getUserProfile(context: Context): UserProfile? {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_USER_PROFILE, null) ?: return null
        
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 检查用户信息是否存在
     */
    fun hasUserProfile(context: Context): Boolean {
        return getUserProfile(context) != null
    }
    
    /**
     * 清除用户信息
     */
    fun clearUserProfile(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().remove(KEY_USER_PROFILE).apply()
    }
    
    /**
     * 获取SharedPreferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 根据出生日期计算星座
     */
    fun calculateZodiacSign(birthDate: String): String {
        try {
            val parts = birthDate.split("-")
            if (parts.size != 3) return ""
            
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            
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
                (month == 2 && day >= 19) || (month == 3 && day <= 20) -> "双鱼座"
                else -> ""
            }
        } catch (e: Exception) {
            return ""
        }
    }
    
    /**
     * 根据出生年份计算生肖
     */
    fun calculateChineseZodiac(birthDate: String): String {
        try {
            val parts = birthDate.split("-")
            if (parts.size != 3) return ""
            
            val year = parts[0].toInt()
            val zodiacs = arrayOf("猴", "鸡", "狗", "猪", "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊")
            
            return zodiacs[year % 12]
        } catch (e: Exception) {
            return ""
        }
    }
}
