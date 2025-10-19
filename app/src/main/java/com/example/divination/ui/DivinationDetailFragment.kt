package com.example.divination.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentDivinationDetailBinding
import com.example.divination.model.DivinationMethod
import com.example.divination.model.InputField
import com.example.divination.ui.CustomDatePickerDialog
import com.example.divination.utils.DeepSeekService
import com.example.divination.utils.DivinationMethodProvider
import com.example.divination.utils.LocalStorageService
import com.example.divination.utils.safePerformDivination
import java.util.*

class DivinationDetailFragment : Fragment() {

    private var _binding: FragmentDivinationDetailBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null, Fragment可能已被销毁")
    
    private lateinit var methodId: String
    private var method: DivinationMethod? = null
    private var isActive = true // 跟踪Fragment是否处于活跃状态
    
    companion object {
        private const val ARG_METHOD_ID = "method_id"
        
        fun newInstance(methodId: String): DivinationDetailFragment {
            val fragment = DivinationDetailFragment()
            val args = Bundle()
            args.putString(ARG_METHOD_ID, methodId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        methodId = arguments?.getString(ARG_METHOD_ID) ?: ""
        method = DivinationMethodProvider.getMethodById(methodId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDivinationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (method == null) {
            showError("算命方法不存在")
            return
        }
        
        setupUI()
        setupInputFields()
        setupSubmitButton()
    }
    
    private fun setupUI() {
        method?.let { method ->
            binding.tvMethodTitle.text = method.name
            binding.tvMethodDescription.text = method.description
            binding.ivMethodIcon.setImageResource(method.iconResId)
        }
    }
    
    private fun setupInputFields() {
        binding.inputFieldsContainer.removeAllViews()
        
        method?.inputFields?.forEach { field ->
            val fieldView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_input_field, binding.inputFieldsContainer, false)
            
            val labelView = fieldView.findViewById<android.widget.TextView>(R.id.tvFieldLabel)
            val valueView = fieldView.findViewById<android.widget.EditText>(R.id.etFieldValue)
            val spinnerLayout = fieldView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.spinnerLayout)
            val textInputLayout = fieldView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.textInputLayout)
            val spinnerView = fieldView.findViewById<AutoCompleteTextView>(R.id.spinnerFieldValue)
            
            labelView.text = field.name
            
            // 设置字段ID作为tag
            fieldView.tag = field.id
            
            when (field.type) {
                1 -> { // 文本
                    textInputLayout.isVisible = true
                    spinnerLayout.isVisible = false
                    valueView.setHint("请输入")
                }
                2 -> { // 日期
                    textInputLayout.isVisible = true
                    spinnerLayout.isVisible = false
                    valueView.setHint("选择日期")
                    valueView.isFocusableInTouchMode = false
                    valueView.isFocusable = false
                    valueView.isClickable = true
                    valueView.setOnClickListener {
                        showDatePicker(valueView)
                    }
                }
                3 -> { // 时间
                    textInputLayout.isVisible = true
                    spinnerLayout.isVisible = false
                    valueView.setHint("选择时间")
                    valueView.isFocusableInTouchMode = false
                    valueView.isFocusable = false
                    valueView.isClickable = true
                    valueView.setOnClickListener {
                        showTimePicker(valueView)
                    }
                }
                4 -> { // 选择
                    textInputLayout.isVisible = false
                    spinnerLayout.isVisible = true
                    
                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.item_dropdown_menu,
                        field.options
                    )
                    spinnerView.setAdapter(adapter)
                    spinnerView.setHint("请选择")
                }
            }
            
            binding.inputFieldsContainer.addView(fieldView)
        }
    }
    
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val inputData = collectInputData()
            
            // 验证输入
            if (!validateInput(inputData)) {
                showError("请填写所有必填项")
                return@setOnClickListener
            }
            
            // 对于塔罗牌，启动特殊的动画界面
            if (methodId == "tarot") {
                // 启动塔罗牌动画Fragment
                val tarotFragment = TarotAnimationFragment.newInstance(methodId, inputData)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, tarotFragment)
                    .addToBackStack(null)
                    .commit()
                return@setOnClickListener
            }
            
            // 对于老黄历，启动专门的老黄历界面
            if (methodId == "almanac") {
                // 获取用户选择的日期
                val selectedDate = inputData["date"] ?: ""
                // 启动老黄历Fragment，并传递日期参数
                val almanacFragment = ChineseAlmanacFragment.newInstance(selectedDate)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, almanacFragment)
                    .addToBackStack(null)
                    .commit()
                return@setOnClickListener
            }
            
            // 显示加载状态
            safeShowLoading(true)
            
            // 执行算命
            method?.let { method ->
                safePerformDivination(
                    requireContext(),
                    method,
                    inputData
                ) { result, error ->
                    // 检查Fragment是否仍然处于活跃状态，避免在Fragment销毁后更新UI
                    if (!isActive || !isAdded) {
                        return@safePerformDivination
                    }
                    
                    try {
                        safeShowLoading(false)
                    
                    if (error != null) {
                            safeShowError(error.message ?: "算命失败")
                    } else if (result != null) {
                        // 保存结果
                        LocalStorageService.saveResult(requireContext(), result)
                        
                            // 确保Fragment仍处于活跃状态
                            if (isActive && isAdded) {
                        // 显示结果
                        val resultFragment = DivinationResultFragment.newInstance(result.id)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, resultFragment)
                            .addToBackStack(null)
                            .commit()
                            }
                        }
                    } catch (e: Exception) {
                        // 捕获任何可能的异常，避免应用崩溃
                    }
                }
            }
        }
    }
    
    private fun collectInputData(): Map<String, String> {
        val data = mutableMapOf<String, String>()
        
        for (i in 0 until binding.inputFieldsContainer.childCount) {
            val fieldView = binding.inputFieldsContainer.getChildAt(i)
            val fieldId = fieldView.tag as? String ?: continue
            
            val valueView = fieldView.findViewById<android.widget.EditText>(R.id.etFieldValue)
            val spinnerView = fieldView.findViewById<AutoCompleteTextView>(R.id.spinnerFieldValue)
            
            val field = method?.inputFields?.find { it.id == fieldId }
            
            when (field?.type) {
                1, 2, 3 -> {
                    data[fieldId] = valueView.text.toString()
                }
                4 -> {
                    data[fieldId] = spinnerView.text.toString()
                }
            }
        }
        
        return data
    }
    
    private fun validateInput(inputData: Map<String, String>): Boolean {
        method?.inputFields?.forEach { field ->
            if (field.required && (inputData[field.id].isNullOrBlank())) {
                return false
            }
        }
        return true
    }
    
    private fun showDatePicker(valueView: android.widget.EditText) {
        val calendar = Calendar.getInstance()
        
        // 使用自定义的日期选择对话框，禁用年份修改功能
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            valueView.setText(selectedDate)
        }
        
        val datePickerDialog = CustomDatePickerDialog(
            requireContext(),
            datePickerListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // 设置标题，以明确用户正在选择日期而非修改年份
        datePickerDialog.setTitle("选择日期")
        
        datePickerDialog.show()
    }
    
    private fun showTimePicker(valueView: android.widget.EditText) {
        val calendar = Calendar.getInstance()
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = "$hourOfDay:$minute"
                valueView.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        
        timePickerDialog.show()
    }
    
    // 添加安全的UI更新方法
    private fun safeShowLoading(isLoading: Boolean) {
        if (!isActive || !isAdded || _binding == null) return
        try {
            // 显示/隐藏加载卡片
            binding.cardLoading.isVisible = isLoading
            binding.btnSubmit.isEnabled = !isLoading
            
            // 添加随机的加载提示文本
            if (isLoading) {
                binding.tvLoadingHint.text = getRandomLoadingHint()
                startLoadingAnimation()
            }
        } catch (e: Exception) {
            // 忽略可能的异常
        }
    }
    
    // 获取随机的加载提示文本
    private fun getRandomLoadingHint(): String {
        val hints = when (methodId) {
            "bazi" -> listOf(
                "🔮 正在推算八字命盘...",
                "⚡ 天干地支排列中...",
                "🌟 分析五行生克...",
                "✨ 计算喜用神...",
                "🎯 推演命运走势..."
            )
            "zhouyi" -> listOf(
                "🔮 正在起卦问天...",
                "☯ 推演卦象变化...",
                "📿 解析爻辞含义...",
                "✨ 占测吉凶祸福...",
                "🎋 参悟易经智慧..."
            )
            "tarot" -> listOf(
                "🔮 塔罗牌正在为您占卜...",
                "🃏 解读牌面能量...",
                "✨ 分析牌阵组合...",
                "🌙 感应宇宙讯息...",
                "💫 揭示命运真相..."
            )
            "astrology" -> listOf(
                "🔮 计算星盘位置...",
                "🌟 分析行星相位...",
                "✨ 推演星座能量...",
                "🌙 解读天象启示...",
                "💫 预测运势走向..."
            )
            "ziwei" -> listOf(
                "🔮 正在排紫微斗数盘...",
                "⭐ 推算命宫星曜...",
                "✨ 分析十二宫位...",
                "🌟 解读星耀组合...",
                "💫 预测人生格局..."
            )
            "dream" -> listOf(
                "🔮 正在解析梦境...",
                "💭 探索潜意识讯息...",
                "✨ 分析梦境象征...",
                "🌙 解读心灵密语...",
                "💫 揭示梦境真意..."
            )
            "numerology" -> listOf(
                "🔮 正在计算生命数字...",
                "🔢 分析数字能量...",
                "✨ 推演命运密码...",
                "💫 解读数字奥秘...",
                "🎯 揭示人生使命..."
            )
            else -> listOf(
                "🔮 AI正在为您推算命运...",
                "✨ 正在解析您的问题...",
                "💫 推演命运轨迹中...",
                "🌟 感应天地玄机...",
                "🎯 为您揭示未来..."
            )
        }
        return hints.random()
    }
    
    // 启动加载动画（可以在这里添加更多动画效果）
    private fun startLoadingAnimation() {
        if (!isActive || !isAdded || _binding == null) return
        try {
            // 为整个卡片添加淡入和缩放动画
            val fadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(
                requireContext(), 
                R.anim.fade_in
            )
            binding.cardLoading.startAnimation(fadeInAnimation)
            
            // 为进度条外圈添加旋转动画
            val rotateAnimation = android.view.animation.AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.rotate_animation
            )
            binding.progressBar.parent?.let { parent ->
                if (parent is android.view.View) {
                    parent.startAnimation(rotateAnimation)
                }
            }
            
            // 为提示文字添加脉动效果
            val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.pulse_animation
            )
            binding.tvLoadingHint.startAnimation(pulseAnimation)
            
            // 为三个小圆点添加错开的脉动动画
            animateLoadingDots()
        } catch (e: Exception) {
            // 忽略可能的异常
        }
    }
    
    // 为加载指示点添加错开的动画效果
    private fun animateLoadingDots() {
        if (!isActive || !isAdded || _binding == null) return
        try {
            val dot1 = binding.root.findViewById<View>(R.id.loadingDot1)
            val dot2 = binding.root.findViewById<View>(R.id.loadingDot2)
            val dot3 = binding.root.findViewById<View>(R.id.loadingDot3)
            
            // 为每个点创建脉动动画，但添加不同的延迟
            dot1?.let { animateDot(it, 0) }
            dot2?.let { animateDot(it, 200) }
            dot3?.let { animateDot(it, 400) }
        } catch (e: Exception) {
            // 忽略可能的异常
        }
    }
    
    // 为单个圆点添加动画
    private fun animateDot(dot: View, startDelay: Long) {
        dot.postDelayed({
            if (isActive && isAdded && _binding != null) {
                dot.animate()
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .alpha(0.3f)
                    .setDuration(600)
                    .withEndAction {
                        if (isActive && isAdded && _binding != null) {
                            dot.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1f)
                                .setDuration(600)
                                .withEndAction {
                                    // 循环动画
                                    if (isActive && isAdded && _binding != null && binding.cardLoading.isVisible) {
                                        animateDot(dot, 0)
                                    }
                                }
                                .start()
                        }
                    }
                    .start()
            }
        }, startDelay)
    }
    
    // 获取当前算命方法的超时时间（秒）
    private fun getMethodTimeout(): Int {
        return when (methodId) {
            "bazi" -> 60
            "zhouyi", "ziwei", "qimen" -> 90
            "tarot", "astrology", "dream" -> 80 
            "face", "palmistry" -> 70
            "numerology", "almanac" -> 90
            else -> 60
        }
    }
    
    // 原始方法保留，但在内部使用安全版本
    private fun showLoading(isLoading: Boolean) {
        safeShowLoading(isLoading)
    }
    
    private fun safeShowError(message: String) {
        if (!isActive || !isAdded) return
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // 忽略可能的异常
        }
    }
    
    private fun showError(message: String) {
        safeShowError(message)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }
} 