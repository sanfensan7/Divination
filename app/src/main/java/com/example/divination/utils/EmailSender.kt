package com.example.divination.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * 邮件发送工具类
 */
object EmailSender {
    private const val TAG = "EmailSender"
    
    // 从AppConfig中获取配置
    private val SMTP_HOST = AppConfig.EmailConfig.SMTP_HOST
    private val SMTP_PORT = AppConfig.EmailConfig.SMTP_PORT
    private val SENDER_EMAIL = AppConfig.EmailConfig.SENDER_EMAIL
    private val SENDER_PASSWORD = AppConfig.EmailConfig.SMTP_PASSWORD
    private val FEEDBACK_EMAIL = AppConfig.EmailConfig.FEEDBACK_EMAIL
    
    /**
     * 使用协程发送邮件
     * 
     * @param subject 邮件主题
     * @param body 邮件内容
     * @param toEmail 收件人邮箱，默认为FEEDBACK_EMAIL
     * @return 发送成功返回true，否则返回false
     */
    suspend fun sendEmail(
        subject: String, 
        body: String, 
        toEmail: String = FEEDBACK_EMAIL
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 检查邮箱配置是否已设置
                if (SENDER_PASSWORD == "your_password_here") {
                    Log.e(TAG, "邮箱密码未配置，请在AppConfig中配置正确的邮箱信息")
                    return@withContext false
                }
                
                // 设置邮件属性
                val props = Properties().apply {
                    put("mail.smtp.host", SMTP_HOST)
                    put("mail.smtp.port", SMTP_PORT)
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    
                    // 可能需要SSL连接
                    put("mail.smtp.socketFactory.port", SMTP_PORT)
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    
                    // 调试模式
                    put("mail.debug", "true")
                    
                    // 超时设置
                    put("mail.smtp.connectiontimeout", "10000")
                    put("mail.smtp.timeout", "10000")
                }
                
                // 创建认证器
                val auth = object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                    }
                }
                
                // 创建邮件会话
                val session = Session.getInstance(props, auth)
                
                // 创建邮件消息
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL))
                    addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                    setSubject(subject)
                    setText(body)
                }
                
                // 发送邮件
                Transport.send(message)
                
                Log.d(TAG, "邮件发送成功：$subject")
                true
            } catch (e: Exception) {
                Log.e(TAG, "邮件发送失败: ${e.message}", e)
                false
            }
        }
    }
} 