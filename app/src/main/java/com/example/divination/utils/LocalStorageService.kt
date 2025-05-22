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
            
            // 读取文件内容
            val json = file.readText()
            if (json.isBlank()) {
                return null
            }
            
            try {
                // 解析JSON
                val gson = Gson()
                val result = gson.fromJson(json, DivinationResult::class.java)
                
                // 验证结果数据，确保不包含空节
                if (result != null) {
                    // 检查结果部分是否为空
                    if (result.resultSections.isEmpty()) {
                        // 添加一个默认节来修复数据
                        return createRepairResult(result)
                    }
                    
                    // 检查是否所有部分内容都为空
                    val allEmpty = result.resultSections.all { it.content.isBlank() }
                    if (allEmpty) {
                        return createRepairResult(result)
                    }
                    
                    return result
                }
                return null
            } catch (e: Exception) {
                // 如果JSON解析失败，尝试创建一个修复的结果
                return createEmergencyResult(resultId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 创建修复结果（保留原始元数据，添加错误提示内容）
     */
    private fun createRepairResult(original: DivinationResult): DivinationResult {
        // 创建修复的部分列表
        val repairedSections = mutableListOf<ResultSection>()
        
        // 根据不同的算命方法提供针对性的修复
        val isZhouYi = original.methodId == "zhouyi"
        val isBazi = original.methodId == "bazi"
        val isTarot = original.methodId == "tarot"
        val isAstrology = original.methodId == "astrology"
        val isDream = original.methodId == "dream"
        
        // 添加错误提示部分
        repairedSections.add(
            ResultSection(
                title = "数据恢复提示",
                content = when {
                    isZhouYi -> 
                        "周易占卜数据在处理过程中可能出现了超时或通信中断，这是系统自动修复的版本。\n\n" +
                        "您可以尝试在网络稳定的环境下重新进行算命以获取完整结果。\n\n" +
                        "如果问题持续存在，建议检查API设置或清除应用数据。"
                    isBazi ->
                        "八字命理分析在处理过程中出现了超时或数据异常，这是系统自动修复的版本。\n\n" +
                        "八字命理需要复杂的天干地支计算和五行分析，在不稳定的网络环境下可能需要较长时间。\n\n" +
                        "您可以尝试以下方法解决问题：\n" +
                        "1. 在网络稳定的环境下重新进行占卜\n" +
                        "2. 在设置中检查API密钥是否有效\n" +
                        "3. 尝试使用应用的其他占卜方式"
                    isTarot ->
                        "塔罗牌分析在处理过程中可能出现了超时或通信中断，这是系统自动修复的版本。\n\n" +
                        "塔罗牌解读需要分析多张牌面及其组合含义，处理复杂度较高。\n\n" +
                        "建议您：\n" +
                        "1. 在网络稳定的环境下重新进行占卜\n" +
                        "2. 尝试使用更简单的牌阵（如三张牌阵）\n" +
                        "3. 检查API设置是否正确"
                    isAstrology ->
                        "占星分析在处理过程中可能出现了超时或数据异常，这是系统自动修复的版本。\n\n" +
                        "占星需要计算多个行星位置及其相位关系，数据处理较为复杂。\n\n" +
                        "建议您：\n" +
                        "1. 确保输入的出生信息准确\n" +
                        "2. 在网络稳定的环境下重试\n" +
                        "3. 检查API设置是否正确"
                    isDream ->
                        "解梦分析在处理过程中可能出现了超时或数据问题，这是系统自动修复的版本。\n\n" +
                        "梦境解析需要大量文本理解和分析，处理复杂度较高。\n\n" +
                        "建议您：\n" +
                        "1. 尝试提供更简短、更具体的梦境描述\n" +
                        "2. 在网络稳定的环境下重试\n" +
                        "3. 检查API设置是否正确"
                    else ->
                        "原始结果数据已损坏或内容为空，这是系统自动修复的版本。\n\n" +
                        "您可以尝试重新进行算命以获取完整结果。\n\n" +
                        "如果问题持续存在，建议检查网络连接或API设置。"
                }
            )
        )
        
        // 保留原始的可读部分
        original.resultSections.forEach { section ->
            if (section.content.isNotBlank() && section.title != "错误提示" && section.title != "解析错误") {
                repairedSections.add(section)
            }
        }
        
        // 根据不同的占卜方法提供特定的备用内容
        if (isZhouYi) {
            // 为周易提供静态备用内容
            if (repairedSections.size <= 1) {  // 只有提示部分
                repairedSections.add(
                    ResultSection(
                        title = "卦象参考",
                        content = "由于数据问题无法恢复完整卦象，此处提供简要参考：\n\n" +
                                "在中国传统周易中，卦象代表宇宙运行的基本规律和状态，包含生活各方面的启示。" +
                                "建议您重新进行周易占卜，确保网络稳定，以获取完整专业的解读。"
                    )
                )
                
                // 添加一些基本的周易内容作为备用
                repairedSections.add(
                    ResultSection(
                        title = "卦象解析",
                        content = "周易包含八卦：乾、坤、震、艮、离、坎、兑、巽，代表天、地、雷、山、火、水、泽、风。\n\n" +
                                "通过八卦组合形成六十四卦，每一卦都包含特定智慧和启示，帮助人们理解时势变化和自身处境。\n\n" +
                                "卦辞、爻辞包含丰富的哲理，需要结合变化的时间和条件进行理解与应用。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "参考建议",
                        content = "1. 顺应时势，不要强求\n" +
                                "2. 保持谦逊的态度，平和心境\n" +
                                "3. 稳扎稳打，做好当下事情\n" +
                                "4. 关注自身修养，提升内在品质\n" +
                                "5. 多与智者交流，汲取智慧"
                    )
                )
            }
        } else if (isBazi) {
            // 为八字占卜提供静态备用内容
            if (repairedSections.size <= 1) {  // 只有提示部分
                repairedSections.add(
                    ResultSection(
                        title = "八字基本解读",
                        content = "八字命理以天干地支为基础，分析您的出生年月日时形成的四柱，揭示命运趋势。\n\n" +
                                "完整的八字分析包括四柱天干地支组合、五行强弱、喜用神与忌神、格局分析等多个方面。\n\n" +
                                "由于数据处理问题，此处无法提供您的完整八字分析，建议在网络稳定时重新尝试。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "命理通论",
                        content = "八字命理理论认为，人的命运受天时、地利、人和三方面影响，其中天时是最基础的，" +
                                "表现为出生时的天干地支组合。\n\n" +
                                "五行（金木水火土）的平衡与偏颇是分析命局的重要维度，五行各有其性质与象征意义。\n\n" +
                                "通过找出命局中的\"喜用神\"，可以知道哪些五行对您有利，从而在生活、事业、环境选择等方面做出更明智的决策。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "一般建议",
                        content = "1. 了解自己的五行特点，顺应而非对抗\n" +
                                "2. 选择适合自己的环境与职业方向\n" +
                                "3. 注重自身修养，提高应对能力\n" +
                                "4. 保持平和心态，不迷信也不排斥\n" +
                                "5. 结合当下具体情况，灵活应用命理智慧"
                    )
                )
            }
        } else if (isTarot) {
            // 为塔罗牌提供静态备用内容
            if (repairedSections.size <= 1) {
                repairedSections.add(
                    ResultSection(
                        title = "塔罗概览",
                        content = "塔罗牌是西方神秘传统的一部分，包含22张大阿卡纳牌和56张小阿卡纳牌，通过牌面组合提供对问题的洞察。\n\n" +
                                "每张牌都有独特象征意义，而牌面组合和牌位则为解读提供了更丰富的层次。\n\n" +
                                "由于数据处理问题，此处无法提供您的完整塔罗分析，建议稍后重新尝试。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "通用解读",
                        content = "塔罗牌的核心理念是个人能量与宇宙能量的连接，牌面展示的是当下能量状态而非注定的命运。\n\n" +
                                "大阿卡纳牌（如愚者、魔术师、命运之轮等）展示人生旅程的重要阶段和能量转变。\n\n" +
                                "小阿卡纳牌则分为四个花色（权杖、圣杯、宝剑、星币），分别对应事业/创造力、情感/关系、思想/挑战和物质/财富。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "一般建议",
                        content = "1. 保持开放心态，接受变化的可能性\n" +
                                "2. 信任您的直觉，它常常比逻辑思维更能接触真相\n" +
                                "3. 记住塔罗展示的是可能性，而非注定的结果\n" +
                                "4. 定期反思自己的选择和行动\n" +
                                "5. 使用塔罗提供的洞察作为自我成长的工具"
                    )
                )
            }
        } else if (isAstrology) {
            // 为占星学提供静态备用内容
            if (repairedSections.size <= 1) {
                repairedSections.add(
                    ResultSection(
                        title = "占星概述",
                        content = "占星学研究天体位置与人类命运的关联，通过分析出生时刻的行星位置，提供对个人性格和命运走向的洞察。\n\n" +
                                "完整的占星分析包括太阳、月亮、上升等多个要素，以及行星间的相位关系。\n\n" +
                                "由于数据处理问题，此处无法提供您的完整占星分析，建议稍后重新尝试。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "星座能量",
                        content = "十二星座代表不同的能量状态和性格特质：\n\n" +
                                "火象星座（白羊、狮子、射手）具有激情、行动力和创造性。\n" +
                                "土象星座（金牛、处女、摩羯）具有稳定性、实用性和耐心。\n" +
                                "风象星座（双子、天秤、水瓶）具有思考能力、社交性和创新精神。\n" +
                                "水象星座（巨蟹、天蝎、双鱼）具有情感深度、直觉力和同理心。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "一般建议",
                        content = "1. 了解自己的星盘特点，但避免过度限定自己\n" +
                                "2. 星座只是身份的一部分，不要完全依赖它定义自己\n" +
                                "3. 利用占星学的见解促进自我意识和个人成长\n" +
                                "4. 尊重个体差异，避免星座刻板印象\n" +
                                "5. 将占星学作为自我探索的工具，而非绝对的预测系统"
                    )
                )
            }
        } else if (isDream) {
            // 为解梦提供静态备用内容
            if (repairedSections.size <= 1) {
                repairedSections.add(
                    ResultSection(
                        title = "梦境分析基础",
                        content = "梦境是潜意识的表达，常常包含我们清醒时未处理的情绪、记忆和渴望。\n\n" +
                                "从弗洛伊德到荣格，心理学家们认为梦是理解内心世界的重要窗口。\n\n" +
                                "由于数据处理问题，此处无法提供您梦境的完整解析，建议稍后重新尝试。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "常见梦境象征",
                        content = "不同元素在梦中往往有特定的象征意义：\n\n" +
                                "水：表示情感状态，平静的水代表情绪平和，汹涌的水可能暗示情绪波动。\n" +
                                "飞行：常与自由感、超越限制或逃避现实有关。\n" +
                                "坠落：可能反映失控感、焦虑或对失败的恐惧。\n" +
                                "追逐：常与压力、逃避问题或未解决的冲突有关。\n" +
                                "迷路：可能表示生活中的迷茫、决策困难或寻找自我。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "一般建议",
                        content = "1. 记录梦境，寻找重复的主题或模式\n" +
                                "2. 关注梦中的情绪，它们常比内容更能揭示梦的意义\n" +
                                "3. 思考梦境元素与您当前生活的可能联系\n" +
                                "4. 将梦视为自我对话的一种形式，而非预言\n" +
                                "5. 尝试冥想或放松练习，有助于减轻可能引起噩梦的压力"
                    )
                )
            }
        } else {
            // 为其他占卜方法提供通用备用内容
            if (repairedSections.size <= 1) {
                repairedSections.add(
                    ResultSection(
                        title = "分析概述",
                        content = "占卜是人类尝试理解命运和获取指引的古老方式，不同文化发展出各自独特的占卜体系。\n\n" +
                                "无论采用何种方法，占卜的核心是连接直觉与理性，提供新的视角来看待生活中的问题。\n\n" +
                                "由于数据处理问题，无法提供完整分析，建议稍后重新尝试。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "普遍原则",
                        content = "所有占卜系统都基于以下原则：\n\n" +
                                "1. 整体性 - 宇宙万物相互关联\n" +
                                "2. 对应性 - 外在世界反映内在状态\n" +
                                "3. 同步性 - 有意义的巧合提供洞察\n" +
                                "4. 象征性 - 符号和意象传达深层信息\n\n" +
                                "这些原则帮助我们在看似随机的事件中发现意义和模式。"
                    )
                )
                
                repairedSections.add(
                    ResultSection(
                        title = "一般建议",
                        content = "1. 保持开放而警觉的心态\n" +
                                "2. 将占卜视为反思和洞察的工具，而非决策的唯一依据\n" +
                                "3. 注重占卜提供的整体方向，而非细节预测\n" +
                                "4. 定期反思和调整自己的行动和选择\n" +
                                "5. 记住自由意志始终是决定命运的关键因素"
                    )
                )
            }
        }
        
        // 创建新的DivinationResult对象
        return DivinationResult(
            id = original.id,
            methodId = original.methodId,
            createTime = original.createTime,
            inputData = original.inputData,
            resultSections = repairedSections
        )
    }
    
    /**
     * 创建紧急结果（JSON完全损坏的情况）
     */
    private fun createEmergencyResult(resultId: String): DivinationResult {
        val emergencySections = listOf(
            ResultSection(
                title = "数据错误",
                content = "结果数据已损坏，无法恢复。\n\n" +
                        "请尝试重新进行算命以获取新的结果。\n\n" +
                        "如果问题持续存在，建议清除应用数据或重新安装应用。"
            ),
            ResultSection(
                title = "可能原因",
                content = "1. 文件系统损坏\n" +
                        "2. 存储空间不足\n" +
                        "3. 应用更新导致数据格式不兼容\n" +
                        "4. 系统问题导致写入失败"
            )
        )
        
        return DivinationResult(
            id = resultId,
            methodId = "unknown",
            createTime = Date(),
            inputData = mapOf("error" to "数据损坏"),
            resultSections = emergencySections
        )
    }
    
    /**
     * 获取所有结果ID
     */
    fun getAllResultIds(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("result_ids", "[]") ?: "[]"
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
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