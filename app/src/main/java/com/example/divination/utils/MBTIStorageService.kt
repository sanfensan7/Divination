package com.example.divination.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.divination.model.MBTIResult
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * MBTI本地存储服务
 * 负责保存和读取MBTI测试历史记录
 */
class MBTIStorageService(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    /**
     * 保存MBTI测试结果
     */
    fun saveResult(result: MBTIResult): Boolean {
        return try {
            val results = getAllResults().toMutableList()
            results.add(0, result) // 最新的在前面
            
            // 限制最多保存的记录数
            if (results.size > MAX_HISTORY_COUNT) {
                results.subList(MAX_HISTORY_COUNT, results.size).clear()
            }
            
            val jsonArray = JSONArray()
            results.forEach { jsonArray.put(it.toJson()) }
            
            prefs.edit()
                .putString(KEY_RESULTS, jsonArray.toString())
                .putLong(KEY_LAST_TEST_DATE, result.testDate)
                .putString(KEY_LAST_RESULT, result.personalityType)
                .apply()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取所有测试结果
     */
    fun getAllResults(): List<MBTIResult> {
        return try {
            val jsonString = prefs.getString(KEY_RESULTS, null) ?: return emptyList()
            val jsonArray = JSONArray(jsonString)
            val results = mutableListOf<MBTIResult>()
            
            for (i in 0 until jsonArray.length()) {
                try {
                    val result = MBTIResult.fromJson(jsonArray.getJSONObject(i))
                    results.add(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 获取最新的测试结果
     */
    fun getLatestResult(): MBTIResult? {
        return getAllResults().firstOrNull()
    }
    
    /**
     * 根据索引获取测试结果
     */
    fun getResultByIndex(index: Int): MBTIResult? {
        val results = getAllResults()
        return if (index in results.indices) results[index] else null
    }
    
    /**
     * 删除指定的测试结果
     */
    fun deleteResult(testDate: Long): Boolean {
        return try {
            val results = getAllResults().toMutableList()
            results.removeIf { it.testDate == testDate }
            
            val jsonArray = JSONArray()
            results.forEach { jsonArray.put(it.toJson()) }
            
            val editor = prefs.edit().putString(KEY_RESULTS, jsonArray.toString())
            
            // 如果删除的是最新记录，更新最新记录信息
            if (results.isNotEmpty()) {
                editor.putLong(KEY_LAST_TEST_DATE, results[0].testDate)
                editor.putString(KEY_LAST_RESULT, results[0].personalityType)
            } else {
                editor.remove(KEY_LAST_TEST_DATE)
                editor.remove(KEY_LAST_RESULT)
            }
            
            editor.apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 清空所有测试记录
     */
    fun clearAllResults(): Boolean {
        return try {
            prefs.edit()
                .remove(KEY_RESULTS)
                .remove(KEY_LAST_TEST_DATE)
                .remove(KEY_LAST_RESULT)
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取测试历史数量
     */
    fun getResultCount(): Int {
        return getAllResults().size
    }
    
    /**
     * 获取上次测试日期
     */
    fun getLastTestDate(): Long {
        return prefs.getLong(KEY_LAST_TEST_DATE, 0L)
    }
    
    /**
     * 获取上次测试结果类型
     */
    fun getLastResultType(): String? {
        return prefs.getString(KEY_LAST_RESULT, null)
    }
    
    /**
     * 格式化日期
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
        return sdf.format(Date(timestamp))
    }
    
    /**
     * 保存当前答题进度
     */
    fun saveProgress(questionId: Int, answers: String) {
        prefs.edit()
            .putInt(KEY_CURRENT_QUESTION, questionId)
            .putString(KEY_CURRENT_ANSWERS, answers)
            .putLong(KEY_PROGRESS_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * 获取答题进度
     */
    fun getProgress(): Pair<Int, String?> {
        val questionId = prefs.getInt(KEY_CURRENT_QUESTION, 0)
        val answers = prefs.getString(KEY_CURRENT_ANSWERS, null)
        return Pair(questionId, answers)
    }
    
    /**
     * 清除答题进度
     */
    fun clearProgress() {
        prefs.edit()
            .remove(KEY_CURRENT_QUESTION)
            .remove(KEY_CURRENT_ANSWERS)
            .remove(KEY_PROGRESS_TIMESTAMP)
            .apply()
    }
    
    /**
     * 检查是否有未完成的测试
     */
    fun hasUnfinishedTest(): Boolean {
        val timestamp = prefs.getLong(KEY_PROGRESS_TIMESTAMP, 0L)
        if (timestamp == 0L) return false
        
        // 如果进度超过24小时，视为过期
        val hoursSinceProgress = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60)
        return hoursSinceProgress < 24
    }
    
    companion object {
        private const val PREFS_NAME = "mbti_storage"
        private const val KEY_RESULTS = "results"
        private const val KEY_LAST_TEST_DATE = "last_test_date"
        private const val KEY_LAST_RESULT = "last_result"
        private const val KEY_CURRENT_QUESTION = "current_question"
        private const val KEY_CURRENT_ANSWERS = "current_answers"
        private const val KEY_PROGRESS_TIMESTAMP = "progress_timestamp"
        private const val MAX_HISTORY_COUNT = 50 // 最多保存50条记录
        
        @Volatile
        private var instance: MBTIStorageService? = null
        
        fun getInstance(context: Context): MBTIStorageService {
            return instance ?: synchronized(this) {
                instance ?: MBTIStorageService(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

