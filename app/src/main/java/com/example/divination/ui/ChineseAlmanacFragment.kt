package com.example.divination.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.divination.databinding.FragmentChineseAlmanacBinding
import com.example.divination.model.ChineseAlmanac
import com.example.divination.utils.ChineseAlmanacService
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class ChineseAlmanacFragment : Fragment() {

    private var _binding: FragmentChineseAlmanacBinding? = null
    private val binding get() = _binding!!
    
    private var currentDate: Date = Date()
    private lateinit var almanac: ChineseAlmanac

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChineseAlmanacBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 检查是否有传入的日期参数
        arguments?.getString(ARG_DATE)?.let { dateString ->
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                currentDate = sdf.parse(dateString) ?: Date()
            } catch (e: Exception) {
                // 解析失败，使用当前日期
                currentDate = Date()
            }
        }
        
        setupCalendarView()
        loadAlmanacData(currentDate)
    }

    private fun setupCalendarView() {
        // 通过创建子类覆盖原有日历行为来禁用年份修改功能
        val calendarView = binding.calendarView
        
        // 设置日历初始日期为传入的日期或当前日期
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendarView.date = calendar.timeInMillis
        
        // 监听日期变更事件
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            currentDate = calendar.time
            loadAlmanacData(currentDate)
        }
        
        // 阻止CalendarView中的年份编辑事件
        calendarView.setOnLongClickListener {
            // 拦截长按事件，防止触发年份修改
            true
        }
    }
    
    private fun loadAlmanacData(date: Date) {
        // 获取指定日期的老黄历数据
        almanac = ChineseAlmanacService.getAlmanacForDate(date)
        
        // 更新界面显示
        updateUI()
    }
    
    private fun updateUI() {
        // 日期和基本信息
        binding.tvSolarDate.text = ChineseAlmanacService.formatDate(almanac.date)
        binding.tvLunarDate.text = "农历 ${almanac.lunarDate}"
        binding.tvChineseZodiac.text = "生肖：${almanac.chineseZodiac}"
        binding.tvFiveElements.text = "五行：${almanac.fiveElements}"
        
        // 宜忌事项
        binding.tvGoodActivities.text = almanac.goodActivities.joinToString(" ")
        binding.tvBadActivities.text = almanac.badActivities.joinToString(" ")
        
        // 方位吉凶
        updateDirectionUI()
        
        // 其他信息
        binding.tvGodOfBaby.text = "胎神：${almanac.godOfBaby}"
        binding.tvChongSha.text = "冲煞：${almanac.chongSha}"
        binding.tvYearStar.text = "值年星：${almanac.yearStar}"
        binding.tvDayStar.text = "值日星：${almanac.dayStar}"
    }
    
    private fun updateDirectionUI() {
        // 更新方位信息
        updateDirectionText(binding.tvDirectionEast, "东", almanac.direction["东"] ?: "")
        updateDirectionText(binding.tvDirectionSouth, "南", almanac.direction["南"] ?: "")
        updateDirectionText(binding.tvDirectionWest, "西", almanac.direction["西"] ?: "")
        updateDirectionText(binding.tvDirectionNorth, "北", almanac.direction["北"] ?: "")
        updateDirectionText(binding.tvDirectionNortheast, "东北", almanac.direction["东北"] ?: "")
        updateDirectionText(binding.tvDirectionSoutheast, "东南", almanac.direction["东南"] ?: "")
        updateDirectionText(binding.tvDirectionSouthwest, "西南", almanac.direction["西南"] ?: "")
        updateDirectionText(binding.tvDirectionNorthwest, "西北", almanac.direction["西北"] ?: "")
    }
    
    private fun updateDirectionText(textView: TextView, direction: String, luck: String) {
        textView.text = "$direction：$luck"
        
        // 根据吉凶设置颜色
        if (luck == "吉") {
            textView.setBackgroundColor(requireContext().getColor(android.R.color.holo_green_light))
        } else {
            textView.setBackgroundColor(requireContext().getColor(android.R.color.holo_red_light))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        const val ARG_DATE = "date"
        
        fun newInstance(): ChineseAlmanacFragment {
            return ChineseAlmanacFragment()
        }
        
        fun newInstance(date: String): ChineseAlmanacFragment {
            val fragment = ChineseAlmanacFragment()
            val args = Bundle()
            args.putString(ARG_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }
} 