package com.example.divination.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentHomeBinding
import com.example.divination.model.ChineseAlmanac
import com.example.divination.ui.CustomDatePickerDialog
import com.example.divination.utils.ChineseAlmanacService
import java.util.Calendar
import java.util.Date

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private var currentDate: Date = Date()
    private lateinit var almanac: ChineseAlmanac

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化老黄历数据
        loadAlmanacData(currentDate)
        
        // 设置日期选择按钮点击事件
        binding.btnChangeDate.setOnClickListener {
            showDatePicker()
        }
        
        // 设置推荐算命方式点击事件
        binding.cardChineseFortune.setOnClickListener {
            // 切换底部导航到算命页面
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_divination
        }
        
        binding.cardWesternFortune.setOnClickListener {
            // 切换底部导航到算命页面
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_divination
        }
    }
    
    private fun loadAlmanacData(date: Date) {
        // 获取指定日期的老黄历数据
        almanac = ChineseAlmanacService.getAlmanacForDate(date)
        
        // 更新界面显示
        updateAlmanacUI()
    }
    
    private fun updateAlmanacUI() {
        // 日期和基本信息
        binding.tvSolarDate.text = ChineseAlmanacService.formatDate(almanac.date)
        binding.tvLunarDate.text = "农历 ${almanac.lunarDate}"
        binding.tvChineseZodiac.text = "生肖：${almanac.chineseZodiac}"
        binding.tvFiveElements.text = "五行：${almanac.fiveElements}"
        
        // 宜忌事项
        binding.tvGoodActivities.text = almanac.goodActivities.joinToString(" ")
        binding.tvBadActivities.text = almanac.badActivities.joinToString(" ")
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        
        // 使用自定义的日期选择对话框，禁用年份修改功能
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            currentDate = selectedCalendar.time
            loadAlmanacData(currentDate)
        }
        
        val datePickerDialog = CustomDatePickerDialog(
            requireContext(),
            datePickerListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 