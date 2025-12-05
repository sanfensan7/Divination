package com.example.divination.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.divination.model.DivinationResult
import com.example.divination.model.MBTIResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 分享工具：将算命结果渲染为图片并通过系统分享。
 */
object ShareUtils {

    suspend fun shareDivinationResult(context: Context, result: DivinationResult) {
        val bitmap = withContext(Dispatchers.Default) {
            createDivinationShareBitmap(context, result)
        }
        withContext(Dispatchers.Main) {
            shareBitmap(context, bitmap, "分享算命结果")
        }
    }

    suspend fun shareMBTIResult(context: Context, result: MBTIResult) {
        val bitmap = withContext(Dispatchers.Default) {
            createMbtiShareBitmap(result)
        }
        withContext(Dispatchers.Main) {
            shareBitmap(context, bitmap, "分享 MBTI 结果")
        }
    }

    private fun createDivinationShareBitmap(context: Context, result: DivinationResult): Bitmap {
        val width = 1080
        val padding = 72
        val contentWidth = width - padding * 2

        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 60f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f
        }

        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        val headerText = "智能算命 · 分享卡片"
        val subHeaderText = "生成时间：${dateFormat.format(result.createTime)}"
        val sections = result.resultSections.take(3)

        fun buildLayout(text: String, paint: TextPaint): StaticLayout =
            StaticLayout.Builder
                .obtain(text, 0, text.length, paint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(10f, 1f)
                .setIncludePad(false)
                .build()

        val headerLayout = buildLayout(headerText, headerPaint)
        val subHeaderLayout = buildLayout(subHeaderText, bodyPaint)

        val sectionLayouts = sections.flatMap { section ->
            val titleLayout = buildLayout("◆ ${section.title}", titlePaint)
            val contentLayout = buildLayout(section.content, bodyPaint)
            listOf(titleLayout, contentLayout)
        }

        val totalHeight = padding * 2 +
            headerLayout.height +
            16 +
            subHeaderLayout.height +
            32 +
            sectionLayouts.sumOf { it.height } +
            (sections.size * 40)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val gradient = LinearGradient(
            0f,
            0f,
            canvas.width.toFloat(),
            canvas.height.toFloat(),
            intArrayOf(
                Color.parseColor("#5F2EEA"),
                Color.parseColor("#312879"),
                Color.parseColor("#0F172A")
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPaint(Paint().apply { shader = gradient })

        var currentY = padding.toFloat()

        fun drawLayout(layout: StaticLayout) {
            canvas.save()
            canvas.translate(padding.toFloat(), currentY)
            layout.draw(canvas)
            canvas.restore()
            currentY += layout.height
        }

        drawLayout(headerLayout)
        currentY += 16f
        drawLayout(subHeaderLayout)
        currentY += 32f

        sectionLayouts.chunked(2).forEach { (titleLayout, contentLayout) ->
            drawLayout(titleLayout)
            currentY += 12f
            drawLayout(contentLayout)
            currentY += 40f
        }

        // 底部标识
        val footerText = "来自 Divination 智能算命"
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B3FFFFFF")
            textSize = 32f
        }
        canvas.drawText(
            footerText,
            padding.toFloat(),
            (bitmap.height - padding / 2).toFloat(),
            footerPaint
        )

        return bitmap
    }

    private fun createMbtiShareBitmap(result: MBTIResult): Bitmap {
        val width = 1080
        val padding = 72
        val contentWidth = width - padding * 2

        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val typePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#60A5FA")
            textSize = 120f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
        }

        val dimensionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f
        }

        fun layout(text: String, paint: TextPaint): StaticLayout =
            StaticLayout.Builder
                .obtain(text, 0, text.length, paint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(8f, 1f)
                .setIncludePad(false)
                .build()

        val header = layout("MBTI 测试结果", headerPaint)
        val type = layout(result.personalityType, typePaint)
        val date = layout(
            SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(result.testDate)),
            subPaint
        )

        val dimensions = listOf(
            "外向(E)/内向(I)" to result.getEILabel(),
            "实感(S)/直觉(N)" to result.getSNLabel(),
            "思考(T)/情感(F)" to result.getTFLabel(),
            "判断(J)/知觉(P)" to result.getJPLabel(),
        )

        val dimensionLayouts = dimensions.flatMap { (title, desc) ->
            val titleLayout = layout(title, subPaint)
            val descLayout = layout(desc, dimensionPaint)
            listOf(titleLayout, descLayout)
        }

        val totalHeight = padding * 2 +
            header.height + 16 +
            type.height + 24 +
            date.height + 32 +
            dimensionLayouts.sumOf { it.height } +
            40 * dimensions.size +
            80

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), totalHeight.toFloat(),
            intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1D2671")),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPaint(Paint().apply { shader = gradient })

        var currentY = padding.toFloat()
        fun drawLayout(layout: StaticLayout) {
            canvas.save()
            canvas.translate(padding.toFloat(), currentY)
            layout.draw(canvas)
            canvas.restore()
            currentY += layout.height
        }

        drawLayout(header)
        currentY += 16f
        drawLayout(type)
        currentY += 24f
        drawLayout(date)
        currentY += 32f

        dimensionLayouts.chunked(2).forEach { (titleLayout, valueLayout) ->
            drawLayout(titleLayout)
            drawLayout(valueLayout)
            currentY += 40f
        }

        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B3FFFFFF")
            textSize = 32f
        }
        canvas.drawText(
            "来自 Divination 智能算命",
            padding.toFloat(),
            (bitmap.height - padding / 2).toFloat(),
            footerPaint
        )

        return bitmap
    }

    private fun shareBitmap(context: Context, bitmap: Bitmap, title: String) {
        val cacheDir = File(context.cacheDir, "shares").apply { mkdirs() }
        val shareFile = File(cacheDir, "divination_share_${System.currentTimeMillis()}.png")
        FileOutputStream(shareFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }

        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, shareFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
}
