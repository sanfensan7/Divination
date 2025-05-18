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
                val prompt = generatePrompt(method, inputData)
                
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
     * 生成DeepSeek提示词
     */
    private fun generatePrompt(method: DivinationMethod, inputData: Map<String, String>): String {
        val basePrompt = """
        你是一位精通中国传统和西方占卜术的资深算命大师，拥有30年实践经验。
        请根据以下信息，生成一份详细、专业且符合文化背景的${method.name}分析结果：
        
        ## 分析背景
        - 分析方法：${method.name}（${if (method.type == 1) "中国传统" else "西方传统"}）
        - 分析目的：提供准确、有深度的命运解读，帮助用户理解自身状况和未来发展
        - 分析特点：结合传统理论与现代心理学，既尊重传统又符合现代人思维
        
        ## 用户输入信息
        ${inputData.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }}
        
        ## 输出要求
        1. 内容分为至少3个独立章节，每章节有标题和详细解读
        2. 整体分析长度在800-1200字之间
        3. 语言风格应专业、庄重但不晦涩，易于理解
        4. 必须包含传统文化元素和专业术语解释
        5. 分析必须全面平衡，既指出积极优势，也要直接指出潜在问题、性格弱点和需要注意的风险
        6. 避免极端负面预测（如灾难、死亡等），但应指出若不改变会带来的实际挑战和后果
        7. 给予具体建议时，应包含规避风险、克服弱点的实际方法
        8. 在适当位置引用传统经典语句增加可信度
        
        ## 特殊要求
        ${getMethodSpecificPrompt(method.id)}
        
        请确保分析符合${method.name}的核心理论和方法论，不要混入其他占卜体系的元素。
        分析应具有个性化和针对性，避免空泛、模糊的表述。
        提供的建议应当实用、可操作，既有正面引导也有问题警示。
        """
        
        return basePrompt
    }
    
    /**
     * 根据不同算命方法获取特定提示词补充
     */
    private fun getMethodSpecificPrompt(methodId: String): String {
        return when (methodId) {
            "bazi" -> """
                - 必须按照天干地支理论分析八字四柱（年柱、月柱、日柱、时柱）
                - 详细解读用户五行强弱、日主特质、大运走向和流年影响
                - 分析命局中的喜用神与忌神，提出趋吉避凶的方法
                - 解释十神关系如正官、七杀、正印、偏印等在命局中的作用
                - 评估日主与各柱之间的关系，分析财、官、印、食等方面的表现
                - 包含传统五行相生相克理论对人生的影响
                - 指出命局中的冲克、刑害关系及其可能带来的挑战
                - 说明大运流年对命局的影响，包括顺逆运势的客观分析
                - 提出针对命局弱点的调整方法，如调整五行、选择适当的方位等
            """
            
            "ziwei" -> """
                - 必须基于紫微斗数十四主星和诸多辅星的组合与飞化
                - 详细分析十二宫位（命宫、财帛宫、兄弟宫、夫妻宫等）的主星组合
                - 解读四化星（化科、化禄、化权、化忌）对命盘的影响
                - 评估三方四正的吉凶格局
                - 分析大限、小限的流年运势变化
                - 重点解读命宫、身宫的星曜组合对性格的影响
                - 详细说明各宫位主管事项的发展情况
                - 直接指出各宫位中不利星曜的作用及其可能带来的挑战
                - 解释命盘中的煞星组合，如擎羊、陀罗、火星、铃星等的不利影响
                - 提供如何通过后天努力减轻不利星曜影响的建议
            """
            
            "qimen" -> """
                - 依据奇门遁甲九宫八门九星理论进行分析
                - 详细解读所在局的天盘、地盘、人盘三盘关系
                - 分析值符、值使所在宫位及其表现
                - 解释九星（天蓬、天任、天冲等）与八门（休、生、伤、杜等）的组合
                - 评估八神（值符、腾蛇、太阴等）在盘中的吉凶
                - 针对用户关注事项，分析适用宫位的吉凶与趋势
                - 说明三奇六仪太阳星的落宫对大局的影响
                - 明确指出盘中的凶门、煞神所在位置及其不利影响
                - 分析门户冲克关系带来的潜在问题
                - 建议如何趋吉避凶，规避盘中不利因素
            """
            
            "astrology" -> """
                - 必须首先提供星盘图的详细文字描述，以便我们能在APP中还原星盘图，请使用以下格式：
                  * 太阳位于白羊座15度
                  * 月亮位于金牛座10度
                  * 水星位于双子座5度
                  * 金星位于处女座20度
                  * 火星位于天秤座15度
                  * 木星位于天蝎座30度
                  * 土星位于金牛座0度
                  * 天王星位于双子座15度
                  * 海王星位于双鱼座0度
                  * 冥王星位于摩羯座0度
                  * 上升点位于白羊座0度
                  * 中天点位于摩羯座0度
                  (必须包含以上所有行星位置和度数)
                - 必须详细描述行星之间的相位关系，使用以下格式：
                  * 太阳和木星形成三分相(120度)
                  * 太阳和土星形成对分相(180度)
                  * 火星和冥王星形成四分相(90度)
                  * 金星和海王星形成六分相(60度)
                  (至少列出5个主要相位)
                - 详细分析太阳星座、月亮星座、上升星座三大主要星座特质
                - 解读十大行星在各宫位的影响和相位关系
                - 分析命盘中的相位组合（三分相、六分相、四分相、合相等）
                - 解释北交点、南交点对灵魂使命的启示
                - 评估命盘中的元素平衡（火、地、风、水）
                - 分析命盘中的主要格局和星盘形状（大三角、T十字等）
                - 结合用户当前的行运和进行相位分析
                - 详细解读不利相位（如四分相、对分相）的挑战和潜在问题
                - 分析受克制的行星及其可能导致的人生困境
                - 说明命盘中的缺失元素或弱势宫位带来的不平衡
                - 提供如何通过意识和行动弥补星盘缺陷的建议
            """
            
            "tarot" -> """
                - 详细解读每张塔罗牌的象征意义和牌面故事
                - 分析牌阵中的相互关系和整体故事线
                - 针对每个位置的牌义进行专业诠释（过去、现在、未来等）
                - 解释正位与逆位的不同含义
                - 分析大阿卡纳牌与小阿卡纳牌的不同侧重
                - 结合牌面元素（数字、符号、颜色）进行多层次解读
                - 对牌阵整体趋势给予清晰总结
                - 包含塔罗经典理论和心理学元素的专业解析
                - 直接解读牌阵中的负面牌（如死神、塔、恶魔等）的警示意义
                - 指出逆位牌所代表的具体挑战和阻碍
                - 分析牌阵中的不协调关系和潜在冲突
                - 在建议中包含如何应对负面牌预示的困境
            """
            
            "face" -> """
                - 基于传统面相学五官与命运关系的分析
                - 详细解读额头（天庭）、眉毛、眼睛、鼻子、嘴巴、下巴（地阁）等特征
                - 分析面部气色、痣点、纹理对运势的影响
                - 根据三停五岳理论评估面相整体平衡
                - 解释耳朵、眉毛等细节特征的命理含义
                - 分析面相中的财富线、婚姻线、事业线等特征
                - 结合传统相学与现代面部特征研究
                - 提供面相改善的建议（如提升气色、注意健康等）
                - 指出五官中的不协调特征及其反映的性格弱点
                - 解读面相中的"破绽"和"缺陷"对运势的影响
                - 分析面部特征中预示的健康隐患和注意事项
                - 提供如何通过表情、气色调整来改善面相能量的建议
            """
            
            "palm" -> """
                - 详细分析手掌的生命线、智慧线、感情线、命运线等主要线纹
                - 解读手型（水型、火型、土型、风型）对性格的影响
                - 评估指纹类型及指节特征
                - 分析大拇指、金星丘、月丘等丘陵的特点
                - 解释掌纹交叉、断裂、分叉等特殊纹路的含义
                - 结合两手掌纹的差异（先天与后天的对比）
                - 详细说明特殊标记（岛、星、格等）的命理意义
                - 提供如何强化有利掌纹能量的建议
                - 直接指出掌纹中的断裂、岛纹、交叉等不利特征的具体影响
                - 解读掌纹中预示的健康、情感或事业挑战
                - 分析手掌整体能量中的弱点和不平衡之处
                - 提供如何通过行为和习惯改变来弥补掌纹缺陷的方法
            """
            
            "fengshui" -> """
                - 基于传统风水学理论分析住宅或办公环境
                - 详细解读房屋坐向、门窗位置与五行关系
                - 分析住宅内部格局与八宅派、玄空派理论的契合度
                - 评估环境中的明堂、水口、煞气等因素
                - 解释室内装饰、颜色、材质对宅运的影响
                - 根据用户八字分析最适宜的风水布局
                - 提出具体可行的风水调整建议
                - 说明不同空间（卧室、客厅、厨房等）的风水要点
                - 指出住宅中存在的煞气、穿堂煞、冲煞等不利因素
                - 解析格局中的缺角、尖角对居住者的潜在影响
                - 评估住宅朝向与住户命理相冲的问题
                - 分析装修色彩、材质与主人五行不协调之处
                - 提供消除或化解不良风水的具体方法
            """
            
            "dream" -> """
                - 基于中国传统周公解梦和现代心理学理论
                - 详细解析梦境中的人物、事件、场景、情绪的象征意义
                - 分析梦境与做梦者现实生活的关联
                - 解读不同类型梦境（预知梦、重复梦、清醒梦等）的特殊含义
                - 结合用户生活背景与心理状态进行个性化解读
                - 从潜意识角度探讨梦境的深层次信息
                - 提供如何利用梦境指引改善现实生活的建议
                - 特别关注梦境中的关键符号和情感体验
                - 直接解读梦境中的负面元素（如死亡、坠落、追逐等）的警示意义
                - 分析梦中出现的焦虑、恐惧等情绪所反映的潜在问题
                - 指出梦境可能揭示的自我欺骗或盲点
                - 提供如何面对梦中揭示的内心冲突和不安的方法
            """
            
            "name" -> """
                - 基于传统姓名学五格剖象法进行详细分析
                - 解读天格、地格、人格、外格、总格的数理吉凶
                - 分析姓名的三才配置（天才、人才、地才）
                - 评估姓名中汉字的五行属性与用户八字的搭配
                - 解释姓名笔画数与数理能量的关系
                - 分析姓名的音律和声调对运势的影响
                - 提供姓名能量对事业、婚姻、健康等方面的影响分析
                - 如有必要，建议更为吉利的用字或改名方向
                - 直接指出姓名中的凶数、不良音律或失衡五行
                - 解析姓名与八字相冲或内部结构不协调的问题
                - 评估姓名可能对性格形成的负面影响
                - 提供根据具体情况如何选择更为平衡的用字方案
            """
            
            else -> """
                - 提供全面、系统的分析，涵盖命理主要方面
                - 结合传统理论与现代解读，平衡古典智慧与实用性
                - 关注用户最关心的问题，提供针对性的建议
                - 包含近期运势变化与长期发展趋势的分析
                - 提供具体可行的改善建议和行动方向
                - 直接指出分析中发现的潜在风险和弱点
                - 客观评估用户面临的挑战和可能的阻碍
                - 避免过度乐观，保持客观平衡的分析态度
                - 提供实际的风险规避建议和应对策略
            """
        }
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
        
        // 添加负面特质描述
        val negativeTraits = listOf("固执", "急躁", "优柔寡断", "敏感", "多疑", "冲动", "自我中心", "过度理想化", "情绪化", "缺乏耐心")
        val selectedNegativeTraits = mutableListOf<String>()
        val negativeTraitCount = random.nextInt(2) + 1  // 1-2个负面特质
        for (i in 0 until negativeTraitCount) {
            val trait = negativeTraits[random.nextInt(negativeTraits.size)]
            if (!selectedNegativeTraits.contains(trait)) {
                selectedNegativeTraits.add(trait)
            }
        }
        sb.append("同时也存在${selectedNegativeTraits.joinToString("和")}等需要注意的性格特点。")
        
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
        
        // 添加事业挑战
        val careerChallenges = listOf(
            "工作中容易因${selectedNegativeTraits.firstOrNull() ?: "急躁"}而影响决策质量",
            "事业发展可能遇到来自同行的强烈竞争",
            "工作压力较大，需注意调整心态",
            "容易被琐事分散注意力，影响效率",
            "职场人际关系需要更多耐心经营"
        )
        sb.append("需要注意的是，" + careerChallenges[random.nextInt(careerChallenges.size)] + "。")
        
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
        
        val wealthRisks = listOf(
            "投资需谨慎，近期有财务损失风险",
            "财务规划欠缺系统性，易造成资源浪费",
            "消费习惯需要调整，避免冲动消费",
            "财运虽好但守财能力较弱",
            "容易受他人影响做出不理性的财务决策"
        )
        sb.append("但也要注意，" + wealthRisks[random.nextInt(wealthRisks.size)] + "。")
        
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
        
        // 添加关系挑战
        val relationshipChallenges = listOf(
            "感情中易因沟通不畅导致误会",
            "对伴侣期望过高容易造成压力",
            "缺乏表达情感的能力可能影响亲密关系",
            "个人独立性与亲密关系的平衡需要调整",
            "过去的感情阴影可能影响当前关系"
        )
        sb.append("需要克服的问题是，" + relationshipChallenges[random.nextInt(relationshipChallenges.size)] + "。")
        
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
        
        val healthConcerns = listOf(
            "生活作息不规律可能导致${weakBodyPart}问题",
            "工作压力过大，注意预防精神紧张引发的身体不适",
            "饮食结构不合理可能导致营养失衡",
            "缺乏运动习惯，可能影响长期健康",
            "易忽视小症状，建议定期体检"
        )
        sb.append("值得警惕的是，" + healthConcerns[random.nextInt(healthConcerns.size)] + "。")
        
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
        
        // 对于占星学方法，需要特殊处理星盘描述
        if (method.id == "astrology") {
            // 尝试提取星盘描述部分
            val astrologyDescription = extractAstrologyChartDescription(response)
            
            // 如果提取到星盘描述，添加到结果部分的开头
            if (astrologyDescription.isNotEmpty()) {
                sections.add(0, ResultSection("星盘描述", astrologyDescription))
            }
        }
        
        // 创建算命结果
        return DivinationResult(
            id = UUID.randomUUID().toString(),
            methodId = method.id,
            createTime = Date(),
            inputData = inputData,
            resultSections = sections
        )
    }
    
    /**
     * 提取占星学星盘描述
     */
    private fun extractAstrologyChartDescription(response: String): String {
        val planetPositions = mutableListOf<String>()
        val aspectRelations = mutableListOf<String>()
        
        // 星盘位置的正则表达式模式
        val planetPattern = "(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星|上升点|中天点)\\s*位于\\s*(白羊|金牛|双子|巨蟹|狮子|处女|天秤|天蝎|射手|摩羯|水瓶|双鱼)座\\s*(\\d+)\\s*度".toRegex()
        
        // 相位关系的正则表达式模式
        val aspectPattern = "(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星|上升点|中天点)\\s*和\\s*(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星|上升点|中天点)\\s*形成\\s*(三分相|六分相|四分相|对分相|合相)\\s*\\((\\d+)度\\)".toRegex()
        
        // 查找所有行星位置
        val planetMatches = planetPattern.findAll(response)
        planetMatches.forEach { match ->
            val planet = match.groupValues[1]
            val zodiac = match.groupValues[2]
            val degree = match.groupValues[3]
            
            // 添加到位置列表
            planetPositions.add("$planet位于$zodiac座$degree度")
        }
        
        // 查找所有相位关系
        val aspectMatches = aspectPattern.findAll(response)
        aspectMatches.forEach { match ->
            val planet1 = match.groupValues[1]
            val planet2 = match.groupValues[2]
            val aspectType = match.groupValues[3]
            val degree = match.groupValues[4]
            
            // 添加到相位列表
            aspectRelations.add("$planet1和$planet2形成$aspectType($degree度)")
        }
        
        // 如果没有找到行星位置数据，尝试用更通用的模式再次查找
        if (planetPositions.isEmpty()) {
            val simplePlanetPattern = "(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星|上升点|中天点).*?(白羊|金牛|双子|巨蟹|狮子|处女|天秤|天蝎|射手|摩羯|水瓶|双鱼)座.*?(\\d+)[°度]".toRegex()
            val simpleMatches = simplePlanetPattern.findAll(response)
            simpleMatches.forEach { match ->
                val planet = match.groupValues[1]
                val zodiac = match.groupValues[2]
                val degree = match.groupValues[3]
                
                // 添加到位置列表
                planetPositions.add("$planet位于$zodiac座$degree度")
            }
        }
        
        // 如果没有找到相位关系数据，尝试用更通用的模式再次查找
        if (aspectRelations.isEmpty()) {
            val simpleAspectPattern = "(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星).*?(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星).*?(三分相|六分相|四分相|合相|对分相)".toRegex()
            val simpleMatches = simpleAspectPattern.findAll(response)
            simpleMatches.forEach { match ->
                val planet1 = match.groupValues[1]
                val planet2 = match.groupValues[2]
                val aspectType = match.groupValues[3]
                
                // 根据相位类型确定度数
                val degree = when (aspectType) {
                    "三分相" -> "120"
                    "六分相" -> "60"
                    "四分相" -> "90"
                    "对分相", "反对相" -> "180"
                    "合相" -> "0"
                    else -> "0"
                }
                
                // 添加到相位列表
                aspectRelations.add("$planet1和$planet2形成$aspectType($degree度)")
            }
        }
        
        // 构建最终的星盘描述
        val sb = StringBuilder()
        
        // 行星位置部分
        if (planetPositions.isNotEmpty()) {
            sb.append("行星位置：\n")
            planetPositions.forEach { sb.append("* $it\n") }
        }
        
        // 相位关系部分
        if (aspectRelations.isNotEmpty()) {
            sb.append("\n行星相位：\n")
            aspectRelations.forEach { sb.append("* $it\n") }
        }
        
        return sb.toString()
    }
}