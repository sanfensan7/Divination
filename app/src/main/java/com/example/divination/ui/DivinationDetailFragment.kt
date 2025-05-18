package com.example.divination.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentDivinationDetailBinding
import com.example.divination.model.DivinationMethod
import com.example.divination.model.InputField
import com.example.divination.utils.DeepSeekService
import com.example.divination.utils.DivinationMethodProvider
import com.example.divination.utils.LocalStorageService
import java.util.*

class DivinationDetailFragment : Fragment() {

    private var _binding: FragmentDivinationDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var methodId: String
    private var method: DivinationMethod? = null
    
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
            val spinnerView = fieldView.findViewById<android.widget.Spinner>(R.id.spinnerFieldValue)
            
            labelView.text = field.name
            
            // 设置字段ID作为tag
            fieldView.tag = field.id
            
            when (field.type) {
                1 -> { // 文本
                    valueView.isVisible = true
                    spinnerView.isVisible = false
                }
                2 -> { // 日期
                    valueView.isVisible = true
                    spinnerView.isVisible = false
                    valueView.hint = "选择日期"
                    valueView.isFocusable = false
                    valueView.setOnClickListener {
                        showDatePicker(valueView)
                    }
                }
                3 -> { // 时间
                    valueView.isVisible = true
                    spinnerView.isVisible = false
                    valueView.hint = "选择时间"
                    valueView.isFocusable = false
                    valueView.setOnClickListener {
                        showTimePicker(valueView)
                    }
                }
                4 -> { // 选择
                    valueView.isVisible = false
                    spinnerView.isVisible = true
                    
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        field.options
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerView.adapter = adapter
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
                // 启动老黄历Fragment
                val almanacFragment = ChineseAlmanacFragment.newInstance()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, almanacFragment)
                    .addToBackStack(null)
                    .commit()
                return@setOnClickListener
            }
            
            // 显示加载状态
            showLoading(true)
            
            // 执行算命
            method?.let { method ->
                DeepSeekService.performDivination(
                    requireContext(),
                    method,
                    inputData
                ) { result, error ->
                    showLoading(false)
                    
                    if (error != null) {
                        showError(error.message ?: "算命失败")
                    } else if (result != null) {
                        // 保存结果
                        LocalStorageService.saveResult(requireContext(), result)
                        
                        // 显示结果
                        val resultFragment = DivinationResultFragment.newInstance(result.id)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, resultFragment)
                            .addToBackStack(null)
                            .commit()
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
            val spinnerView = fieldView.findViewById<android.widget.Spinner>(R.id.spinnerFieldValue)
            
            val field = method?.inputFields?.find { it.id == fieldId }
            
            when (field?.type) {
                1, 2, 3 -> {
                    data[fieldId] = valueView.text.toString()
                }
                4 -> {
                    data[fieldId] = spinnerView.selectedItem.toString()
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
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnSubmit.isEnabled = !isLoading
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 