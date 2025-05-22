package com.example.divination.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentSettingsBinding
import com.example.divination.utils.AppConfig
import com.example.divination.utils.LocalStorageService

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupApiKeyInput()
        setupVersionInfo()
        setupFeedbackButton()
        setupPrivacyPolicyButton()
        setupAboutButton()
    }
    
    private fun setupApiKeyInput() {
        // 加载已保存的API密钥
        val apiKey = LocalStorageService.getApiKey(requireContext())
        binding.etApiKey.setText(apiKey)
        
        // 保存按钮点击事件
        binding.btnSaveApiKey.setOnClickListener {
            val newApiKey = binding.etApiKey.text.toString().trim()
            LocalStorageService.saveApiKey(requireContext(), newApiKey)
            Toast.makeText(requireContext(), "API密钥已保存", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupVersionInfo() {
        // 设置版本信息
        binding.tvVersion.text = getString(com.example.divination.R.string.version, 
            AppConfig.getVersionName(requireContext()))
    }
    
    private fun setupFeedbackButton() {
        // 反馈按钮点击事件
        binding.btnFeedback.setOnClickListener {
            // 跳转到反馈表单页面
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FeedbackFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    
    private fun setupPrivacyPolicyButton() {
        // 隐私政策按钮点击事件
        binding.btnPrivacyPolicy.setOnClickListener {
            // 打开隐私政策网页
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"))
            startActivity(intent)
        }
    }
    
    private fun setupAboutButton() {
        // 关于按钮点击事件
        binding.btnAbout.setOnClickListener {
            // 显示关于对话框
            val aboutDialog = android.app.AlertDialog.Builder(requireContext())
                .setTitle("关于")
                .setMessage(getString(com.example.divination.R.string.about_content, 
                    AppConfig.getVersionName(requireContext())))
                .setPositiveButton("确定", null)
                .create()
            
            aboutDialog.show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 