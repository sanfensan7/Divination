package com.example.divination.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 图片处理工具类
 */
object ImageUtils {
    
    /**
     * 压缩图片到指定大小
     */
    fun compressImage(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // 首先获取图片尺寸
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // 计算缩放比例
            var scale = 1
            while (options.outWidth / scale > maxWidth || options.outHeight / scale > maxHeight) {
                scale *= 2
            }
            
            // 解码图片
            val inputStream2 = context.contentResolver.openInputStream(uri) ?: return null
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            var bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
            inputStream2.close()
            
            // 处理图片旋转
            bitmap = bitmap?.let { rotateImageIfRequired(context, it, uri) }
            
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 根据EXIF信息旋转图片
     */
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(selectedImage) ?: return img
            val ei = ExifInterface(inputStream)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()
            
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
                else -> img
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return img
        }
    }
    
    /**
     * 旋转图片
     */
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }
    
    /**
     * 将Bitmap转换为Base64字符串
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * 将Base64字符串转换为Bitmap
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 保存Bitmap到文件
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        return try {
            val file = File(context.cacheDir, fileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 创建临时图片文件
     */
    fun createTempImageFile(context: Context): File {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    /**
     * 分析图片内容（简单描述）
     */
    fun analyzeImageContent(bitmap: Bitmap, isPalmistry: Boolean): String {
        // 这里可以添加更复杂的图像分析逻辑
        // 目前返回基本描述
        val width = bitmap.width
        val height = bitmap.height
        
        return if (isPalmistry) {
            buildString {
                append("手掌照片分析：\n")
                append("- 图片尺寸：${width}x${height}\n")
                append("- 图片清晰度：良好\n")
                append("- 建议：请确保手掌纹路清晰可见，光线充足")
            }
        } else {
            buildString {
                append("面部照片分析：\n")
                append("- 图片尺寸：${width}x${height}\n")
                append("- 图片清晰度：良好\n")
                append("- 建议：请确保五官清晰，正面拍摄效果最佳")
            }
        }
    }
}
