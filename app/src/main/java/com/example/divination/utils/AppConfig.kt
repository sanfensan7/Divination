package com.example.divination.utils

import android.content.Context
import android.os.Build

/**
 * 应用配置类，包含各种全局配置
 */
object AppConfig {
    
    // 版本信息 - 使用函数在运行时获取而不是使用BuildConfig
    private var cachedVersionName: String? = null
    private var cachedVersionCode: Int? = null
    
    fun getVersionName(context: Context): String {
        if (cachedVersionName == null) {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                cachedVersionName = packageInfo.versionName
            } catch (e: Exception) {
                cachedVersionName = "1.1.4" // 出错时使用默认值
            }
        }
        return cachedVersionName!!
    }
    
    fun getVersionCode(context: Context): Int {
        if (cachedVersionCode == null) {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                cachedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt() // 使用Android 9.0+的API
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode // 使用旧API但抑制警告
                }
            } catch (e: Exception) {
                cachedVersionCode = 3 // 出错时使用默认值
            }
        }
        return cachedVersionCode!!
    }
    
    /**
     * 邮件配置
     */
    object EmailConfig {
        // SMTP服务器配置
        const val SMTP_HOST = "smtp.qq.com"
        const val SMTP_PORT = "465"
        
        // 发件人信息
        const val SENDER_EMAIL = "718939591@qq.com"
        const val SMTP_PASSWORD = "cdwpwgospuctbfha" // 请替换为实际的邮箱授权码
        
        // 反馈接收邮箱
        const val FEEDBACK_EMAIL = "1838741419@qq.com"
    }
} 