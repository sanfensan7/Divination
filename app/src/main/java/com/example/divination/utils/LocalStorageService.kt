package com.example.divination.utils

import android.content.Context
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

/**
 * 本地存储服务
 */
object LocalStorageService {
    
    private const val RESULTS_DIR = "results"
    private const val PREFS_NAME = "divination_prefs"
    
    /**
     * 保存算命结果
     */
    fun saveResult(context: Context, result: DivinationResult): Boolean {
        try {
            // 创建目录
            val resultDir = File(context.filesDir, RESULTS_DIR)
            if (!resultDir.exists()) {
                resultDir.mkdirs()
            }
            
            // 将结果转换为JSON
            val gson = Gson()
            val json = gson.toJson(result)
            
            // 保存到文件
            val file = File(resultDir, "${result.id}.json")
            file.writeText(json)
            
            // 更新结果索引
            updateResultIndex(context, result.id)
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 获取算命结果
     */
    fun getResult(context: Context, resultId: String): DivinationResult? {
        try {
            // 读取文件
            val file = File(File(context.filesDir, RESULTS_DIR), "$resultId.json")
            if (!file.exists()) {
                return null
            }
            
            // 解析JSON
            val json = file.readText()
            val gson = Gson()
            return gson.fromJson(json, DivinationResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 获取所有结果ID
     */
    fun getAllResultIds(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("result_ids", "[]") ?: "[]"
        val gson = Gson()
        return gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }
    
    /**
     * 获取所有算命结果
     */
    fun getAllResults(context: Context): List<DivinationResult> {
        val resultIds = getAllResultIds(context)
        return resultIds.mapNotNull { getResult(context, it) }
    }
    
    /**
     * 删除算命结果
     */
    fun deleteResult(context: Context, resultId: String): Boolean {
        try {
            // 删除文件
            val file = File(File(context.filesDir, RESULTS_DIR), "$resultId.json")
            if (file.exists()) {
                file.delete()
            }
            
            // 更新结果索引
            val resultIds = getAllResultIds(context).toMutableList()
            resultIds.remove(resultId)
            
            val gson = Gson()
            val json = gson.toJson(resultIds)
            
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("result_ids", json).apply()
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 更新结果索引
     */
    private fun updateResultIndex(context: Context, resultId: String) {
        val resultIds = getAllResultIds(context).toMutableList()
        if (!resultIds.contains(resultId)) {
            resultIds.add(resultId)
            
            val gson = Gson()
            val json = gson.toJson(resultIds)
            
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("result_ids", json).apply()
        }
    }
    
    /**
     * 保存API密钥
     */
    fun saveApiKey(context: Context, apiKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("api_key", apiKey).apply()
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("api_key", "") ?: ""
    }
} 