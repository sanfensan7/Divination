package com.example.divination.ui.screen.detail

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divination.model.InputField
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import com.example.divination.utils.DeepSeekService
import com.example.divination.utils.ImageUtils
import com.example.divination.utils.LocalStorageService
import java.text.SimpleDateFormat
import java.util.*

/**
 * 算命详情页面
 * 
 * 显示算命方法的详细信息和输入表单
 * 
 * 特性：
 * - iOS 风格导航栏显示方法名称
 * - 使用 IOSCard 显示方法介绍
 * - 使用 IOSSection 组织输入表单
 * - 根据不同字段类型显示相应输入组件
 * - 使用 IOSButton 提交算命请求
 * - 使用 IOSLoadingIndicator 显示加载状态
 * 
 * **Validates: Requirements 21.1, 21.2**
 */
@Composable
fun DivinationDetailScreen(
    methodId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: (String) -> Unit = {},
    onNavigateToMBTITest: () -> Unit = {},
    viewModel: DivinationDetailViewModel = viewModel(
        factory = DivinationDetailViewModelFactory(methodId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    var isOnlineSubmitting by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 风格导航栏
        IOSNavigationBar(
            title = uiState.method?.name ?: "算命详情",
            scrollState = scrollState
        )
        
        // 内容区域
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(vertical = IOSSpacing.Medium)
        ) {
            // 加载状态
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(IOSSpacing.XXLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        IOSLoadingIndicator()
                    }
                }
            }
            
            // 错误状态
            if (uiState.error != null) {
                item {
                    IOSSection(title = "错误") {
                        IOSCard {
                            Text(
                                text = uiState.error ?: "未知错误",
                                style = IOSTypography.Body,
                                color = IOSColor.SystemRed
                            )
                        }
                    }
                }
            }
            
            // 方法详情
            uiState.method?.let { method ->
                // 方法介绍
                item {
                    IOSSection(title = "介绍") {
                        IOSCard {
                            Text(
                                text = method.description,
                                style = IOSTypography.Body,
                                color = IOSColor.TextPrimary
                            )
                        }
                    }
                }
                
                // 输入表单
                if (method.inputFields.isNotEmpty()) {
                    item {
                        IOSSection(title = "请填写信息") {
                            IOSCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                                ) {
                                    method.inputFields.forEachIndexed { index, field ->
                                        if (index > 0) {
                                            Divider(
                                                color = IOSColor.Separator,
                                                modifier = Modifier.padding(vertical = IOSSpacing.XSmall)
                                            )
                                        }
                                        
                                        InputFieldComponent(
                                            field = field,
                                            value = uiState.inputValues[field.id] ?: "",
                                            onValueChange = { value ->
                                                viewModel.updateInputValue(field.id, value)
                                            },
                                            error = uiState.validationErrors[field.id]
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 提交按钮 / 开始测试
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = IOSSpacing.PageHorizontal)
                             .padding(top = IOSSpacing.Large)
                    ) {
                        IOSButton(
                            text = if (method.id == "mbti") {
                                // MBTI 特殊：显示“开始测试”
                                "开始测试"
                            } else if (isOnlineSubmitting) {
                                "提交中..."
                            } else {
                                "开始算命"
                            },
                            onClick = {
                                val currentMethod = uiState.method
                                if (currentMethod == null || isOnlineSubmitting) {
                                    return@IOSButton
                                }

                                // MBTI：直接进入测试题页面，不调用算命逻辑
                                if (currentMethod.id == "mbti") {
                                    onNavigateToMBTITest()
                                    return@IOSButton
                                }

                                // 其他算命方法：使用 DeepSeek 在线算命
                                isOnlineSubmitting = true

                                DeepSeekService.performDivination(
                                    context = context,
                                    method = currentMethod,
                                    inputData = uiState.inputValues
                                ) { result, error ->
                                    isOnlineSubmitting = false

                                    if (error != null || result == null) {
                                        // 在线调用失败时，提示并回退到本地模拟提交流程
                                        Toast.makeText(
                                            context,
                                            error?.message ?: "AI 服务不可用，已使用本地模式",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        viewModel.submitDivination { resultId ->
                                            onNavigateToResult(resultId)
                                        }
                                    } else {
                                        // 保存结果并跳转结果页
                                        LocalStorageService.saveResult(context, result)
                                        onNavigateToResult(result.id)
                                    }
                                }
                            },
                            enabled = !isOnlineSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // 提交加载状态
                if (isOnlineSubmitting) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(IOSSpacing.Medium),
                            contentAlignment = Alignment.Center
                        ) {
                            IOSLoadingIndicator()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 输入字段组件
 * 
 * 根据字段类型显示不同的输入控件
 */
@Composable
private fun InputFieldComponent(
    field: InputField,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
    ) {
        // 字段标签
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = field.name,
                style = IOSTypography.Body,
                color = IOSColor.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            if (field.required) {
                Text(
                    text = "*",
                    style = IOSTypography.Body,
                    color = IOSColor.SystemRed
                )
            }
        }
        
        // 根据字段类型显示不同的输入控件
        when (field.type) {
            1 -> {
                // 文本输入
                TextInputField(
                    value = value,
                    onValueChange = onValueChange,
                    hint = field.hint,
                    error = error
                )
            }
            2 -> {
                // 日期输入
                DateInputField(
                    value = value,
                    onValueChange = onValueChange,
                    error = error
                )
            }
            3 -> {
                // 时间输入
                TimeInputField(
                    value = value,
                    onValueChange = onValueChange,
                    error = error
                )
            }
            4 -> {
                // 选择输入
                SelectInputField(
                    value = value,
                    options = field.options,
                    onValueChange = onValueChange,
                    error = error
                )
            }
            5 -> {
                // 图片输入（暂时使用文本输入作为占位符）
                ImageInputField(
                    value = value,
                    onValueChange = onValueChange,
                    hint = field.hint,
                    error = error
                )
            }
        }
        
        // 错误提示
        if (error != null) {
            Text(
                text = error,
                style = IOSTypography.Footnote,
                color = IOSColor.SystemRed
            )
        }
    }
}

/**
 * 文本输入字段
 */
@Composable
private fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = hint.ifEmpty { "请输入" },
                style = IOSTypography.Body,
                color = IOSColor.TextTertiary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = IOSTypography.Body.copy(color = IOSColor.TextPrimary),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = IOSColor.SystemBlue,
            unfocusedBorderColor = IOSColor.Separator,
            errorBorderColor = IOSColor.SystemRed,
            cursorColor = IOSColor.SystemBlue
        ),
        isError = error != null,
        singleLine = false,
        maxLines = 3
    )
}

/**
 * 日期输入字段
 */
@Composable
private fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // 日期选择器
    if (showDatePicker) {
        val currentDate = if (value.isNotEmpty()) {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }
        
        IOSDatePicker(
            visible = showDatePicker,
            selectedDate = currentDate,
            onDateSelected = { date ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onValueChange(dateFormat.format(date))
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }
    
    IOSButton(
        text = value.ifEmpty { "选择日期" },
        onClick = { showDatePicker = true },
        style = IOSButtonStyle.Secondary,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * 时间输入字段
 */
@Composable
private fun TimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = "请输入时间（如：12:00）",
                style = IOSTypography.Body,
                color = IOSColor.TextTertiary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = IOSTypography.Body.copy(color = IOSColor.TextPrimary),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = IOSColor.SystemBlue,
            unfocusedBorderColor = IOSColor.Separator,
            errorBorderColor = IOSColor.SystemRed,
            cursorColor = IOSColor.SystemBlue
        ),
        isError = error != null,
        singleLine = true
    )
}

/**
 * 选择输入字段
 */
@Composable
private fun SelectInputField(
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
    ) {
        options.forEach { option ->
            IOSButton(
                text = option,
                onClick = { onValueChange(option) },
                style = if (value == option) IOSButtonStyle.Primary else IOSButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 图片输入字段（占位符实现）
 */
@Composable
private fun ImageInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    error: String? = null
) {
    val context = LocalContext.current

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showChooser by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = ImageUtils.compressImage(context, uri)
            if (bitmap != null) {
                val base64 = ImageUtils.bitmapToBase64(bitmap)
                onValueChange(base64)
            } else {
                Toast.makeText(context, "图片处理失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val uri = tempImageUri
            if (uri != null) {
                val bitmap = ImageUtils.compressImage(context, uri)
                if (bitmap != null) {
                    val base64 = ImageUtils.bitmapToBase64(bitmap)
                    onValueChange(base64)
                } else {
                    Toast.makeText(context, "图片处理失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val startCameraCapture = {
        val file = ImageUtils.createTempImageFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        tempImageUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCameraCapture()
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    if (showChooser) {
        IOSCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = IOSSpacing.Small)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
            ) {
                IOSButton(
                    text = "拍照上传",
                    onClick = {
                        showChooser = false
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            startCameraCapture()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    style = IOSButtonStyle.Primary,
                    modifier = Modifier.fillMaxWidth()
                )

                IOSButton(
                    text = "从相册选择",
                    onClick = {
                        showChooser = false
                        galleryLauncher.launch("image/*")
                    },
                    style = IOSButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                IOSButton(
                    text = "取消",
                    onClick = { showChooser = false },
                    style = IOSButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    IOSButton(
        text = if (value.isEmpty()) hint.ifEmpty { "选择图片" } else "已选择图片",
        onClick = { showChooser = true },
        style = IOSButtonStyle.Secondary,
        modifier = Modifier.fillMaxWidth()
    )
}
