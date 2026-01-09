# ElinkAIGallery

本项目是一个 Android 端本地化智能相册，所有识别与分类均在端侧完成。

## 智能分类的工作原理与流程

下面是当前“智能分类”的完整数据流与处理链路，按实际运行顺序描述。

### 1. 媒体扫描与入库
- 通过 MediaStore 扫描本地图片。
- 写入 Room 的 `media_items`，并以 Room 作为唯一可信源。

### 2. 后台任务调度
- 启动时与充电时都会触发 WorkManager 任务。
- `TaggingWorkScheduler` 会安排 `TaggingWorker`：
  - 启动立即执行一次
  - 设备充电时周期执行

### 3. 标签识别（场景分类）
- `TaggingWorker` 逐批读取“未标注”的图片。
- 使用 `ImageClassifierHelper` 进行端侧分类，取 Top-K 结果。
- 先取得原始标签与分数，再做“白名单 + 阈值”过滤。
- 只写入规范标签（目前为：`Person` / `Food` / `Nature` / `Sky`），其它标签不落库。
- 标签结果写入 `image_tags`，并记录 `media_tag_analysis` 表示已完成标注。

### 4. 人物检测与聚类
- 使用 ML Kit FaceDetection 做人脸检测。
- 过滤掉过小或姿态角度过大的脸（降低误识别）。
- 对每张人脸生成 embedding（`FaceEmbeddingHelper`）。
- 与已有 `PersonEntity` 做余弦相似度匹配：
  - 达到阈值则归入同一人并更新均值 embedding
  - 未达到则新建人物
- 将“人脸-人物-图片”关系写入 `face_embeddings`，并记录 `media_face_analysis`。

### 5. UI 聚合与展示
- 智能分类页基于 `image_tags` 聚合：
  - 人物：`label = Person`
  - 美食：`label = Food`
  - 风景：`label = Nature` + `Sky`
- 人物目录页基于 `persons + face_embeddings` 聚合：
  - 统计每个人物的图片数量
  - 取最新图片作为封面
- 点击人物进入详情网格，按 `face_embeddings` 反查图片列表。

### 6. 搜索与中英映射
- 搜索同时匹配：标签、路径、文件夹名。
- 中文关键词会映射为规范标签（例如：美食 -> `Food`，风景 -> `Nature`，天空 -> `Sky`）。

## 当前识别状态
- **人物**：聚类已可用，人物目录可浏览。
- **美食/风景**：识别命中率仍偏低，需要进一步完善模型输出映射与阈值策略。

## 开发提示
- 若需让新策略生效，应清理旧标签与分析记录后重新运行标注任务。
- 端侧推理必须在 `Dispatchers.IO` 运行，禁止引入网络请求库。
