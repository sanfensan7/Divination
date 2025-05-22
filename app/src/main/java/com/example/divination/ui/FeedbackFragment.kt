package com.example.divination.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.divination.databinding.FragmentFeedbackBinding
import com.example.divination.R
import com.example.divination.utils.AppConfig
import com.example.divination.utils.EmailSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSubmitButton()
        setupEmailInfoText()
    }
    
    private fun setupEmailInfoText() {
        // 显示接收邮箱信息
        binding.tvAppInfo.text = "反馈将直接发送至：${AppConfig.EmailConfig.FEEDBACK_EMAIL}\n应用信息将自动添加到反馈中"
    }

    private fun setupSubmitButton() {
        binding.btnSubmitFeedback.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val content = binding.etContent.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "请输入反馈内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取反馈类型
        val feedbackType = when {
            binding.rbBug.isChecked -> "功能异常"
            binding.rbSuggestion.isChecked -> "功能建议"
            else -> "其他问题"
        }

        // 获取联系方式（可选）
        val contact = binding.etContact.text.toString().trim()
        
        // 构建邮件主题和内容
        val emailSubject = "算命应用反馈：$feedbackType"
        val emailBody = buildEmailBody(content, feedbackType, contact)
        
        // 显示正在提交的进度
        setSubmitting(true)
        
        // 在后台线程中发送邮件
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = EmailSender.sendEmail(emailSubject, emailBody)
                
                setSubmitting(false)
                
                if (success) {
                    Toast.makeText(requireContext(), "反馈提交成功，感谢您的宝贵意见！", Toast.LENGTH_SHORT).show()
                    // 清空表单
                    binding.etContent.setText("")
                    binding.etContact.setText("")
                    binding.rgFeedbackType.check(R.id.rbBug)
                    
                    // 返回上一页
                    parentFragmentManager.popBackStack()
                } else {
                    showEmailSendError("邮件服务器配置可能有误，请联系开发者")
                }
            } catch (e: Exception) {
                setSubmitting(false)
                showEmailSendError("发送失败: ${e.message}")
            }
        }
    }
    
    private fun showEmailSendError(message: String) {
        val errorMsg = "反馈提交失败。$message\n" +
                       "请尝试直接发送邮件至: ${AppConfig.EmailConfig.FEEDBACK_EMAIL}"
        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun setSubmitting(isSubmitting: Boolean) {
        binding.progressBar.visibility = if (isSubmitting) View.VISIBLE else View.GONE
        binding.btnSubmitFeedback.isEnabled = !isSubmitting
        binding.btnSubmitFeedback.text = if (isSubmitting) "提交中..." else "提交反馈"
    }

    private fun buildEmailBody(content: String, feedbackType: String, contact: String): String {
        val sb = StringBuilder()
        
        // 添加反馈类型和内容
        sb.append("【反馈类型】\n")
        sb.append("$feedbackType\n\n")
        
        sb.append("【反馈内容】\n")
        sb.append("$content\n\n")
        
        // 添加联系方式（如果有）
        if (contact.isNotEmpty()) {
            sb.append("【联系方式】\n")
            sb.append("$contact\n\n")
        }
        
        // 添加设备和应用信息
        sb.append("【设备信息】\n")
        sb.append("设备型号: ${Build.MANUFACTURER} ${Build.MODEL}\n")
        sb.append("Android版本: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
        sb.append("应用版本: ${AppConfig.getVersionName(requireContext())} (${AppConfig.getVersionCode(requireContext())})\n")
        
        return sb.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 