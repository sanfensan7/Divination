package com.example.divination.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.divination.model.DivinationRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * 算命记录存储服务
 */
object DivinationRecordService {
    
    private const val PREFS_NAME = "divination_records_prefs"
    private const val KEY_RECORDS = "records"
    private const val MAX_RECORDS = 1000  // 最多保存1000条记录
    
    private val gson = Gson()
    
    /**
     * 保存算命记录
     */
    fun saveRecord(context: Context, record: DivinationRecord) {
        val records = getAllRecords(context).toMutableList()
        records.add(0, record)  // 添加到开头
        
        // 限制记录数量
        if (records.size > MAX_RECORDS) {
            records.subList(MAX_RECORDS, records.size).clear()
        }
        
        val json = gson.toJson(records)
        getPrefs(context).edit().putString(KEY_RECORDS, json).apply()
    }
    
    /**
     * 获取所有记录
     */
    fun getAllRecords(context: Context): List<DivinationRecord> {
        val json = getPrefs(context).getString(KEY_RECORDS, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<DivinationRecord>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 获取最近N条记录
     */
    fun getRecentRecords(context: Context, count: Int): List<DivinationRecord> {
        return getAllRecords(context).take(count)
    }
    
    /**
     * 获取指定时间范围内的记录
     */
    fun getRecordsByDateRange(context: Context, startDate: Date, endDate: Date): List<DivinationRecord> {
        return getAllRecords(context).filter {
            it.timestamp.after(startDate) && it.timestamp.before(endDate)
        }
    }
    
    /**
     * 获取指定方法的记录
     */
    fun getRecordsByMethod(context: Context, methodId: String): List<DivinationRecord> {
        return getAllRecords(context).filter { it.methodId == methodId }
    }
    
    /**
     * 清除所有记录
     */
    fun clearAllRecords(context: Context) {
        getPrefs(context).edit().remove(KEY_RECORDS).apply()
    }
    
    /**
     * 获取SharedPreferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
