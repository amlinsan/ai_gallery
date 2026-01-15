# 🖼️ AI Gallery | 智能离线相册

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![TFLite](https://img.shields.io/badge/AI-TensorFlow%20Lite-orange.svg)](https://www.tensorflow.org/lite)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Elink AI Gallery** 是一款基于 Android 的智能本地相册应用。它利用端侧 AI 技术（TensorFlow Lite & ML Kit），在完全离线的情况下实现图片的语义搜索、智能分类、人脸聚类和背景替换功能，极致保护用户隐私。

---

## ✨ 核心特性

*   **🔍 语义搜索 (Semantic Search)**
    *   支持自然语言搜索图片（如“在大海边奔跑的狗”、“红色背景墙”）。
    *   基于 **CLIP** (Contrastive Language-Image Pre-training) 模型，理解图片与文本的深层语义关联。
    *   支持中英文混合搜索（自动映射）。
*   **🏷️ 智能分类 (Auto Tagging)**
    *   利用 EfficientNet/MobileNet 自动识别图片内容（如美食、风景、猫、狗等）。
    *   后台自动扫描与索引。
*   **bust_in_silhouette: 人脸聚类 (Face Clustering)**
    *   自动识别照片中的人脸并进行聚类。
    *   为每个人物生成专属相册。
*   **🎨 AI 魔法编辑**
    *   **人像分割**：基于 MediaPipe Selfie Segmenter 实现高精度人像抠图。
    *   **背景替换**：一键更换照片背景。
*   **🔒 离线优先 & 隐私安全**
    *   所有 AI 推理均在本地设备（On-Device）完成，无需上传云端，确保照片隐私安全。

---

## 📱 截图展示

| 首页浏览 | 语义搜索 | 人物相册 | 详情与编辑 |
|:---:|:---:|:---:|:---:|
| *(待添加首页截图)* | *(待添加搜索截图)* | *(待添加人物截图)* | *(待添加编辑截图)* |

---

## 🚀 快速开始

### 环境要求
*   Android Studio Ladybug 或更新版本
*   JDK 17+
*   Android SDK API Level 35 (Compile SDK)
*   Min SDK: 24 (Android 7.0)

### 1. 克隆仓库

```bash
git clone git@github.com:amlinsan/ai_gallery.git
cd ai_gallery
```

### 2. 下载 AI 模型（⚠️ 重要）

由于 GitHub 文件大小限制，大型 AI 模型文件（如 CLIP 编码器）未包含在 git 仓库中。请务必在构建前运行以下脚本自动下载：

```bash
# 给脚本添加执行权限
chmod +x get_models.sh

# 运行下载脚本
./get_models.sh
```

> **注意**：脚本会自动将 `clip_image_encoder.tflite`, `clip_text_encoder.tflite` 等文件下载到 `app/src/main/assets/` 目录。如果下载失败，请检查网络连接。

#### 模型/资源来源清单

以下为项目涉及的模型与相关资源文件来源（含脚本下载与手动准备项）：

- `mobilenetv1.tflite`：https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/image_classification/android/mobilenet_v1_1.0_224_quantized_1_metadata_1.tflite
- `efficientnet-lite0.tflite`：https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/image_classification/android/efficientnet_lite0_int8_2.tflite
- `efficientnet-lite1.tflite`：https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/image_classification/android/efficientnet_lite1_int8_2.tflite
- `efficientnet-lite2.tflite`（可选）：https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/image_classification/android/efficientnet_lite2_int8_2.tflite
- `selfie_segmenter.tflite`：https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite
- `clip_text_encoder.tflite`：https://huggingface.co/qualcomm/OpenAI-Clip/resolve/main/CLIPTextEncoder.tflite
- `clip_image_encoder.tflite`：https://huggingface.co/qualcomm/OpenAI-Clip/resolve/main/CLIPImageEncoder.tflite
- `face_embedding.tflite`（文件名来自重命名）：https://raw.githubusercontent.com/shubham0204/OnDevice-Face-Recognition-Android/master/app/src/main/assets/facenet.tflite
- `bpe_simple_vocab_16e6.txt.gz`：https://github.com/openai/CLIP/raw/main/bpe_simple_vocab_16e6.txt.gz
- `vocab.json`（可选，BPE 词表扩展）：https://huggingface.co/openai/clip-vit-base-patch32/resolve/main/vocab.json

说明：
1. `app/download_models.gradle` 中 CLIP 的 URL 是占位示例，实际使用请以 `get_models.sh` 为准。
2. `app/download_models.gradle` 里 `selfie_segmenter.tflite` 也提供了固定版本地址（`.../float16/1/...`），以脚本下载的 `latest` 为默认。

### 3. 构建与运行

使用 Android Studio 打开项目，Sync Gradle 后直接点击 **Run** 按钮。

或者使用命令行：

```bash
./gradlew assembleDebug
```

---

## 🛠️ 技术栈

*   **架构模式**: MVVM (Model-View-ViewModel) + Repository Pattern
*   **开发语言**: Kotlin
*   **UI 组件**: Android View System (XML), Fragments, Navigation Component
*   **数据库**: Room (SQLite)
*   **异步处理**: Kotlin Coroutines, Flow
*   **后台任务**: WorkManager (用于后台 AI 索引)
*   **图片加载**: Coil
*   **AI 引擎**:
    *   **TensorFlow Lite**: 运行 CLIP, MobileNet, EfficientNet 模型。
    *   **Google ML Kit**: 快速人脸检测。
    *   **MediaPipe**: 人像分割。

---

## 📂 项目结构

```
app/src/main/java/com/elink/aigallery/
├── ai/              # AI 核心逻辑 (ClipHelper, FaceEmbeddingHelper 等)
├── data/
│   ├── db/          # Room 数据库实体与 DAO
│   ├── model/       # 业务数据模型
│   └── repository/  # 数据仓库 (MediaRepository, PersonRepository)
├── ui/              # UI 界面 (Activities, Fragments, ViewModels)
├── worker/          # 后台任务 (EmbeddingWorker, TaggingWorker)
└── utils/           # 工具类
```

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1.  Fork 本仓库
2.  创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3.  提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4.  推送到分支 (`git push origin feature/AmazingFeature`)
5.  开启一个 Pull Request

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

---

## 🙏 致谢

*   [OpenAI CLIP](https://github.com/openai/CLIP)
*   [TensorFlow Lite Examples](https://github.com/tensorflow/examples/tree/master/lite)
*   [Google MediaPipe](https://developers.google.com/mediapipe)
