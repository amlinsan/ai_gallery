# AGENTS.md - Project Guidelines for ElinkAIGallery

## 0. 使用与变更边界 (Usage & Boundaries)
- 本文档是 AI AGENTS 的强约束规范；如与其他文档冲突，以本文件为准并主动确认。
- 默认进行**最小必要改动**，避免破坏性修改（大规模删除/迁移/重命名/重构/架构调整）。

## 1. 项目概述 (Project Overview)
**项目名称:** ElinkAIGallery
**核心目标:** 一个基于 Android 平台的**本地化 (Local-first)** 智能相册应用。
**核心功能逻辑**:
- **多维浏览**: 支持按“文件夹”物理路径显示，也支持按“AI 智能分类”显示。
- **智能分类**: 自动识别图片内容并归类为：人物、美食、风景等。
- **人脸聚类**: 在“人物”分类下，进一步按不同的人脸特征进行二级目录分发浏览。
- **隐私优先**: 所有的 AI 分析（分类、人脸聚类）必须在**端侧 (On-device)** 完成。

## 2. Agent 开发分工指引
- **Data Agent (数据侧)**: 负责 MediaStore 扫描、Room 数据库设计（MediaItem/Tag/FaceEntity）及 Repository 层。
- **AI Agent (算法侧)**: 负责集成 ML Kit/TFLite，实现场景识别与人脸特征提取的 WorkManager 后台任务。
- **UI Agent (视图侧)**: 负责 XML 布局实现、ViewBinding 绑定、多语言适配及 ViewModel 状态驱动。

## 3. 技术栈约束 (Tech Stack Constraints)
- **语言**: 100% Kotlin。
- **UI 架构**: XML Layouts + ViewBinding + Single Activity (Navigation Component)。
- **异步与数据**: Coroutines + Flow + Room (唯一可信源)。
- **图片加载**: Coil (Coroutines Image Loading)。
- **日志规范**: 统一使用 `utils/MyLog`，Tag 固定为 `elink_aig`。

## 4. 编码规范 (Coding Standards)
- **权限处理**: 适配 Android 13/14/15 媒体权限（READ_MEDIA_IMAGES 等）。
- **资源规范**: 严禁硬编码颜色、尺寸、字符串。所有文本需通过 `strings.xml` 支持中英文。
- **错误处理**: I/O 与 AI 推理必须在 `Dispatchers.IO` 执行。

## 5. 禁止事项 (Negative Constraints)
- 禁止在主线程进行数据库访问或图片解码。
- 禁止引入网络请求库，保持离线属性。
