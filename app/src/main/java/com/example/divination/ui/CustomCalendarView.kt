package com.example.divination.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView

/**
 * 自定义日历视图
 * 禁用年份修改功能
 */
class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.calendarViewStyle
) : CalendarView(context, attrs, defStyleAttr) {

    init {
        // 禁用年份修改
        disableYearEdit()
    }

    /**
     * 禁用年份修改功能
     */
    private fun disableYearEdit() {
        // 拦截长按事件，防止触发年份选择
        setOnLongClickListener { true }
        
        // 禁用内部的日期选择器的焦点
        try {
            // 更直接地处理DatePicker
            val fields = javaClass.declaredFields
            for (field in fields) {
                if (field.name.contains("DatePicker", ignoreCase = true)) {
                    field.isAccessible = true
                    val datePicker = field.get(this) as? View
                    if (datePicker != null) {
                        if (datePicker is ViewGroup) {
                            datePicker.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        }
                    }
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 拦截点击事件
     * 防止点击年份区域触发修改
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (it.action == MotionEvent.ACTION_UP) {
                // 点击位置在顶部区域（年份所在区域）
                if (it.y < height * 0.15) {
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    /**
     * 拦截触摸事件分发
     * 防止年份区域接收到触摸事件
     */
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (it.action == MotionEvent.ACTION_DOWN) {
                // 点击位置在顶部区域（年份所在区域）
                if (it.y < height * 0.15) {
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
} 