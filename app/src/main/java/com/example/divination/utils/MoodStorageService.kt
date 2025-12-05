package com.example.divination.utils

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

/**
 * 心情记录本地存储服务
 */
object MoodStorageService {

    private const val PREFS_NAME = "mood_storage"
    private const val KEY_RECORDS = "records"

    data class MoodEntry(
        val id: String,
        val mood: String,
        val note: String,
        val timestamp: Long
    )

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): List<MoodEntry> {
        return try {
            val json = prefs(context).getString(KEY_RECORDS, null) ?: return emptyList()
            val arr = JSONArray(json)
            val list = mutableListOf<MoodEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "")
                val mood = obj.optString("mood", "")
                val note = obj.optString("note", "")
                val ts = obj.optLong("timestamp", 0L)
                if (id.isNotEmpty() && ts > 0L) {
                    list.add(MoodEntry(id, mood, note, ts))
                }
            }
            list.sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, mood: String, note: String) {
        val current = getAll(context).toMutableList()
        val entry = MoodEntry(
            id = System.currentTimeMillis().toString(),
            mood = mood,
            note = note,
            timestamp = Date().time
        )
        current.add(0, entry)
        saveAll(context, current)
    }

    private fun saveAll(context: Context, list: List<MoodEntry>) {
        val arr = JSONArray()
        list.forEach { e ->
            val obj = JSONObject().apply {
                put("id", e.id)
                put("mood", e.mood)
                put("note", e.note)
                put("timestamp", e.timestamp)
            }
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY_RECORDS, arr.toString()).apply()
    }

    /**
     * 计算情绪画像和预警级别，供 DeepSeek 提示词或 UI 使用
     */
    data class MoodSummary(
        val personaTitle: String,
        val warningTitle: String
    )

    fun getSummary(context: Context): MoodSummary? {
        val entries = getAll(context)
        if (entries.isEmpty()) return null

        val positiveMoods = listOf("开心", "兴奋")
        val negativeMoods = listOf("疲惫", "难过", "焦虑")

        val total = entries.size.coerceAtLeast(1)
        val positiveRatio = entries.count { it.mood in positiveMoods }.toFloat() / total
        val negativeRatio = entries.count { it.mood in negativeMoods }.toFloat() / total

        val personaTitle = when {
            positiveRatio >= 0.7f -> "整体情绪偏乐观"
            negativeRatio >= 0.5f -> "近期情绪偏敏感"
            else -> "情绪总体比较平衡"
        }

        val warningTitle = when {
            negativeRatio >= 0.6f -> "情绪预警：注意过度消耗"
            positiveRatio >= 0.7f -> "状态良好：适合推进重要事项"
            else -> "平稳期：注意节奏和边界"
        }

        return MoodSummary(personaTitle, warningTitle)
    }
}
