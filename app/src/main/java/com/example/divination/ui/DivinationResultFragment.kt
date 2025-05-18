package com.example.divination.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentDivinationResultBinding
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import com.example.divination.utils.DivinationMethodProvider
import com.example.divination.utils.LocalStorageService
import java.text.SimpleDateFormat
import java.util.Locale

class DivinationResultFragment : Fragment() {

    private var _binding: FragmentDivinationResultBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var resultId: String
    private var result: DivinationResult? = null
    
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
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadResult()
        setupShareButton()
    }
    
    private fun loadResult() {
        // 从本地存储加载结果
        result = LocalStorageService.getResult(requireContext(), resultId)
        
        if (result == null) {
            showError("结果不存在")
            parentFragmentManager.popBackStack()
            return
        }
        
        // 显示结果
        displayResult()
    }
    
    private fun displayResult() {
        result?.let { result ->
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
            result.resultSections.forEach { section ->
                addResultSection(section)
            }
        }
    }
    
    private fun addResultSection(section: ResultSection) {
        val sectionView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_result_section, binding.resultSectionsContainer, false)
        
        val titleView = sectionView.findViewById<android.widget.TextView>(R.id.tvSectionTitle)
        val contentView = sectionView.findViewById<android.widget.TextView>(R.id.tvSectionContent)
        val scoreView = sectionView.findViewById<android.widget.TextView>(R.id.tvSectionScore)
        
        titleView.text = section.title
        
        // 处理内容中的年份数字，使其更加突出
        val content = section.content
        
        // 检查是否是星盘描述部分，或者是否是占星学结果
        val isAstrologyResult = result?.methodId == "astrology"
        val isChartDescription = section.title == "星盘描述"
        
        if (isChartDescription || (isAstrologyResult && section.title != "建议")) {
            // 对于星盘描述或占星学结果的非建议部分，不高亮年份
            contentView.text = content
        } else {
            // 对于其他结果，高亮年份
            val highlightedContent = highlightYearNumbers(content)
            contentView.text = android.text.Html.fromHtml(highlightedContent, android.text.Html.FROM_HTML_MODE_COMPACT)
            
            // 仅为非占星学结果添加年份点击事件
            if (!isAstrologyResult) {
                // 添加TextView的点击事件监听
                contentView.setOnClickListener {
                    handleYearClick(contentView.text.toString())
                }
            }
        }
        
        // 显示或隐藏评分
        if (section.score >= 0) {
            scoreView.visibility = View.VISIBLE
            scoreView.text = "${section.score}/100"
        } else {
            scoreView.visibility = View.GONE
        }
        
        binding.resultSectionsContainer.addView(sectionView)
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
    
    /**
     * 处理年份点击事件
     */
    private fun handleYearClick(content: String) {
        // 匹配类似"2023年"或"2023-2025年"的年份表达式
        val yearPattern = "(\\d{4})(-\\d{4})?年".toRegex()
        val matcher = yearPattern.find(content)
        
        matcher?.let {
            val yearText = it.value
            // 创建一个EditText用于输入新的年份
            val editText = android.widget.EditText(requireContext()).apply {
                setText(yearText)
                // 设置输入类型为数字
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }
            
            // 显示年份选择对话框
            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setTitle("修改年份")
                .setMessage("您想要修改\"$yearText\"为：")
                .setView(editText)
                .setPositiveButton("确定") { _, _ ->
                    val newYear = editText.text.toString()
                    
                    // 检查输入的年份是否符合格式
                    if (newYear.matches("\\d{4}年".toRegex()) || newYear.matches("\\d{4}-\\d{4}年".toRegex())) {
                        // 更新当前结果中的年份
                        updateYearInResult(yearText, newYear)
                        
                        // 显示成功消息
                        Toast.makeText(requireContext(), "年份已修改为$newYear", Toast.LENGTH_SHORT).show()
                    } else {
                        // 格式不正确，提示用户
                        Toast.makeText(requireContext(), "年份格式不正确，请使用如'2024年'或'2024-2025年'的格式", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("取消", null)
                .create()
            
            dialog.show()
        }
    }
    
    /**
     * 更新结果中的年份
     */
    private fun updateYearInResult(oldYear: String, newYear: String) {
        // 获取当前的结果
        result?.let { currentResult ->
            // 创建一个新的结果部分列表，用于替换旧的
            val updatedSections = currentResult.resultSections.map { section ->
                // 替换内容中的年份
                val updatedContent = section.content.replace(oldYear, newYear)
                
                // 创建新的结果部分
                if (updatedContent != section.content) {
                    // 内容有更改，创建新的结果部分
                    ResultSection(
                        title = section.title,
                        content = updatedContent,
                        score = section.score
                    )
                } else {
                    // 内容没有更改，使用原来的结果部分
                    section
                }
            }
            
            // 使用新的结果部分列表创建新的结果对象
            val updatedResult = DivinationResult(
                id = currentResult.id,
                methodId = currentResult.methodId,
                createTime = currentResult.createTime,
                inputData = currentResult.inputData,
                resultSections = updatedSections
            )
            
            // 更新当前的结果
            result = updatedResult
            
            // 保存更新后的结果
            LocalStorageService.saveResult(requireContext(), updatedResult)
            
            // 重新显示结果
            binding.resultSectionsContainer.removeAllViews()
            updatedResult.resultSections.forEach { section ->
                addResultSection(section)
            }
        }
    }
    
    private fun setupShareButton() {
        binding.btnShare.setOnClickListener {
            // 分享结果
            val shareText = buildShareText()
            
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            
            startActivity(android.content.Intent.createChooser(shareIntent, "分享算命结果"))
        }
    }
    
    private fun buildShareText(): String {
        val sb = StringBuilder()
        
        result?.let { result ->
            // 获取算命方法信息
            val method = DivinationMethodProvider.getMethodById(result.methodId)
            
            sb.append("【${method?.name ?: "算命"}结果分享】\n\n")
            
            // 添加结果部分
            result.resultSections.forEach { section ->
                sb.append("▶ ${section.title}\n")
                sb.append("${section.content}\n\n")
            }
            
            sb.append("来自智能算命APP")
        }
        
        return sb.toString()
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 设置占星学星盘
     */
    private fun setupAstrologyChart(result: DivinationResult) {
        // 查找星盘描述部分
        val chartSection = result.resultSections.find { it.title == "星盘描述" }
        
        if (chartSection != null) {
            // 有星盘描述，显示星盘视图
            binding.astrologyChartView.visibility = View.VISIBLE
            
            // 添加日志用于调试
            android.util.Log.d("DivinationResult", "设置星盘数据: ${chartSection.content.take(100)}...")
            
            try {
                // 设置星盘数据
                binding.astrologyChartView.setChartData(chartSection.content)
            } catch (e: Exception) {
                // 捕获异常并记录
                android.util.Log.e("DivinationResult", "设置星盘数据失败", e)
                Toast.makeText(requireContext(), "星盘显示异常，请尝试重新生成", Toast.LENGTH_SHORT).show()
            }
            
            // 移除红色年份提示
            binding.tvResultDate.setOnClickListener(null)
        } else {
            // 没有星盘描述，隐藏星盘视图
            binding.astrologyChartView.visibility = View.GONE
            android.util.Log.d("DivinationResult", "找不到星盘描述部分")
            
            // 显示提示信息
            Toast.makeText(requireContext(), "未找到星盘描述数据", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 