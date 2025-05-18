package com.example.divination.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import java.util.Calendar

/**
 * 自定义日期选择对话框
 * 禁用年份修改功能
 */
class CustomDatePickerDialog(
    context: Context,
    private val listener: OnDateSetListener?,
    year: Int,
    month: Int,
    dayOfMonth: Int
) : DatePickerDialog(context, listener, year, month, dayOfMonth) {

    init {
        // 初始化时禁用年份修改功能
        disableYearEdit()
    }
    
    /**
     * 禁用年份修改功能
     */
    private fun disableYearEdit() {
        try {
            val datePickerField = DatePickerDialog::class.java.getDeclaredField("mDatePicker")
            datePickerField.isAccessible = true
            val datePicker = datePickerField.get(this) as DatePicker
            
            // 设置日期选择器的子视图不可获取焦点，防止年份修改
            datePicker.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                try {
                    // 强制使用日历模式
                    val method = DatePicker::class.java.getMethod("setDatePickerMode", Int::class.javaPrimitiveType)
                    method.invoke(datePicker, 1) // 1 - 日历模式
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // 设置标题，明确这是日期选择而非年份修改
            setTitle("选择日期")
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 重写show方法，确保每次显示时都禁用年份修改
     */
    override fun show() {
        super.show()
        // 再次确保年份修改功能被禁用
        disableYearEdit()
    }
    
    /**
     * 创建对话框的静态方法
     */
    companion object {
        /**
         * 创建自定义日期选择对话框
         */
        fun create(
            context: Context,
            listener: DatePickerDialog.OnDateSetListener
        ): CustomDatePickerDialog {
            val calendar = Calendar.getInstance()
            return CustomDatePickerDialog(
                context,
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
} 