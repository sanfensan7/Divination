package com.example.divination.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.divination.model.DailyFortune
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * 运势历史存储服务
 */
object FortuneHistoryService {
    
    private const val PREFS_NAME = "fortune_history_prefs"
    private const val KEY_HISTORY = "history"
    private const val MAX_HISTORY_DAYS = 90  // 最多保存90天
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * 保存今日运势
     */
    fun saveTodayFortune(context: Context, fortune: DailyFortune) {
        val history = getAllHistory(context).toMutableList()
        
        // 移除今天已有的记录
        history.removeAll { it.date == fortune.date }
        
        // 添加新记录
        history.add(0, fortune)
        
        // 限制历史数量
        if (history.size > MAX_HISTORY_DAYS) {
            history.subList(MAX_HISTORY_DAYS, history.size).clear()
        }
        
        val json = gson.toJson(history)
        getPrefs(context).edit().putString(KEY_HISTORY, json).apply()
    }
    
    /**
     * 获取所有历史
     */
    fun getAllHistory(context: Context): List<DailyFortune> {
        val json = getPrefs(context).getString(KEY_HISTORY, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<DailyFortune>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 获取最近N天的历史
     */
    fun getRecentHistory(context: Context, days: Int): List<DailyFortune> {
        return getAllHistory(context).take(days)
    }
    
    /**
     * 获取指定日期的运势
     */
    fun getFortuneByDate(context: Context, date: String): DailyFortune? {
        return getAllHistory(context).find { it.date == date }
    }
    
    /**
     * 获取今日运势
     */
    fun getTodayFortune(context: Context): DailyFortune? {
        val today = dateFormat.format(Date())
        return getFortuneByDate(context, today)
    }
    
    /**
     * 清除所有历史
     */
    fun clearAllHistory(context: Context) {
        getPrefs(context).edit().remove(KEY_HISTORY).apply()
    }
    
    /**
     * 获取SharedPreferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
