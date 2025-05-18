package com.example.divination.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.divination.BuildConfig
import com.example.divination.databinding.FragmentSettingsBinding
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
        binding.tvVersion.text = "版本：${BuildConfig.VERSION_NAME}"
    }
    
    private fun setupFeedbackButton() {
        // 反馈按钮点击事件
        binding.btnFeedback.setOnClickListener {
            // 打开邮件应用发送反馈
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:feedback@example.com")
                putExtra(Intent.EXTRA_SUBJECT, "算命应用反馈")
                putExtra(Intent.EXTRA_TEXT, "请在此处描述您的问题或建议...")
            }
            
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "未找到邮件应用", Toast.LENGTH_SHORT).show()
            }
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
                .setMessage("智能算命应用\n版本：${BuildConfig.VERSION_NAME}\n\n本应用基于DeepSeek AI技术，集成了中外多种主流算命方法，为用户提供专业的命理解析服务。\n\n© 2025 三分三")
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