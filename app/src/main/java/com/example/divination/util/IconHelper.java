package com.example.divination.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.example.divination.R;

/**
 * 帮助类，用于设置算命方法的图标和颜色
 */
public class IconHelper {

    /**
     * 根据算命方法名称设置对应的图标和颜色
     * @param methodName 算命方法名称
     * @param iconView 图标ImageView
     * @param outerCircle 外圆View
     * @param innerCircle 内圆View
     * @param context Context上下文
     */
    public static void setMethodIcon(String methodName, ImageView iconView, 
                                    View outerCircle, View innerCircle, Context context) {
        int iconResId = R.drawable.ic_bazi; // 默认图标
        int colorResId = R.color.divination_primary; // 默认颜色
        
        // 根据方法名称设置不同的图标和颜色
        if (methodName.contains("八字") || methodName.contains("命理")) {
            iconResId = R.drawable.ic_bazi;
            colorResId = R.color.divination_primary;
        } else if (methodName.contains("黄历") || methodName.contains("老黄历")) {
            iconResId = R.drawable.ic_huang_li;
            colorResId = R.color.success_color;
        } else if (methodName.contains("紫微") || methodName.contains("斗数")) {
            iconResId = R.drawable.ic_ziwei;
            colorResId = R.color.warning_color;
        } else if (methodName.contains("周易") || methodName.contains("卦象")) {
            iconResId = R.drawable.ic_zhouyi;
            colorResId = R.color.neutral_gray;
        } else if (methodName.contains("周公") || methodName.contains("解梦")) {
            iconResId = R.drawable.ic_dream;
            colorResId = R.color.divination_primary;
        } else if (methodName.contains("塔罗")) {
            iconResId = R.drawable.ic_tarot;
            colorResId = R.color.warning_color;
        } else if (methodName.contains("占星")) {
            iconResId = R.drawable.ic_astrology;
            colorResId = R.color.success_color;
        } else if (methodName.contains("数字") || methodName.contains("命理学")) {
            iconResId = R.drawable.ic_numerology;
            colorResId = R.color.neutral_gray;
        }
        
        // 设置颜色
        int color = ContextCompat.getColor(context, colorResId);
        
        // 安全设置外圆颜色
        if (outerCircle != null && outerCircle.getBackground() != null) {
            outerCircle.getBackground().setTint(color);
        }
        
        // 安全设置内圆颜色
        if (innerCircle != null && innerCircle.getBackground() != null) {
            innerCircle.getBackground().setTint(Color.WHITE);
        }
        
        // 设置图标颜色
        if (iconView != null) {
            iconView.setColorFilter(color);
            iconView.setImageResource(iconResId);
        }
    }
} 