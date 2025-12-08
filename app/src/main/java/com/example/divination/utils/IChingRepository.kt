package com.example.divination.utils

import android.content.Context
import com.example.divination.R
import org.json.JSONArray
import org.json.JSONObject

/**
 * 《易经》六十四卦数据仓库
 *
 * 从 res/raw/iching_hexagrams.json 读取卦象、卦辞、爻辞等信息，
 * 并根据六爻数值（6/7/8/9）提供本卦 / 变卦与动爻的查询能力。
 */
class IChingRepository private constructor(private val context: Context) {

    data class Yao(
        val index: Int,             // 1-6，初爻在下
        val positionName: String,   // 初九、六二等
        val text: String,           // 爻辞
        val xiang: String?          // 象传（可空）
    )

    data class Hexagram(
        val id: Int,
        val sequence: Int,
        val name: String,
        val pinyin: String,
        val alias: String,
        val upperTrigram: String,
        val lowerTrigram: String,
        val upperTrigramCode: String,
        val lowerTrigramCode: String,
        val binaryCode: String,
        val unicodeSymbol: String?,
        val brief: String?,
        val guaci: String,
        val tuan: String?,
        val xiang: String?,
        val yao: List<Yao>
    )

    /**
     * 起卦结果：本卦 + 变卦 + 动爻
     */
    data class DivinationHexagramResult(
        val originalHexagram: Hexagram?,
        val changedHexagram: Hexagram?,
        val movingLineIndexes: List<Int>  // 1-6，下到上
    )

    private val hexagrams: List<Hexagram> by lazy { loadHexagrams() }

    private fun loadHexagrams(): List<Hexagram> {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.iching_hexagrams)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val arr = root.getJSONArray("hexagrams")
            val list = mutableListOf<Hexagram>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(parseHexagram(obj))
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseHexagram(obj: JSONObject): Hexagram {
        val yaoArray = obj.getJSONArray("yao")
        val yaoList = mutableListOf<Yao>()
        for (i in 0 until yaoArray.length()) {
            val y = yaoArray.getJSONObject(i)
            yaoList.add(
                Yao(
                    index = y.getInt("index"),
                    positionName = y.getString("position_name"),
                    text = y.getString("text"),
                    xiang = y.optString("xiang", "").ifBlank { null }
                )
            )
        }
        return Hexagram(
            id = obj.getInt("id"),
            sequence = obj.optInt("sequence", obj.getInt("id")),
            name = obj.getString("name"),
            pinyin = obj.optString("pinyin", ""),
            alias = obj.optString("alias", ""),
            upperTrigram = obj.optString("upper_trigram", ""),
            lowerTrigram = obj.optString("lower_trigram", ""),
            upperTrigramCode = obj.optString("upper_trigram_code", ""),
            lowerTrigramCode = obj.optString("lower_trigram_code", ""),
            binaryCode = obj.getString("binary_code"),
            unicodeSymbol = obj.optString("unicode_symbol", "").ifBlank { null },
            brief = obj.optString("brief", "").ifBlank { null },
            guaci = obj.getString("guaci"),
            tuan = obj.optString("tuan", "").ifBlank { null },
            xiang = obj.optString("xiang", "").ifBlank { null },
            yao = yaoList
        )
    }

    /** 根据六爻阴阳编码（自下而上）查找本卦 */
    fun getHexagramByBinaryCode(code: String): Hexagram? {
        return hexagrams.firstOrNull { it.binaryCode == code }
    }

    /**
     * 根据 6 个爻值（6/7/8/9，自下而上）计算本卦 / 变卦
     */
    fun getDivinationResult(lines: List<Int>): DivinationHexagramResult {
        require(lines.size == 6) { "Expect 6 lines, got ${lines.size}" }

        // 阴阳编码：阴=0(6,8)，阳=1(7,9)，自下而上
        val bits = lines.map { value ->
            when (value) {
                6, 8 -> 0
                7, 9 -> 1
                else -> error("Invalid line value: $value, expect 6/7/8/9")
            }
        }

        val binaryCode = bits.toBinaryCode()

        // 变卦：6(老阴)->阳，9(老阳)->阴
        val changedLines = lines.map { value ->
            when (value) {
                6 -> 7  // 老阴变少阳
                9 -> 8  // 老阳变少阴
                else -> value
            }
        }
        val changedBits = changedLines.map { value ->
            when (value) {
                6, 8 -> 0
                7, 9 -> 1
                else -> error("Invalid line value: $value, expect 6/7/8/9")
            }
        }
        val changedBinaryCode = changedBits.toBinaryCode()

        val movingIndexes = lines.mapIndexedNotNull { index, v ->
            if (v == 6 || v == 9) index + 1 else null  // 1-6
        }

        return DivinationHexagramResult(
            originalHexagram = getHexagramByBinaryCode(binaryCode),
            changedHexagram = getHexagramByBinaryCode(changedBinaryCode),
            movingLineIndexes = movingIndexes
        )
    }

    private fun List<Int>.toBinaryCode(): String {
        // bits[0] 是初爻（最低位），bits[5] 是上爻
        return this.joinToString(separator = "") { it.toString() }
    }

    fun getAllHexagrams(): List<Hexagram> = hexagrams

    companion object {
        @Volatile
        private var instance: IChingRepository? = null

        fun getInstance(context: Context): IChingRepository {
            return instance ?: synchronized(this) {
                instance ?: IChingRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
