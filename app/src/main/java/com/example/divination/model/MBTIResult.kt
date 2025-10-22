package com.example.divination.model

import org.json.JSONObject
import java.io.Serializable

/**
 * MBTI测试结果
 * 
 * @property personalityType 四字母人格类型代码 (如: INTJ, ENFP)
 * @property eiScore E/I维度得分 (正数偏E, 负数偏I)
 * @property snScore S/N维度得分 (正数偏N, 负数偏S)
 * @property tfScore T/F维度得分 (正数偏F, 负数偏T)
 * @property jpScore J/P维度得分 (正数偏P, 负数偏J)
 * @property testDate 测试日期时间戳
 * @property version 题库版本
 */
data class MBTIResult(
    val personalityType: String,
    val eiScore: Int,
    val snScore: Int,
    val tfScore: Int,
    val jpScore: Int,
    val testDate: Long = System.currentTimeMillis(),
    val version: String = "1.0.0",
    val answers: List<MBTIAnswer> = emptyList()
): Serializable {
    /**
     * 获取E/I维度百分比 (0-100, 数值越大越偏E)
     */
    fun getEIPercentage(): Int {
        val maxScore = 45 // 15题 * 3分
        return ((eiScore + maxScore) * 100 / (2 * maxScore)).coerceIn(0, 100)
    }
    
    /**
     * 获取S/N维度百分比 (0-100, 数值越大越偏N)
     */
    fun getSNPercentage(): Int {
        val maxScore = 45
        return ((snScore + maxScore) * 100 / (2 * maxScore)).coerceIn(0, 100)
    }
    
    /**
     * 获取T/F维度百分比 (0-100, 数值越大越偏F)
     */
    fun getTFPercentage(): Int {
        val maxScore = 45
        return ((tfScore + maxScore) * 100 / (2 * maxScore)).coerceIn(0, 100)
    }
    
    /**
     * 获取J/P维度百分比 (0-100, 数值越大越偏P)
     */
    fun getJPPercentage(): Int {
        val maxScore = 45
        return ((jpScore + maxScore) * 100 / (2 * maxScore)).coerceIn(0, 100)
    }
    
    /**
     * 获取维度标签 (如: 67% 外向)
     */
    fun getEILabel(): String {
        val percentage = getEIPercentage()
        return if (percentage >= 50) {
            "${percentage}% 外向 (E)"
        } else {
            "${100 - percentage}% 内向 (I)"
        }
    }
    
    fun getSNLabel(): String {
        val percentage = getSNPercentage()
        return if (percentage >= 50) {
            "${percentage}% 直觉 (N)"
        } else {
            "${100 - percentage}% 感觉 (S)"
        }
    }
    
    fun getTFLabel(): String {
        val percentage = getTFPercentage()
        return if (percentage >= 50) {
            "${percentage}% 情感 (F)"
        } else {
            "${100 - percentage}% 思考 (T)"
        }
    }
    
    fun getJPLabel(): String {
        val percentage = getJPPercentage()
        return if (percentage >= 50) {
            "${percentage}% 感知 (P)"
        } else {
            "${100 - percentage}% 判断 (J)"
        }
    }
    
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("personalityType", personalityType)
            put("eiScore", eiScore)
            put("snScore", snScore)
            put("tfScore", tfScore)
            put("jpScore", jpScore)
            put("testDate", testDate)
            put("version", version)
            put("answers", MBTIAnswer.toJsonString(answers))
        }
    }
    
    companion object {
        fun fromJson(json: JSONObject): MBTIResult {
            return MBTIResult(
                personalityType = json.getString("personalityType"),
                eiScore = json.getInt("eiScore"),
                snScore = json.getInt("snScore"),
                tfScore = json.getInt("tfScore"),
                jpScore = json.getInt("jpScore"),
                testDate = json.getLong("testDate"),
                version = json.optString("version", "1.0.0"),
                answers = if (json.has("answers")) {
                    MBTIAnswer.fromJsonString(json.getString("answers"))
                } else emptyList()
            )
        }
    }
}


