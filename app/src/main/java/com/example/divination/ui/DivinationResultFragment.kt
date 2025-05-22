package com.example.divination.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.divination.R
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import com.example.divination.databinding.FragmentDivinationResultBinding
import com.example.divination.utils.DivinationMethodProvider
import com.example.divination.utils.LocalStorageService
import java.text.SimpleDateFormat
import java.util.*

class DivinationResultFragment : Fragment() {

    private var _binding: FragmentDivinationResultBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null, Fragment可能已被销毁")
    
    private lateinit var resultId: String
    private var result: DivinationResult? = null
    private var isActive = true // 跟踪Fragment是否处于活跃状态
    
    companion object {
        private const val ARG_RESULT_ID = "result_id"
        
        fun newInstance(resultId: String): DivinationResultFragment {
            val fragment = DivinationResultFragment()
            val args = Bundle()
            args.putString(ARG_RESULT_ID, resultId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultId = arguments?.getString(ARG_RESULT_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDivinationResultBinding.inflate(inflater, container, false)
        isActive = true
        
        // 初始化视图
        setupViews()
        
        // 处理传递的参数
        arguments?.let {
            val resultId = it.getString(ARG_RESULT_ID)
            if (resultId != null) {
                // 加载结果
                loadResult(resultId)
            } else {
                // 没有结果ID，显示错误
                showEmptyResultView("未找到算命结果ID")
            }
        } ?: showEmptyResultView("未找到算命结果")
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadResult()
        setupShareButton()
    }
    
    private fun loadResult(id: String) {
        resultId = id
        loadResult()
    }
    
    private fun loadResult() {
        try {
            // 从本地存储加载结果
            result = LocalStorageService.getResult(requireContext(), resultId)
            
            if (result == null) {
                safeShowError("结果不存在")
                try {
                    if (isActive && isAdded) {
                    parentFragmentManager.popBackStack()
                    }
                } catch (e: Exception) {
                    Log.e("DivinationResult", "返回上一页失败", e)
                    if (isActive && isAdded) {
                    activity?.finish()
                    }
                }
                return
            }
            
            // 显示结果
            displayResult()
        } catch (e: Exception) {
            Log.e("DivinationResult", "加载结果失败", e)
            safeShowError("加载结果失败: ${e.message}")
            try {
                if (isActive && isAdded) {
                parentFragmentManager.popBackStack()
                }
            } catch (ex: Exception) {
                Log.e("DivinationResult", "返回上一页失败", ex)
                if (isActive && isAdded) {
                activity?.finish()
                }
            }
        }
    }
    
    private fun displayResult() {
        try {
            // 如果结果为空，显示错误信息
            if (result == null) {
                showEmptyResultView("结果数据不存在")
                return
            }
            
            result?.let { result ->
                // 检查结果部分是否为空
                if (result.resultSections.isEmpty()) {
                    showEmptyResultView("结果内容为空")
                    return
                }
                
                // 获取算命方法信息
                val method = DivinationMethodProvider.getMethodById(result.methodId)
                
                // 设置标题
                binding.tvResultTitle.text = method?.name ?: "算命结果"
                
                // 设置日期
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                binding.tvResultDate.text = "分析时间：${dateFormat.format(result.createTime)}"
                
                // 检查是否是占星学结果，如果是则显示星盘
                if (method?.id == "astrology") {
                    setupAstrologyChart(result)
                } else {
                    // 非占星学结果，隐藏星盘视图
                    binding.astrologyChartView.visibility = View.GONE
                }
                
                // 清空结果容器
                binding.resultSectionsContainer.removeAllViews()
                
                // 添加结果部分
                try {
                    // 限制段落数量，防止过多内容导致性能问题
                    val maxSections = 10
                    val limitedSections = if (result.resultSections.size > maxSections) {
                        val limited = result.resultSections.take(maxSections).toMutableList()
                        limited.add(ResultSection("注意", "内容过长，已省略部分内容"))
                        limited
                    } else {
                        result.resultSections
                    }
                    
                    var hasAddedAnySection = false
                    
                    limitedSections.forEach { section ->
                        try {
                            if (section.content.isNotBlank()) {
                                addResultSection(section)
                                hasAddedAnySection = true
                            }
                        } catch (e: Exception) {
                            Log.e("DivinationResult", "添加结果段落失败: ${section.title}", e)
                        }
                    }
                    
                    // 检查是否添加了任何段落
                    if (!hasAddedAnySection) {
                        // 如果没有添加任何段落，显示备用内容
                        val fallbackSection = ResultSection(
                            title = "结果提示",
                            content = "未能显示有效内容。这可能是由于数据异常或格式问题导致。请返回重新尝试，或选择其他算命方式。"
                        )
                        addResultSection(fallbackSection)
                    }
                } catch (e: Exception) {
                    Log.e("DivinationResult", "处理结果段落失败", e)
                    showError("显示结果内容失败")
                    
                    // 添加错误信息到结果容器
                    val errorSection = ResultSection(
                        title = "错误提示",
                        content = "显示结果内容时发生错误：${e.message}\n\n请返回重新尝试，或选择其他算命方式。"
                    )
                    addResultSection(errorSection)
                }
            }
        } catch (e: Exception) {
            Log.e("DivinationResult", "显示结果失败", e)
            showError("显示结果失败: ${e.message}")
            showEmptyResultView("显示结果时发生错误")
        }
    }
    
    private fun addResultSection(section: ResultSection) {
        // 检查内容是否为空
        if (section.content.isEmpty() || section.content.isBlank()) {
            Log.d("DivinationResult", "跳过空内容部分: ${section.title}")
            return
        }
        
        try {
            val sectionView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_result_section, binding.resultSectionsContainer, false)
            
            val titleView = sectionView.findViewById<android.widget.TextView>(R.id.tvSectionTitle)
            val contentView = sectionView.findViewById<android.widget.TextView>(R.id.tvSectionContent)
            val scoreView = sectionView.findViewById<android.widget.TextView>(R.id.tvSectionScore)
            
            // 设置标题，确保不为空
            titleView.text = if (section.title.isNotBlank()) section.title else "内容"
            
            // 处理内容中的年份数字，使其更加突出
            val content = section.content
            
            try {
                // 使用我们增强的格式化方法
                val formattedContent = formatContent(content)
                
                // 设置HTML格式文本
                contentView.text = HtmlCompat.fromHtml(formattedContent, HtmlCompat.FROM_HTML_MODE_COMPACT)
                
                // 激活链接点击
                contentView.movementMethod = LinkMovementMethod.getInstance()
            } catch (e: Exception) {
                // 如果格式化失败，直接显示原始内容
                Log.e("DivinationResult", "格式化内容失败，显示原始内容", e)
                contentView.text = content
            }
            
            // 设置评分（如果有）
            if (section.score > 0) {
                scoreView.text = "${section.score}分"
                scoreView.visibility = View.VISIBLE
            } else {
                scoreView.visibility = View.GONE
            }
            
            // 添加到容器
            binding.resultSectionsContainer.addView(sectionView)
            
        } catch (e: Exception) {
            Log.e("DivinationResult", "添加部分失败: ${section.title}", e)
            // 出错时不中断整个流程
        }
    }
    
    /**
     * 格式化内容，增加排版效果
     */
    private fun formatContent(content: String): String {
        try {
            // 检查内容是否为空
            if (content.isEmpty()) {
                return "<div style=\"color:#757575;\">（无内容）</div>"
            }
            
            // 如果内容过短，直接返回简单格式
            if (content.length < 10) {
                return "<div style=\"line-height:1.5; font-size:16px;\">${content}</div>"
            }
            
            // 限制内容长度，防止处理过大的文本
            val limitedContent = if (content.length > 10000) content.substring(0, 10000) + "..." else content
            
            // 初始化格式化文本
            var formattedText = limitedContent
            
            try {
                // 0. 添加基本的样式包装
                formattedText = "<div style=\"line-height:1.5; font-size:16px;\">\n$formattedText\n</div>"
                
                // 1. 处理标题和副标题（#开头的行）- 简化处理逻辑
                formattedText = formattedText.replace(Regex("(^|\n)# (.+?)($|\n)")) { matchResult ->
                    "\n<div style=\"margin-top:15px; margin-bottom:10px; color:#0D47A1;\"><strong>${matchResult.groupValues[2]}</strong></div>\n"
                }
                
                // 2. 处理强调文本（**文本**或者*文本*）- 简化处理逻辑
                formattedText = formattedText.replace(Regex("\\*\\*([^*]+?)\\*\\*")) { matchResult ->
                    "<strong>${matchResult.groupValues[1]}</strong>"
                }
                formattedText = formattedText.replace(Regex("\\*([^*]+?)\\*")) { matchResult ->
                    "<em>${matchResult.groupValues[1]}</em>"
                }
                
                // 3. 简化段落处理：按换行符分割
                formattedText = formattedText.replace("\n", "<br>")
                
                // 4. 处理可能的连续换行符
                formattedText = formattedText.replace(Regex("<br><br>+"), "<br><br>")
                
                // 5. 处理年份，但使用简化的方法
                formattedText = formattedText.replace(Regex("(\\d{4})年")) { matchResult ->
                    "<span style=\"color:#FF6F00;\">${matchResult.value}</span>"
                }
            } catch (e: Exception) {
                Log.e("DivinationResult", "格式化文本处理失败，保留基本格式", e)
                // 如果处理失败，至少确保基本格式存在
                formattedText = "<div style=\"line-height:1.5; font-size:16px;\">\n$limitedContent\n</div>"
            }
            
            return formattedText
        } catch (e: Exception) {
            // 捕获处理过程中的任何异常，返回一个安全的内容
            Log.e("DivinationResult", "格式化内容出现严重错误", e)
            return "<div style=\"color:red;\">内容显示异常，请尝试重新生成结果</div>"
        }
    }
    
    /**
     * 高亮内容中的年份数字
     */
    private fun highlightYearNumbers(content: String): String {
        // 匹配类似"2023年"或"2023-2025年"的年份表达式
        val yearPattern = "(\\d{4})(-\\d{4})?年".toRegex()
        
        // 将匹配到的年份替换为带有样式的HTML
        val result = content.replace(yearPattern) { matchResult ->
            "<font color='#D32F2F'><b><big><u>${matchResult.value}</u></big></b></font>"
        }
        
        return result
    }
    
    private fun setupShareButton() {
        binding.btnShare.setOnClickListener {
            // 分享结果
            val shareText = buildShareText()
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            
            startActivity(Intent.createChooser(shareIntent, "分享算命结果"))
        }
    }
    
    /**
     * 构建用于分享的文本
     */
    private fun buildShareText(): String {
        val sb = StringBuilder()
        
        result?.let { result ->
            // 获取算命方法信息
            val method = DivinationMethodProvider.getMethodById(result.methodId)
            
            sb.append("【${method?.name ?: "算命"}结果分享】\n\n")
            
            // 添加结果部分
            result.resultSections.forEach { section ->
                sb.append("▶ ${section.title}\n")
                
                // 处理格式化的内容
                val formattedContent = formatShareContent(section.content)
                sb.append("${formattedContent}\n\n")
            }
            
            sb.append("来自智能算命APP")
        }
        
        return sb.toString()
    }
    
    /**
     * 格式化分享内容文本
     */
    private fun formatShareContent(content: String): String {
        var formattedContent = content
        
        // 1. 处理Markdown标题符号
        formattedContent = formattedContent.replace(Regex("(^|\n)#{1,3} (.+?)($|\n)")) { matchResult ->
            "\n◆ ${matchResult.groupValues[2]}\n"
        }
        
        // 2. 处理分隔线
        formattedContent = formattedContent.replace(Regex("(^|\n)[-]{3,}($|\n)")) { 
            "\n---------------------\n" 
        }
        
        // 3. 处理强调文本
        formattedContent = formattedContent.replace(Regex("\\*\\*(.+?)\\*\\*")) { matchResult ->
            "★${matchResult.groupValues[1]}★"
        }
        formattedContent = formattedContent.replace(Regex("\\*(.+?)\\*")) { matchResult ->
            "『${matchResult.groupValues[1]}』"
        }
        
        // 4. 处理列表
        formattedContent = formattedContent.replace(Regex("(^|\n)(\\d+)\\. (.+?)($|\n)")) { matchResult ->
            "\n  ${matchResult.groupValues[2]}. ${matchResult.groupValues[3]}\n"
        }
        formattedContent = formattedContent.replace(Regex("(^|\n)- (.+?)($|\n)")) { matchResult ->
            "\n  • ${matchResult.groupValues[2]}\n"
        }
        
        // 5. 处理引言和特殊标记
        formattedContent = formattedContent.replace(Regex("「(.+?)」")) { matchResult ->
            "「${matchResult.groupValues[1]}」"
        }
        
        // 6. 处理关键短语
        val keywords = arrayOf(
            "卦象含义", "财富机遇分析", "风险提示", "性格短板", 
            "外部阻碍", "具体策略", "经典引用", "总结"
        )
        
        for (keyword in keywords) {
            formattedContent = formattedContent.replace("$keyword：", "\n◆ $keyword：\n")
            formattedContent = formattedContent.replace("$keyword:", "\n◆ $keyword：\n")
        }
        
        // 7. 移除多余换行
        formattedContent = formattedContent.replace(Regex("\n{3,}"), "\n\n")
        
        return formattedContent
    }
    
    /**
     * 显示错误提示
     */
    private fun safeShowError(message: String) {
        if (!isActive || !isAdded || _binding == null) return
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DivinationResult", "显示错误失败", e)
        }
    }
    
    private fun showError(message: String) {
        safeShowError(message)
    }
    
    /**
     * 设置占星学星盘
     */
    private fun setupAstrologyChart(result: DivinationResult) {
        try {
            // 添加诊断日志
            Log.d("DivinationResult", "开始设置占星学星盘")
            
            // 打印所有可用的section标题，用于诊断
            val sectionTitles = result.resultSections.map { it.title }
            Log.d("DivinationResult", "可用的结果部分标题: ${sectionTitles.joinToString(", ")}")
            
            // 查找星盘描述部分，尝试多种可能的标题
            var chartSection = result.resultSections.find { 
                it.title == "星盘描述" || it.title == "星盘信息" || it.title.contains("星盘") 
            }
            
            // 如果没有找到，尝试使用第一个部分作为星盘描述
            if (chartSection == null && result.resultSections.isNotEmpty()) {
                chartSection = result.resultSections[0]
                Log.d("DivinationResult", "未找到明确的星盘描述部分，使用第一部分: ${chartSection.title}")
            }
            
            if (chartSection != null) {
                // 有星盘描述，显示星盘视图
                binding.astrologyChartView.visibility = View.VISIBLE
                
                // 添加日志用于调试
                Log.d("DivinationResult", "找到星盘描述部分，长度: ${chartSection.content.length}")
                
                try {
                    // 直接在主线程设置星盘数据
                    val content = chartSection.content
                    
                    // 记录星盘内容前100个字符
                    Log.d("DivinationResult", "星盘内容预览: ${content.take(100)}...")
                    
                    // 设置星盘数据，如果失败再尝试使用默认数据
                    try {
                        binding.astrologyChartView.setChartData(content)
                    } catch (e: Exception) {
                        Log.e("DivinationResult", "设置星盘数据失败，尝试使用默认数据", e)
                        binding.astrologyChartView.useDefaultChartData()
                        binding.astrologyChartView.invalidate()
                    }
                } catch (e: Exception) {
                    Log.e("DivinationResult", "星盘数据处理失败，尝试使用默认数据", e)
                    binding.astrologyChartView.useDefaultChartData()
                    binding.astrologyChartView.invalidate()
                }
            } else {
                // 没有星盘描述，但仍然显示默认星盘视图
                binding.astrologyChartView.visibility = View.VISIBLE
                Log.d("DivinationResult", "找不到星盘描述部分，使用默认星盘数据")
                binding.astrologyChartView.useDefaultChartData()
                binding.astrologyChartView.invalidate()
            }
        } catch (e: Exception) {
            // 捕获所有异常，防止闪退
            Log.e("DivinationResult", "星盘设置过程发生异常，尝试使用默认星盘数据", e)
            try {
                binding.astrologyChartView.visibility = View.VISIBLE
                binding.astrologyChartView.useDefaultChartData()
                binding.astrologyChartView.invalidate()
            } catch (ex: Exception) {
                Log.e("DivinationResult", "无法显示默认星盘", ex)
                binding.astrologyChartView.visibility = View.GONE
            }
        }
    }
    
    /**
     * 显示空结果视图
     */
    private fun showEmptyResultView(message: String) {
        if (!isActive || !isAdded || _binding == null) return
        try {
            // 清空结果容器
            binding.resultSectionsContainer.removeAllViews()
            
            // 设置基本信息
            binding.tvResultTitle.text = "结果查看"
            binding.tvResultDate.text = "分析时间：${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}"
            
            // 隐藏星盘视图
            binding.astrologyChartView.visibility = View.GONE
            
            // 添加错误信息段落
            val errorSection = ResultSection(
                title = "提示信息",
                content = "${message}\n\n可能原因：\n1. 数据加载失败\n2. 网络连接问题\n3. 结果生成异常\n\n建议返回重新尝试，或选择其他算命方式。"
            )
            addResultSection(errorSection)
        } catch (e: Exception) {
            Log.e("DivinationResult", "显示空结果视图失败", e)
            safeShowError("显示错误信息也失败了，请返回重试")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }
    
    /**
     * 初始化视图
     */
    private fun setupViews() {
        // 无需设置返回按钮，因为布局中没有该按钮
        
        // 设置分享按钮
        binding.btnShare?.setOnClickListener {
            shareResult()
        }
        
        // 初始化占星学星盘视图
        try {
            binding.astrologyChartView.apply {
                // 默认隐藏星盘视图
                visibility = View.GONE
                
                // 设置最小高度和宽度
                minimumHeight = resources.getDimensionPixelSize(R.dimen.astrology_chart_min_height)
                minimumWidth = resources.getDimensionPixelSize(R.dimen.astrology_chart_min_width)
                
                // 设置可点击
                isClickable = true
                
                // 添加点击事件
                setOnClickListener {
                    // 点击时放大显示星盘（扩展功能，未实现）
                    Toast.makeText(context, "星盘详情功能正在开发中", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("DivinationResult", "初始化占星学星盘视图失败", e)
        }
    }

    /**
     * 分享结果
     */
    private fun shareResult() {
        // 分享结果
        val shareText = buildShareText()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        
        startActivity(Intent.createChooser(shareIntent, "分享算命结果"))
    }
} 