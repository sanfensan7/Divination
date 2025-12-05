# Divination · 智能算命客户端

> 面向现代用户的全谱系算命/心理洞察应用，融合中国传统玄学、MBTI 心理测评与 DeepSeek AI，采用 Jetpack Compose 打造 iOS 风格体验。

- **平台**：Android 8.0+（API 26）  
- **版本**：v1.2.0  
- **技术栈**：Kotlin · Jetpack Compose · Room · Glide · MPAndroidChart · DeepSeek API

---

## 目录

1. [主要特性](#主要特性)  
2. [系统架构](#系统架构)  
3. [核心模块](#核心模块)  
4. [DeepSeek API 诊断指南](#deepseek-api-诊断指南)  
5. [iOS 风格动画系统](#ios-风格动画系统)  
6. [安装与运行](#安装与运行)  
7. [项目结构](#项目结构)  
8. [常见问题](#常见问题)  
9. [许可证与免责声明](#许可证与免责声明)

---

## 主要特性

- 🧭 **全品类占卜**：八字、紫微、周易、塔罗、占星、数字命理、手相、面相、老黄历、周公解梦等 10+ 体系。
- 🪄 **沉浸式体验**：自研主题、组件、动效，统一配色与质感卡片。
- 🧠 **专业 MBTI 测评**：60 道七级量表题，带进度保存、历史记录、可视化图表。
- 🔮 **可解释报告**：每个算命/测评输出结构化分析、建议、提醒。
- 🔐 **离线保障**：DeepSeek 请求失败自动切换本地模拟模式；用户画像、题库、历史记录全部存储在本地。
- 📊 **用户画像**：星座/生肖/血型/MBTI 汇总、偏好标签、运势趋势、活跃统计，一站式呈现。

---

## 系统架构

| 层级 | 说明 |
| --- | --- |
| UI | Jetpack Compose，少量遗留 Fragment + ViewBinding |
| 状态 | ViewModel + StateFlow/Compose State |
| 导航 | 单 Activity + `NavHost` + 自定义转场动画 |
| 数据 | Room（历史记录）、SharedPreferences+Gson（MBTI/配置）、Raw JSON（题库） |
| 网络 | DeepSeek API + OkHttp，自定义超时与降级策略 |
| 第三方 | Glide、MPAndroidChart、Material Icons Extended |

### 架构概览

```text
MainActivity (Compose Entry)
└─ MainScreen
   ├─ IOSBottomNavigation (Home / Mood / Methods / Profile / Settings)
   └─ AppNavGraph
      ├─ HomeScreen / MoodHistoryScreen
      ├─ DivinationMethodsScreen → Detail → Result
      ├─ MBTITestScreen → MBTIResultScreen
      ├─ Palmistry / FaceReading (图片上传)
      └─ Settings / Feedback
```

---

## 核心模块

### 1. MBTI 人格测试

- **题库**：`res/raw/mbti_questions.json`（四维度 × 15 题，正反向均衡，带版本号）。
- **算法**：七级同意度（-3 ~ +3），按 EI / SN / TF / JP 四象限累计，纯函数可复现。
- **数据层**：`MBTIQuestionProvider`（懒加载+版本校验）、`MBTICalculator`、`MBTIStorageService`（SharedPreferences + Gson 历史记录/进度）。
- **体验**：自动跳题、进度保存、历史最多 50 条、四维条形图、类型说明（概述/优势/劣势/职业/关系/成长）。
- **合规**：题目与文案基于公开理论改写；仅作娱乐与自我探索参考，数据只存本地可随时清除。

### 2. 手相 / 面相（图片上传）

- **输入**：照片（相机/相册）、性别、出生日期；内置压缩、EXIF 修正、预览/删除。
- **AI 调用**：DeepSeek 提示词覆盖掌纹节点、三停五岳、面部比例等；连接超时 30s、读取 65s；提供 `simulate*Response()` 以离线 fallback。
- **权限**：FileProvider、READ/WRITE/Camera 权限统一管理。
- **loading**：多阶段提示语 + Skeleton 动画，保持沉浸体验。

### 3. 用户画像 & 运势系统

- **信息卡**：星座、生肖、血型、MBTI、等级、经验条。
- **智能标签**：基于算命次数、偏好、常问主题、连续登录、近期运势生成 15+ 动态标签。
- **趋势图**：近 7 天综合/爱情/事业/财运/健康折线 + 雷达图（MPAndroidChart）。
- **运势提示**：幸运色、幸运数、吉方位、吉/凶时段及自动提醒语。

---

## DeepSeek API 诊断指南

> 当在线推理失败时，可通过以下流程定位问题。应用已内置降级机制，失败会自动切换本地模拟模式。

1. **收集日志**  
   - Android Studio Logcat 过滤 `DeepSeekService`，复现请求并导出。  
   - 或运行 `adb logcat | findstr "DeepSeekService"`。
2. **常见故障与解决**  
   - **网络/防火墙**：切换网络、关闭 VPN、确认可访问 `https://api.deepseek.com`。  
   - **DNS**：改用 8.8.8.8 / 1.1.1.1，清缓存。  
   - **密钥**：在 DeepSeek 控制台检查配额或重新生成。  
   - **证书/时间**：同步设备时间、更新系统、检查 `network_security_config.xml`。  
   - **超时**：连接 30-60s、读取 60-120s，可按需要上调。
3. **命令行测试**

```bash
curl -X POST https://api.deepseek.com/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer sk-your-key" \
  -d '{
    "model": "deepseek-chat",
    "messages": [{"role": "user", "content": "你好"}],
    "max_tokens": 100
  }'
```

若上述命令成功，说明密钥与网络正常，需排查客户端配置。

---

## iOS 风格动画系统

- **核心文件**：
  - `SpringAnimation.kt`：standard / fast / gentle / smooth / bouncy / listItem 弹簧配置。
  - `ButtonAnimation.kt`：按钮/卡片按压缩放（0.94 / 0.97），亮度降低 15%。
  - `ListItemAnimation.kt`：渐入 + 50 ms 错峰，支持 hover。
  - `LoadingAnimation.kt`：内容淡入、滑动转场、骨架屏脉冲、指示器旋转。
  - `PageTransition.kt`：iOS 推入/推出，边缘滑动返回。
  - `ScrollBehavior.kt`：Large Title 收缩、背景模糊透明联动。
- **参数基线**：按钮 200 ms、页面 350 ms、快速转场 250 ms、列表项 300 ms（偏移 20 dp）、hover 1.02 倍。
- **设计原则**：物理真实感、一致性、性能优先（保持 60 fps）、尊重“减少动态”系统设置。

---

## 安装与运行

1. **环境**：Android Studio Koala+、JDK 11、Android SDK 34、Kotlin 1.9.x。  
2. **依赖**：首次同步后执行 `./gradlew tasks` 验证环境。  
3. **运行**：配置 DeepSeek API Key（设置页或 `local.properties`），选择 API 26+ 设备，点击 Run 或 `./gradlew installDebug`。  
4. **调试提示**：Compose Preview 需 Electric Eel+；`BuildConfig.DEBUG` 可开启额外日志；设置页可切换“本地模拟模式”以离线测试。

---

## 项目结构

```text
Divination/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/divination/
│       │   ├── MainActivity.kt                # Compose 入口
│       │   ├── ui/navigation/                 # Routes / NavGraph / Animations
│       │   ├── ui/screen/                     # Home / Methods / MBTI / Settings 等
│       │   ├── ui/component/                  # IOSBottomNavigation、通用卡片
│       │   ├── ui/theme/                      # 颜色 / 排版 / 间距
│       │   ├── utils/                         # DeepSeekService、MBTI、IChing 等
│       │   └── model/                         # 业务数据模型
│       └── res/                               # Drawable / Layout / Raw 题库
├── gradle/libs.versions.toml                  # 依赖版本清单
└── README.md
```

---

## 常见问题

1. **如何扩展新算命方式？** 在 `DivinationMethodProvider` 注册 -> 创建输入表单/屏幕 -> 在 `Routes` 与 `AppNavGraph` 中挂接 -> 复用统一卡片/动画。  
2. **DeepSeek Key 泄露风险？** 仅存储于本地 SharedPreferences；若需更高安全性，可使用 Keystore 加密。  
3. **无法联网时怎么办？** 设置页启用“本地模拟模式”，所有核心功能（含 MBTI）均可离线运行。  
4. **MBTI 结果是否可导出？** 目前支持历史列表查看与手动复制，后续可扩展分享卡片。  
5. **动画过多会头晕？** Android 无障碍“减少动画”开启后，会自动降低动效强度。

---

## 许可证与免责声明

- **License**：本项目采用 [MIT](LICENSE) 许可证，可自由使用、修改与分发。  
- **免责声明**：所有命理、占卜、心理测评内容仅供娱乐与自我探索参考，请勿作为任何重大决策依据。

欢迎提交 Issue / PR，共同完善 Divination ✨
