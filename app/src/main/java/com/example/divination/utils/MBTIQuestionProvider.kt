package com.example.divination.utils

import android.content.Context
import com.example.divination.R
import com.example.divination.model.MBTIQuestion
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * MBTI题库管理服务
 * 负责加载和提供测试题目
 */
class MBTIQuestionProvider(private val context: Context) {
    
    private var questions: List<MBTIQuestion> = emptyList()
    private var version: String = "1.0.0"
    
    /**
     * 加载题库
     */
    fun loadQuestions(): List<MBTIQuestion> {
        if (questions.isNotEmpty()) {
            return questions
        }
        
        try {
            val inputStream = context.resources.openRawResource(R.raw.mbti_questions)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            val jsonObject = JSONObject(jsonString)
            version = jsonObject.optString("version", "1.0.0")
            
            val questionsArray = jsonObject.getJSONArray("questions")
            val questionsList = mutableListOf<MBTIQuestion>()
            
            for (i in 0 until questionsArray.length()) {
                val questionJson = questionsArray.getJSONObject(i)
                questionsList.add(MBTIQuestion.fromJson(questionJson))
            }
            
            questions = questionsList
            return questions
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    /**
     * 获取题库版本
     */
    fun getVersion(): String = version
    
    /**
     * 获取总题目数量
     */
    fun getTotalQuestions(): Int = questions.size
    
    /**
     * 根据ID获取题目
     */
    fun getQuestionById(id: Int): MBTIQuestion? {
        return questions.find { it.id == id }
    }
    
    /**
     * 按维度获取题目列表
     */
    fun getQuestionsByDimension(dimension: MBTIQuestion.Dimension): List<MBTIQuestion> {
        return questions.filter { it.dimension == dimension }
    }
    
    /**
     * 获取所有题目（确保已加载）
     */
    fun getAllQuestions(): List<MBTIQuestion> {
        if (questions.isEmpty()) {
            loadQuestions()
        }
        return questions
    }
    
    companion object {
        @Volatile
        private var instance: MBTIQuestionProvider? = null
        
        fun getInstance(context: Context): MBTIQuestionProvider {
            return instance ?: synchronized(this) {
                instance ?: MBTIQuestionProvider(context.applicationContext).also {
                    instance = it
                    it.loadQuestions()
                }
            }
        }
    }
}

