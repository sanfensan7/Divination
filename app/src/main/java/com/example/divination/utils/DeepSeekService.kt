package com.example.divination.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.divination.model.DivinationMethod
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

/**
 * DeepSeek API服务类
 */
object DeepSeekService {
    
    private const val TAG = "DeepSeekService"
    private const val API_URL = "https://api.deepseek.com/v1/chat/completions"  // DeepSeek API地址
    
    // 从本地存储获取API密钥
    private fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences("divination_prefs", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""
        
        // 检查并清理API密钥中可能的非法字符（如换行符等）
        return apiKey.trim().replace(Regex("\\s+"), "")
    }
    
    /**
     * 执行算命请求
     * 
     * @param context 上下文
     * @param method 算命方法
     * @param inputData 输入数据
     * @param callback 结果回调
     */
    fun performDivination(
        context: Context,
        method: DivinationMethod,
        inputData: Map<String, String>,
        callback: (DivinationResult?, Exception?) -> Unit
    ) {
        // 在后台线程执行网络请求
        thread {
            try {
                // 构建提示词
                val prompt = buildPrompt(method, inputData)
                
                // 检查API密钥
                val apiKey = getApiKey(context)
                if (apiKey.isEmpty()) {
                    // API密钥未设置，使用模拟响应并显示提示
                    showToast(context, "API密钥未设置，使用本地模拟模式")
                    Log.i(TAG, "API密钥未设置，使用本地模拟模式")
                    
                    // 创建模拟响应
                    val response = simulateResponse(prompt)
                    
                    // 添加模拟标记和解析响应
                    val simulatedSection = ResultSection(
                        title = "提示",
                        content = "本次结果由本地模拟生成，未使用AI服务。请在设置中配置API密钥以获取真实AI算命结果。"
                    )
                    val sections = parseResponseToSections(response)
                    val result = DivinationResult(
                        id = UUID.randomUUID().toString(),
                        methodId = method.id,
                        createTime = Date(),
                        inputData = inputData,
                        resultSections = listOf(simulatedSection) + sections
                    )
                    
                    // 回调主线程
                    android.os.Handler(context.mainLooper).post {
                        callback(result, null)
                    }
                    return@thread
                }
                
                try {
                    // 尝试发送API请求
                    showToast(context, "正在连接AI服务...")
                    val response = sendRequest(context, apiKey, prompt)
                    
                    // 解析响应
                    val result = parseResponse(method, inputData, response)
                    
                    // 回调主线程
                    android.os.Handler(context.mainLooper).post {
                        callback(result, null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "API调用失败", e)
                    
                    // 使用模拟响应作为备选
                    showToast(context, "AI服务调用失败：${e.message?.take(50)}...，使用本地模拟模式")
                    
                    // 创建模拟响应
                    val response = simulateResponse(prompt)
                    
                    // 添加模拟标记和解析响应
                    val errorSection = ResultSection(
                        title = "错误信息",
                        content = "API调用失败：${e.message}\n使用本地模拟生成的结果代替。请检查API密钥设置或网络连接。"
                    )
                    val sections = parseResponseToSections(response)
                    val result = DivinationResult(
                        id = UUID.randomUUID().toString(),
                        methodId = method.id,
                        createTime = Date(),
                        inputData = inputData,
                        resultSections = listOf(errorSection) + sections
                    )
                    
                    // 回调主线程
                    android.os.Handler(context.mainLooper).post {
                        callback(result, null)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "算命请求失败", e)
                
                // 回调主线程
                android.os.Handler(context.mainLooper).post {
                    callback(null, e)
                }
            }
        }
    }
    
    /**
     * 显示提示消息
     */
    private fun showToast(context: Context, message: String) {
        android.os.Handler(context.mainLooper).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 构建提示词
     */
    private fun buildPrompt(method: DivinationMethod, inputData: Map<String, String>): String {
        val sb = StringBuilder()
        
        // 添加算命方法说明
        sb.append("请作为一个专业的${method.name}分析师，")
        
        // 添加输入数据
        when (method.id) {
            "bazi" -> {
                sb.append("根据以下信息进行八字命理分析：\n")
                sb.append("出生日期：${inputData["birthDate"]}\n")
                sb.append("出生时间：${inputData["birthTime"]}\n")
                sb.append("性别：${inputData["gender"]}\n")
                sb.append("\n请提供详细的八字命理分析，包括命主的五行属性、日主强弱、大运走向、事业财运、婚姻情感等方面的解读。")
            }
            "ziwei" -> {
                sb.append("根据以下信息进行紫微斗数分析：\n")
                sb.append("出生日期：${inputData["birthDate"]}\n")
                sb.append("出生时辰：${inputData["birthTime"]}\n")
                sb.append("性别：${inputData["gender"]}\n")
                sb.append("\n请提供详细的紫微斗数星盘分析，包括十二宫位解读、主星分析、流年运势等内容。")
            }
            "zhouyi" -> {
                sb.append("请对以下问题进行周易预测：\n")
                sb.append("预测问题：${inputData["question"]}\n")
                sb.append("\n请随机生成一个六十四卦之一，并提供详细的卦象分析，包括卦辞、爻辞解读和针对问题的预测结果。")
            }
            "tarot" -> {
                sb.append("请进行塔罗牌预测：\n")
                sb.append("咨询问题：${inputData["question"]}\n")
                sb.append("牌阵：${inputData["spread"]}\n")
                sb.append("\n请随机抽取适合该牌阵的塔罗牌，并提供详细的牌面解读、牌位含义以及对问题的预测和建议。")
            }
            "astrology" -> {
                sb.append("请根据以下信息进行占星分析：\n")
                sb.append("出生日期：${inputData["birthDate"]}\n")
                sb.append("出生时间：${inputData["birthTime"]}\n")
                sb.append("出生地点：${inputData["birthPlace"]}\n")
                sb.append("\n请提供详细的星盘分析，必须包含星盘图的详细文字描述，以便我们能在APP中还原星盘图。描述中需要包括：")
                sb.append("\n1. 各行星的位置，请使用以下格式描述行星位置：")
                sb.append("\n   太阳位于白羊座15度")
                sb.append("\n   月亮位于金牛座10度")
                sb.append("\n   (其他行星同理，必须包含：太阳、月亮、水星、金星、火星、木星、土星、天王星、海王星、冥王星、上升点、中天点)")
                sb.append("\n2. 行星之间的相位关系，请使用以下格式：")
                sb.append("\n   太阳和月亮形成六分相")
                sb.append("\n   水星和金星形成合相")
                sb.append("\n   (其他相位关系同理)")
                sb.append("\n3. 上升星座和中天星座")
                sb.append("\n4. 十二宫的分布")
                sb.append("\n5. 关键星团或特殊点的位置")
                sb.append("\n\n之后再提供太阳、月亮、上升星座解读，行星相位分析，以及对性格特质、事业发展、人际关系等方面的解读。")
            }
            "numerology" -> {
                sb.append("请根据以下信息进行数字命理学分析：\n")
                sb.append("全名：${inputData["fullName"]}\n")
                sb.append("出生日期：${inputData["birthDate"]}\n")
                sb.append("\n请计算命运数字、灵魂数字、表现数字等关键数字，并提供详细的数字能量解读，包括性格特质、生命使命、潜在挑战等方面的分析。")
            }
            else -> {
                sb.append("请根据以下信息进行命理分析：\n")
                inputData.forEach { (key, value) ->
                    sb.append("$key：$value\n")
                }
            }
        }
        
        // 添加输出格式要求
        sb.append("\n请按照以下格式返回结果，以便应用程序解析：\n")
        
        // 为占星学添加特殊的星盘部分
        if (method.id == "astrology") {
            sb.append("【星盘描述】\n详细的星盘结构描述\n")
        }
        
        sb.append("【总论】\n总体运势分析\n")
        sb.append("【事业】\n事业运势分析\n")
        sb.append("【财运】\n财运分析\n")
        sb.append("【感情】\n感情运势分析\n")
        sb.append("【健康】\n健康状况分析\n")
        sb.append("【建议】\n改善建议\n")
        
        return sb.toString()
    }
    
    /**
     * 发送API请求
     */
    private fun sendRequest(context: Context, apiKey: String, prompt: String): String {
        // 创建DeepSeek API请求
        val url = URL(API_URL)
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.doOutput = true
        
        try {
            // 构建请求体
            val requestBody = JSONObject()
            requestBody.put("model", "deepseek-chat") // 使用原始模型名称
            
            val messagesArray = JSONArray()
            val messageObject = JSONObject()
            messageObject.put("role", "user")
            messageObject.put("content", prompt)
            messagesArray.put(messageObject)
            
            requestBody.put("messages", messagesArray)
            requestBody.put("temperature", 0.7)
            requestBody.put("max_tokens", 2000)
            
            // 发送请求
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(requestBody.toString().toByteArray(StandardCharsets.UTF_8))
            outputStream.flush()
            outputStream.close()
            
            // 获取响应
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                // 解析JSON响应
                val jsonResponse = JSONObject(response.toString())
                return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                
            } else {
                // 如果API调用失败，抛出异常
                throw Exception("API调用失败，状态码：$responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * 模拟API响应（仅在实际API调用失败时使用）
     */
    private fun simulateResponse(prompt: String): String {
        // 生成一个随机响应，使每次结果不同
        val currentTime = System.currentTimeMillis()
        val random = Random(currentTime)
        
        // 提取用户输入的关键信息
        val birthDateMatch = "出生日期：([^\\n]+)".toRegex().find(prompt)
        val birthTimeMatch = "出生时间：([^\\n]+)|出生时辰：([^\\n]+)".toRegex().find(prompt)
        val genderMatch = "性别：([^\\n]+)".toRegex().find(prompt)
        val questionMatch = "预测问题：([^\\n]+)|咨询问题：([^\\n]+)".toRegex().find(prompt)
        val fullNameMatch = "全名：([^\\n]+)".toRegex().find(prompt)
        
        val birthDate = birthDateMatch?.groupValues?.get(1)?.trim()
        val birthTime = birthTimeMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } 
            ?: birthTimeMatch?.groupValues?.get(2)?.trim()
        val gender = genderMatch?.groupValues?.get(1)?.trim()
        val question = questionMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } 
            ?: questionMatch?.groupValues?.get(2)?.trim()
        val fullName = fullNameMatch?.groupValues?.get(1)?.trim()
        
        val sb = StringBuilder()
        
        // 根据提示词中的关键字定制化响应
        val hasNameKeyword = prompt.contains("全名") || prompt.contains("姓名")
        val hasBirthDateKeyword = prompt.contains("出生日期") || prompt.contains("生日")
        val hasCareerKeyword = prompt.contains("事业") || prompt.contains("职业")
        val hasLoveKeyword = prompt.contains("感情") || prompt.contains("爱情") || prompt.contains("婚姻")
        
        // 生成总论部分
        sb.append("【总论】\n")
        if (fullName != null) {
            sb.append("${fullName}，")
        }
        
        if (birthDate != null) {
            sb.append("根据您${birthDate}") 
            if (birthTime != null) {
                sb.append(" ${birthTime}")
            }
            sb.append("的出生信息，")
        } else {
            sb.append("根据分析，")
        }
        
        sb.append("您的命盘显示")
        
        val traits = listOf("创造力", "领导力", "直觉", "耐心", "细心", "热情", "冷静", "理性", "感性", "随和")
        val selectedTraits = mutableListOf<String>()
        val traitCount = random.nextInt(3) + 2  // 2-4个特质
        for (i in 0 until traitCount) {
            val trait = traits[random.nextInt(traits.size)]
            if (!selectedTraits.contains(trait)) {
                selectedTraits.add(trait)
            }
        }
        sb.append("较强的${selectedTraits.joinToString("和")}。")
        
        // 随机选择五行属性
        val elements = listOf("金", "木", "水", "火", "土")
        val strongElements = elements[random.nextInt(elements.size)]
        val weakElements = elements.filter { it != strongElements }.random()
        sb.append("五行中${strongElements}较旺，${weakElements}较弱。")
        
        // 随机生成运势趋势
        val trends = listOf("稳步上升", "波动起伏", "缓慢提升", "先抑后扬", "保持平稳")
        sb.append("总体命运呈现${trends[random.nextInt(trends.size)]}的趋势，")
        
        // 随机生成关键年龄
        val age1 = random.nextInt(11) + 25  // 25-35岁
        val age2 = random.nextInt(11) + 40  // 40-50岁
        sb.append("尤其在${age1}岁和${age2}岁将有重要转折。\n\n")
        
        // 生成事业部分
        sb.append("【事业】\n")
        if (hasCareerKeyword) {
            val careerFields = listOf("管理", "教育", "艺术", "科技", "医疗", "金融", "服务", "制造", "传媒", "法律")
            val selectedFields = mutableListOf<String>()
            val fieldCount = random.nextInt(3) + 2  // 2-4个领域
            for (i in 0 until fieldCount) {
                val field = careerFields[random.nextInt(careerFields.size)]
                if (!selectedFields.contains(field)) {
                    selectedFields.add(field)
                }
            }
            sb.append("您适合从事${selectedFields.joinToString("、")}等领域的工作。")
        } else {
            sb.append("在事业发展上，")
        }
        
        val careerStages = listOf("起步", "成长", "稳定", "转型", "辉煌")
        sb.append("当前处于${careerStages[random.nextInt(careerStages.size)]}阶段，")
        
        val year1 = 2023 + random.nextInt(4)  // 2023-2026年
        val year2 = year1 + random.nextInt(3) + 1  // year1后的1-3年
        sb.append("【${year1}年到${year2}年】有重要机遇，可能带来职业上的重要突破。")
        
        val careerAdvices = listOf(
            "建议持续学习新技能，提升专业能力",
            "适合大胆创新，尝试新的领域",
            "宜稳健发展，避免冒险",
            "可以寻求合作伙伴，共同发展",
            "应该加强人脉拓展，寻求贵人相助"
        )
        sb.append(careerAdvices[random.nextInt(careerAdvices.size)] + "。\n\n")
        
        // 生成财运部分
        sb.append("【财运】\n")
        val wealthLevels = listOf("较好", "波动较大", "稳步增长", "需要谨慎规划", "潜力巨大")
        sb.append("财运整体${wealthLevels[random.nextInt(wealthLevels.size)]}，")
        
        val wealthSources = listOf("主要来自固定收入", "可通过投资获得额外收益", "有意外之财的可能", "需要勤劳积累")
        sb.append(wealthSources[random.nextInt(wealthSources.size)] + "。")
        
        val wealthYear = 2023 + random.nextInt(3)  // 2023-2025年
        sb.append("【${wealthYear}年】财运较为旺盛，")
        
        val wealthAdvices = listOf(
            "适合投资理财",
            "宜稳健理财，避免风险投资",
            "可适当进行房产投资",
            "应优先偿还债务，稳固财务基础",
            "建议增加被动收入来源"
        )
        sb.append(wealthAdvices[random.nextInt(wealthAdvices.size)] + "。\n\n")
        
        // 生成感情部分
        sb.append("【感情】\n")
        if (hasLoveKeyword || gender != null) {
            val loveStates = listOf(
                "感情线条清晰但曲折",
                "感情发展较为平稳",
                "感情经历波折但最终圆满",
                "需要主动追求才能获得理想感情",
                "容易吸引异性但需谨慎选择"
            )
            sb.append(loveStates[random.nextInt(loveStates.size)] + "，")
            
            val relationshipCount = random.nextInt(4) + 1  // 1-4段重要感情
            sb.append("一生中可能经历${relationshipCount}段重要感情。")
        }
        
        val loveTraits = listOf(
            "您在感情中较为理想化",
            "您在感情中注重精神交流",
            "您在感情中渴望安全感",
            "您在感情中追求刺激与新鲜感",
            "您在感情中重视忠诚与信任"
        )
        sb.append(loveTraits[random.nextInt(loveTraits.size)] + "，")
        
        val loveAdvices = listOf(
            "需要找到理解您独立精神的伴侣",
            "适合与性格互补的人建立关系",
            "应该提高沟通能力，避免误解",
            "宜放下戒备，敞开心扉接受真爱",
            "建议多关注对方需求，增进感情和谐"
        )
        sb.append(loveAdvices[random.nextInt(loveAdvices.size)] + "。\n\n")
        
        // 生成健康部分
        sb.append("【健康】\n")
        val healthStates = listOf("总体良好", "需要注意保养", "有潜在隐患", "较为稳定", "需定期检查")
        sb.append("健康状况${healthStates[random.nextInt(healthStates.size)]}，")
        
        val bodyParts = listOf("呼吸系统", "消化系统", "神经系统", "心血管系统", "肌肉骨骼", "免疫系统")
        val weakBodyPart = bodyParts[random.nextInt(bodyParts.size)]
        sb.append("需特别关注${weakBodyPart}的保养。")
        
        val healthAdvices = listOf(
            "建议保持规律作息，避免熬夜",
            "适当增加有氧运动，增强体质",
            "注意饮食均衡，少食多餐",
            "建议定期体检，预防疾病",
            "可尝试冥想或瑜伽，缓解压力"
        )
        sb.append(healthAdvices[random.nextInt(healthAdvices.size)] + "。\n\n")
        
        // 生成建议部分
        sb.append("【建议】\n")
        
        // 如果有具体问题，添加针对性建议
        if (question != null) {
            sb.append("关于\"${question}\"的问题，")
            val specificAdvices = listOf(
                "建议您保持耐心，时机尚未成熟",
                "目前情况对您有利，可以积极行动",
                "需要谨慎考虑各方面因素，不宜操之过急",
                "有贵人相助的迹象，可以寻求他人支持",
                "时机已到，可以果断决策"
            )
            sb.append(specificAdvices[random.nextInt(specificAdvices.size)] + "。\n\n")
        }
        
        val adviceCount = random.nextInt(3) + 4  // 4-6条建议
        val allAdvices = listOf(
            "培养耐心和持续力，不要因短期困难放弃长远目标",
            "加强情绪管理，避免冲动决策",
            "建立健康的生活习惯，包括饮食、运动和休息",
            "学习财务规划，合理配置资产",
            "在人际关系中保持真诚，但也要有适当边界",
            "定期反思与调整，使人生方向与内心期望一致",
            "多与积极向上的人交往，远离负能量",
            "培养一项终身爱好，丰富精神世界",
            "学会感恩，保持积极乐观的心态",
            "关注精神成长，提升内在修养"
        )
        
        val selectedAdvices = allAdvices.shuffled().take(adviceCount)
        for (i in selectedAdvices.indices) {
            sb.append("${i + 1}. ${selectedAdvices[i]}\n")
        }
        
        return sb.toString()
    }
    
    /**
     * 解析响应文本为结果段落列表
     */
    private fun parseResponseToSections(response: String): List<ResultSection> {
        val resultSections = mutableListOf<ResultSection>()
        
        // 解析各个部分
        val parts = response.split("【")
        for (part in parts) {
            if (part.isEmpty()) continue
            
            val titleEnd = part.indexOf("】")
            if (titleEnd > 0) {
                val title = part.substring(0, titleEnd)
                val content = part.substring(titleEnd + 1).trim()
                
                // 创建结果部分
                resultSections.add(
                    ResultSection(
                        title = title,
                        content = content
                    )
                )
            }
        }
        
        return resultSections
    }
    
    /**
     * 解析API响应
     */
    private fun parseResponse(
        method: DivinationMethod,
        inputData: Map<String, String>,
        response: String
    ): DivinationResult {
        // 解析响应内容为结果段落
        val sections = parseResponseToSections(response)
        
        // 创建算命结果
        return DivinationResult(
            id = UUID.randomUUID().toString(),
            methodId = method.id,
            createTime = Date(),
            inputData = inputData,
            resultSections = sections
        )
    }
}