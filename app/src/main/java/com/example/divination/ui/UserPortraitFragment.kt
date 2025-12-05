package com.example.divination.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentUserPortraitBinding
import com.example.divination.model.UserPortrait
import com.example.divination.utils.UserPortraitAnalyzer
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户画像Fragment - 简化版（不使用图表库）
 */
class UserPortraitFragment : Fragment() {
    
    private var _binding: FragmentUserPortraitBinding? = null
    private val binding get() = _binding!!
    
    private var userPortrait: UserPortrait? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserPortraitBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupEditButton()
        loadUserPortrait()
    }
    
    private fun setupEditButton() {
        binding.btnEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MoodHistoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    
    private fun loadUserPortrait() {
        userPortrait = UserPortraitAnalyzer.generateUserPortrait(requireContext())
        
        if (userPortrait == null) {
            showEmptyState()
            return
        }
        
        displayUserPortrait(userPortrait!!)
    }
    
    private fun showEmptyState() {
        binding.tvBasicInfo.text = "请先完善个人信息并使用算命功能"
        binding.llLevelInfo.isVisible = false
        binding.cardSmartTags.isVisible = false
        binding.cardPersonality.isVisible = false
        binding.cardPreferences.isVisible = false
        binding.cardFortuneTrend.isVisible = false
        binding.cardBehaviorStats.isVisible = false
    }
    
    private fun displayUserPortrait(portrait: UserPortrait) {
        displayBasicInfo(portrait)
        displayLevelInfo(portrait)
        displaySmartTags(portrait)
        displayPersonalityTraits(portrait)
        displayPreferences(portrait)
        displayFortuneTrend(portrait)
        displayBehaviorStats(portrait)
    }
    
    private fun displayBasicInfo(portrait: UserPortrait) {
        val profile = portrait.basicInfo
        val info = buildString {
            append("姓名：${profile.name}\n")
            append("性别：${profile.gender}\n")
            append("年龄：${profile.getAge()}岁\n")
            
            if (profile.zodiacSign.isNotEmpty()) {
                append("星座：${profile.zodiacSign}\n")
            }
            
            if (profile.chineseZodiac.isNotEmpty()) {
                append("生肖：${profile.chineseZodiac}\n")
            }
            
            if (profile.bloodType.isNotEmpty()) {
                append("血型：${profile.bloodType}\n")
            }
            
            if (profile.mbtiType.isNotEmpty()) {
                append("MBTI：${profile.mbtiType}")
            }
        }
        
        binding.tvBasicInfo.text = info
    }
    
    private fun displayLevelInfo(portrait: UserPortrait) {
        binding.llLevelInfo.isVisible = true
        binding.tvLevel.text = "Lv.${portrait.level}"
        binding.tvExperience.text = "经验值：${portrait.experience}"
        
        val nextLevelExp = portrait.level * 100
        val progress = ((portrait.experience % nextLevelExp) * 100 / nextLevelExp).coerceIn(0, 100)
        binding.progressLevel.progress = progress
    }
    
    private fun displaySmartTags(portrait: UserPortrait) {
        if (portrait.smartTags.isEmpty()) {
            binding.cardSmartTags.isVisible = false
            return
        }
        
        binding.cardSmartTags.isVisible = true
        binding.chipGroupTags.removeAllViews()
        
        portrait.smartTags.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.primary)
                setTextColor(Color.WHITE)
            }
            binding.chipGroupTags.addView(chip)
        }
    }
    
    private fun displayPersonalityTraits(portrait: UserPortrait) {
        if (portrait.personalityTraits.isEmpty()) {
            binding.cardPersonality.isVisible = false
            return
        }
        
        binding.cardPersonality.isVisible = true
        binding.chipGroupPersonality.removeAllViews()
        
        portrait.personalityTraits.forEach { trait ->
            val chip = Chip(requireContext()).apply {
                text = trait
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(android.R.color.holo_blue_light)
                setTextColor(Color.WHITE)
            }
            binding.chipGroupPersonality.addView(chip)
        }
    }
    
    private fun displayPreferences(portrait: UserPortrait) {
        val prefs = portrait.preferences
        if (prefs == null || prefs.totalCount == 0) {
            binding.cardPreferences.isVisible = false
            return
        }
        
        binding.cardPreferences.isVisible = true
        
        val summary = buildString {
            append("总算命次数：${prefs.totalCount}次\n")
            append("最爱方法：${prefs.favoriteMethod} (${prefs.favoriteMethodPercentage.toInt()}%)\n")
            append("活跃时段：${prefs.activeTimeSlot}\n\n")
            
            append("算命方法分布：\n")
            prefs.methodDistribution.entries.sortedByDescending { it.value }.forEach { (method, count) ->
                val percentage = (count * 100.0 / prefs.totalCount).toInt()
                append("  • ")
                append(method)
                append(": ")
                append(count)
                append("次 (")
                append(percentage)
                append("%)\n")
            }
        }
        binding.tvPreferencesSummary.text = summary
    }
    
    private fun displayFortuneTrend(portrait: UserPortrait) {
        val trend = portrait.fortuneTrend
        if (trend == null || trend.fortuneHistory.isEmpty()) {
            binding.cardFortuneTrend.isVisible = false
            return
        }
        
        binding.cardFortuneTrend.isVisible = true
        
        val summary = buildString {
            append("近${trend.period}平均运势：\n")
            append("  • 综合：${String.format("%.1f", trend.averageOverallLuck)}星\n")
            append("  • 爱情：${String.format("%.1f", trend.averageLoveLuck)}星\n")
            append("  • 事业：${String.format("%.1f", trend.averageCareerLuck)}星\n")
            append("  • 财运：${String.format("%.1f", trend.averageWealthLuck)}星\n")
            append("  • 健康：${String.format("%.1f", trend.averageHealthLuck)}星\n\n")
            append("幸运日：${trend.luckyDays}天 | 不利日：${trend.unluckyDays}天\n\n")
            
            append("最近运势记录：\n")
            trend.fortuneHistory.take(5).forEach { fortune ->
                val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fortune.date)
                append("  ${dateFormat.format(date!!)} 综合${fortune.overallLuck}星 ")
                append("爱${fortune.loveLuck}星 ")
                append("事${fortune.careerLuck}星 ")
                append("财${fortune.wealthLuck}星 ")
                append("健${fortune.healthLuck}星\n")
            }
        }
        binding.tvFortuneSummary.text = summary
    }
    
    private fun displayBehaviorStats(portrait: UserPortrait) {
        val stats = portrait.behaviorStats
        
        binding.cardBehaviorStats.isVisible = true
        
        val statsText = buildString {
            append("算命总次数：${stats.totalDivinationCount}次\n")
            append("连续登录：${stats.continuousLoginDays}天\n")
            append("总登录天数：${stats.totalLoginDays}天\n")
            
            if (stats.lastVisitTime != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                append("最后访问：${dateFormat.format(stats.lastVisitTime)}\n")
            }
            
            val avgMinutes = stats.averageSessionDuration / 1000 / 60
            append("平均停留：${avgMinutes}分钟")
        }
        
        binding.tvBehaviorStats.text = statsText
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
