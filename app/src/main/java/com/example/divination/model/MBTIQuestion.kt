package com.example.divination.model

import org.json.JSONObject

/**
 * MBTI测试题目数据模型
 * 
 * @property id 题目ID
 * @property text 题目内容
 * @property dimension 所属维度 (EI/SN/TF/JP)
 * @property direction 计分方向 (1: 正向, -1: 反向)
 */
data class MBTIQuestion(
    val id: Int,
    val text: String,
    val dimension: Dimension,
    val direction: Int = 1 // 1 表示正向计分, -1 表示反向计分
) {
    /**
     * MBTI四个维度枚举
     */
    enum class Dimension(val code: String, val displayName: String) {
        EI("EI", "外向/内向"),    // Extraversion/Introversion
        SN("SN", "感觉/直觉"),    // Sensing/Intuition
        TF("TF", "思考/情感"),    // Thinking/Feeling
        JP("JP", "判断/感知");    // Judging/Perceiving
        
        companion object {
            fun fromCode(code: String): Dimension {
                return values().find { it.code == code } 
                    ?: throw IllegalArgumentException("Unknown dimension code: $code")
            }
        }
    }
    
    companion object {
        /**
         * 从JSON对象创建Question实例
         */
        fun fromJson(json: JSONObject): MBTIQuestion {
            return MBTIQuestion(
                id = json.getInt("id"),
                text = json.getString("text"),
                dimension = Dimension.fromCode(json.getString("dimension")),
                direction = json.optInt("direction", 1)
            )
        }
    }
}
