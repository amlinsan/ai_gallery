# AGENTS.md - Project Guidelines for ElinkAIGallery

## 0. 使用与变更边界 (Usage & Boundaries)
- 本文档是 AI AGENTS 的强约束规范；如与其他文档冲突，以本文件为准并主动确认。
- 默认进行**最小必要改动**，避免破坏性修改（大规模删除/迁移/重命名/重构/架构调整）。
- 涉及架构/导航/数据结构/资源命名/大范围 UI 调整时，需先说明影响范围并获得确认。

## 1. 项目概述 (Project Overview)
**项目名称:** ElinkAIGallery
**核心目标:** 一个基于 Android 平台的**本地化 (Local-first)** 智能相册应用。
**当前已实现**:
- **多维浏览**: “文件夹 / 智能分类”双入口；智能分类按标签聚合为「人物 / 美食 / 风景」列表。
- **图片查看**: 支持全屏滑动浏览 + 手势缩放（`ZoomImageView`）。
- **智能搜索**: 关键词可匹配标签/路径/文件夹名，并支持中英文关键词映射。
- **安全删除**: 支持单图/批量/分类长按删除；系统级确认后以 Room 刷新 UI。
- **隐私优先**: 标签识别、人脸检测、Embedding 全部端侧完成。
- **人物聚类与目录**: 已支持人物聚类，并提供人物目录页与人物相册浏览。
**当前限制 / 待完善**:
- **美食/风景**: 识别效果仍不足/命中率偏低，需要下一步完善与校准。

## 2. Agent 开发分工指引
- **Data Agent (数据侧)**:
  - 维护 MediaStore 扫描与 Room（`MediaItem`/`ImageTag`/`MediaTagAnalysis`/`MediaFaceAnalysis`/`PersonEntity`/`FaceEmbedding`）的唯一可信源。
  - 如需新增人物聚类/人物相册的数据支持，应优先复用 `PersonEntity` 与 `FaceEmbedding`，并提供聚合查询（如：人物封面图、人数统计、最近更新时间）。
  - 任何数据库结构变更必须提供迁移，禁止破坏已有数据；查询必须可在 Flow 中稳定订阅。
  - 删除逻辑使用 MediaStore API，适配 Scoped Storage (IntentSender)，删除后同步 Room。
- **AI Agent (算法侧)**:
  - 负责标签识别 + 人脸检测/Embedding 的 WorkManager 后台任务（`TaggingWorker`）。
  - 若完善美食/风景识别：需更新标签映射（含中英文搜索映射）、阈值与模型输出对齐。
  - 若实现人物聚类：以增量方式更新 `PersonEntity`/`FaceEmbedding`，避免大规模离线重算；聚类阈值/合并逻辑需可配置且可回滚。
  - 必须端侧推理，禁止引入网络请求。
- **UI Agent (视图侧)**:
  - 负责 XML 布局实现、ViewBinding 绑定、多语言适配及 ViewModel 状态驱动。
  - 现有导航结构为单 Activity + `GalleryFragment -> MediaGridFragment -> PhotoFragment`；若新增“人物二级目录/人物相册”页面，需先说明导航变更与影响范围并获得确认。
  - 删除交互需显式确认（系统弹窗），批量/智能分类删除需二次提示。

## 3. 技术栈约束 (Tech Stack Constraints)
- **语言**: 100% Kotlin。
- **UI 架构**: XML Layouts + ViewBinding + Single Activity (Navigation Component)。
- **异步与数据**: Coroutines + Flow + Room (唯一可信源)。
- **图片加载**: Coil (Coroutines Image Loading)。
- **后台任务**: WorkManager（`TaggingWorker` 为唯一 AI 入口）。
- **日志规范**: 统一使用 `utils/MyLog`，Tag 固定为 `elink_aig`。

## 4. 编码规范 (Coding Standards)
- **权限处理**: 适配 Android 13/14/15 媒体权限（READ_MEDIA_IMAGES 等）。删除操作需适配 Android 10+ 的 `RecoverableSecurityException` 及 Android 11+ 的 `createDeleteRequest`。
- **资源规范**: 严禁硬编码颜色、尺寸、字符串。所有文本需通过 `strings.xml` 支持中英文；详细规则见 `UI_UE_Design.md`。
- **删除规范**: 禁止静默删除，必须经过系统级确认流程。
- **错误处理**: I/O 与 AI 推理必须在 `Dispatchers.IO` 执行。
- **标签规范**: 已使用的标签为 `Person` / `Food` / `Nature` / `Sky`；新增标签需同步更新 UI 分类标题与搜索映射。

## 5. 禁止事项 (Negative Constraints)
- 禁止在主线程进行数据库访问或图片解码。
- 禁止引入网络请求库，保持离线属性。
