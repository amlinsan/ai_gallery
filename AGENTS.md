# AGENTS.md - Project Guidelines for ElinkAIGallery

## 1. 项目概述 (Project Overview)
**项目名称:** ElinkAIGallery
**包名:** `com.elink.aigallery`
**核心目标:** 这是一个基于 Android 平台的**本地化 (Local-first)** 智能相册应用。
**关键特性:**
- **隐私优先:** 所有的 AI 分析（图像分类、标签生成）必须在**端侧 (On-device)** 完成，严禁上传图片到云端。
- **高性能:** 即使有数千张图片，滚动和搜索也必须流畅。
- **AI 驱动:** 基于 TensorFlow Lite / ML Kit 实现离线图像识别。

## 2. 技术栈约束 (Tech Stack Constraints)
你必须严格遵守以下技术选型，不得引入未经允许的第三方库：

- **语言:** 100% Kotlin。严禁使用 Java。
- **UI 架构:** - **XML Layouts** + **ViewBinding** (当前阶段标准)。
  - **Single Activity Architecture** (MainActivity + Fragments + Navigation Component)。
- **架构模式:** MVVM (Model-View-ViewModel)。
  - UI 逻辑与业务逻辑分离。
  - 使用 `ViewModel` 持有 UI 状态 (`StateFlow`/`LiveData`)。
- **异步处理:** - **Kotlin Coroutines** (协程) + **Flow**。
  - 严禁使用 `RxJava` 或 `AsyncTask`。
- **本地数据:** - **Room Database** (SQLite ORM)。
  - 使用 `DataStore` 存储简单的键值配置（如需）。
- **图片加载:** **Coil** (Coroutines Image Loading)。
- **后台任务:** **WorkManager** (用于 AI 批量分析任务)。
- **AI 引擎:** Google ML Kit (Vision) 或 TensorFlow Lite 自定义模型。

## 3. 编码规范 (Coding Standards)

### Android 版本适配
- **minSdk:** 24 (Android 7.0)
- **targetSdk:** 35 (Android 15)
- **权限处理:** - 必须针对 Android 13/14/15 的细分媒体权限 (`READ_MEDIA_IMAGES`, `READ_MEDIA_VISUAL_USER_SELECTED`) 进行适配。
  - 必须优雅处理权限被拒绝的情况。

### 数据层 (Data Layer)
- **Repository Pattern:** UI 层 (ViewModel) 不得直接访问 DAO 或 SharedPreferences，必须通过 `Repository`。
- **Source of Truth:** Room 数据库是唯一可信源。
  - 流程：系统相册扫描 -> 写入 Room -> ViewModel 观察 Room -> UI 更新。
  - 不要在 UI 层直接展示 `MediaStore` cursor，必须转换为 `MediaItem` 实体。

### 错误处理 (Error Handling)
- 所有的 I/O 操作（数据库、文件读取、AI 推理）必须在 `Dispatchers.IO` 中执行。
- 捕获异常时，不仅要打印 Log，还需考虑 UI 层的提示（如 Toast 或 Snackbar）。
- **日志规范**：统一使用 `utils/MyLog`，基础 Tag 固定为 `elink_aig`；各类可定义 `TAG` 作为业务关键字，最终日志格式为 `[TAG] message`。

## 4. 禁止事项 (Negative Constraints)
- **禁止** 在主线程 (Main Thread) 进行数据库访问或图片解码。
- **禁止** 删除项目中已有的 `MediaItem` 和 `ImageTag` 实体定义。
- **禁止** 引入网络请求库 (Retrofit/OkHttp)，除非明确要求（本项目为离线应用）。

## 5. 当前基线锁定 (Baseline Lock)
以下内容为当前阶段已确认的框架基线，后续所有调试与迭代必须以此为准，未经允许不得更改或回退：
- **架构与技术栈**：Kotlin + MVVM + Room + Flow/Coroutines + WorkManager + Coil + XML/ViewBinding。
- **导航结构**：Single Activity + Fragments + Navigation Component。
- **SDK 版本**：`minSdk=24`，`targetSdk=35`，`compileSdk=35`。
- **数据链路**：MediaStore 扫描 -> Room 入库 -> ViewModel 观察 -> UI 展示。
- **AI 后台化**：使用 WorkManager 后台批处理，端侧推理，禁止云端调用。
