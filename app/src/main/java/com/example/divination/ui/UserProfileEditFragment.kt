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
import com.example.divination.databinding.FragmentUserProfileBinding
import com.example.divination.model.UserProfile
import com.example.divination.utils.DailyFortuneGenerator
import com.example.divination.utils.FortuneHistoryService
import com.example.divination.utils.UserProfileService
import java.util.*

/**
 * 用户信息编辑Fragment
 */
class UserProfileEditFragment : Fragment() {
    
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    
    private var currentProfile: UserProfile? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupGenderSpinner()
        setupBloodTypeSpinner()
        setupDateTimePickers()
        setupSaveButton()
        
        loadUserProfile()
        updateDailyFortune()
    }
    
    private fun setupGenderSpinner() {
        val genders = arrayOf("男", "女")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.actvGender.setAdapter(adapter)
    }
    
    private fun setupBloodTypeSpinner() {
        val bloodTypes = arrayOf("A型", "B型", "O型", "AB型")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bloodTypes)
        binding.actvBloodType.setAdapter(adapter)
    }
    
    private fun setupDateTimePickers() {
        binding.etBirthDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.etBirthTime.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        currentProfile?.birthDate?.let { dateStr ->
            try {
                val parts = dateStr.split("-")
                if (parts.size == 3) {
                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            } catch (e: Exception) {
                // 使用默认日期
            }
        }
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etBirthDate.setText(selectedDate)
                updateCalculatedInfo(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                binding.etBirthTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        
        timePickerDialog.show()
    }
    
    private fun updateCalculatedInfo(birthDate: String) {
        val zodiacSign = UserProfileService.calculateZodiacSign(birthDate)
        val chineseZodiac = UserProfileService.calculateChineseZodiac(birthDate)
        
        val age = try {
            val parts = birthDate.split("-")
            val birthYear = parts[0].toInt()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            currentYear - birthYear
        } catch (e: Exception) {
            0
        }
        
        if (zodiacSign.isNotEmpty() || chineseZodiac.isNotEmpty()) {
            val info = buildString {
                if (age > 0) {
                    append("年龄：${age}岁")
                }
                if (zodiacSign.isNotEmpty()) {
                    if (isNotEmpty()) append(" | ")
                    append("星座：$zodiacSign")
                }
                if (chineseZodiac.isNotEmpty()) {
                    if (isNotEmpty()) append(" | ")
                    append("生肖：$chineseZodiac")
                }
            }
            
            binding.tvCalculatedInfo.text = info
            binding.llCalculatedInfo.isVisible = true
        } else {
            binding.llCalculatedInfo.isVisible = false
        }
    }
    
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
    }
    
    private fun loadUserProfile() {
        currentProfile = UserProfileService.getUserProfile(requireContext())
        
        currentProfile?.let { profile ->
            binding.etName.setText(profile.name)
            binding.actvGender.setText(profile.gender, false)
            binding.etBirthDate.setText(profile.birthDate)
            binding.etBirthTime.setText(profile.birthTime)
            binding.etBirthPlace.setText(profile.birthPlace)
            binding.actvBloodType.setText(profile.bloodType, false)
            
            if (profile.birthDate.isNotEmpty()) {
                updateCalculatedInfo(profile.birthDate)
            }
        }
    }
    
    private fun saveUserProfile() {
        val name = binding.etName.text.toString().trim()
        val gender = binding.actvGender.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()
        val birthTime = binding.etBirthTime.text.toString().trim()
        val birthPlace = binding.etBirthPlace.text.toString().trim()
        val bloodType = binding.actvBloodType.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "请输入姓名", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (gender.isEmpty()) {
            Toast.makeText(requireContext(), "请选择性别", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (birthDate.isEmpty()) {
            Toast.makeText(requireContext(), "请选择出生日期", Toast.LENGTH_SHORT).show()
            return
        }
        
        val zodiacSign = UserProfileService.calculateZodiacSign(birthDate)
        val chineseZodiac = UserProfileService.calculateChineseZodiac(birthDate)
        
        val profile = UserProfile(
            name = name,
            gender = gender,
            birthDate = birthDate,
            birthTime = birthTime,
            birthPlace = birthPlace,
            zodiacSign = zodiacSign,
            chineseZodiac = chineseZodiac,
            bloodType = bloodType,
            mbtiType = currentProfile?.mbtiType ?: "",
            luckyNumber = currentProfile?.luckyNumber ?: 0,
            luckyColor = currentProfile?.luckyColor ?: "",
            phone = currentProfile?.phone ?: "",
            email = currentProfile?.email ?: "",
            createdTime = currentProfile?.createdTime ?: Date(),
            updatedTime = Date()
        )
        
        UserProfileService.saveUserProfile(requireContext(), profile)
        currentProfile = profile
        
        Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
        
        updateDailyFortune()
    }
    
    private fun updateDailyFortune() {
        val profile = UserProfileService.getUserProfile(requireContext())
        
        if (profile == null || !profile.isComplete()) {
            binding.tvFortuneSummary.text = "请先完善个人信息，以获取专属运势分析"
            binding.llFortuneDetails.isVisible = false
            return
        }
        
        val fortune = DailyFortuneGenerator.generateDailyFortune(profile)
        
        // 保存到历史
        FortuneHistoryService.saveTodayFortune(requireContext(), fortune)
        
        binding.tvFortuneSummary.text = fortune.summary
        binding.llFortuneDetails.isVisible = true
        
        binding.tvOverallLuck.text = fortune.getStarDisplay(fortune.overallLuck)
        binding.tvLoveLuck.text = fortune.getStarDisplay(fortune.loveLuck)
        binding.tvCareerLuck.text = fortune.getStarDisplay(fortune.careerLuck)
        binding.tvWealthLuck.text = fortune.getStarDisplay(fortune.wealthLuck)
        binding.tvHealthLuck.text = fortune.getStarDisplay(fortune.healthLuck)
        
        binding.tvLuckyElements.text = "幸运色：${fortune.luckyColor} | 幸运数字：${fortune.luckyNumber} | 吉方位：${fortune.luckyDirection}"
        binding.tvTimes.text = "吉时：${fortune.luckyTime} | 凶时：${fortune.avoidTime}"
        
        if (fortune.warnings.isNotEmpty()) {
            binding.tvWarnings.text = fortune.warnings.joinToString("\n")
            binding.tvWarnings.isVisible = true
        } else {
            binding.tvWarnings.isVisible = false
        }
        
        if (fortune.suggestions.isNotEmpty()) {
            binding.tvSuggestions.text = fortune.suggestions.joinToString("\n")
            binding.tvSuggestions.isVisible = true
        } else {
            binding.tvSuggestions.isVisible = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
