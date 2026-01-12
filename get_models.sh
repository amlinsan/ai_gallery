#!/bin/bash

# 设置颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 目标目录
ASSETS_DIR="app/src/main/assets"
mkdir -p "$ASSETS_DIR"

echo -e "${YELLOW}开始检查并下载 AI 模型文件...${NC}"
echo -e "目标目录: ${ASSETS_DIR}\n"

# 定义下载函数
download_if_missing() {
    local url="$1"
    local filename="$2"
    local output_path="${ASSETS_DIR}/${filename}"

    if [ -f "$output_path" ]; then
        echo -e "${GREEN}[已存在]${NC} ${filename}"
    else
        echo -e "${YELLOW}[下载中]${NC} ${filename}..."
        # 使用 curl 下载，-L 跟随重定向，-o 指定输出文件
        curl -L "$url" -o "$output_path"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}[成功]${NC} ${filename} 下载完成"
        else
            echo -e "${RED}[失败]${NC} ${filename} 下载失败！"
            # 删除可能损坏的空文件
            rm -f "$output_path"
            exit 1
        fi
    fi
}

# ---------------------------------------------------------
# 1. Selfie Segmenter (人像分割)
# 来源: Google MediaPipe
# ---------------------------------------------------------
download_if_missing \
    "https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite" \
    "selfie_segmenter.tflite"

# ---------------------------------------------------------
# 2. CLIP Text Encoder (文本编码器)
# 来源: Hugging Face (Qualcomm/OpenAI-Clip 转换版示例)
# 注意: 如果您有特定的模型版本，请替换此 URL
# ---------------------------------------------------------
# 这是一个兼容的 CLIP Text Encoder TFLite
download_if_missing \
    "https://huggingface.co/qualcomm/OpenAI-Clip/resolve/main/CLIPTextEncoder.tflite" \
    "clip_text_encoder.tflite"

# ---------------------------------------------------------
# 3. CLIP Image Encoder (图像编码器)
# 来源: Hugging Face (Qualcomm/OpenAI-Clip 转换版示例)
# ---------------------------------------------------------
# 这是一个兼容的 CLIP Image Encoder TFLite
download_if_missing \
    "https://huggingface.co/qualcomm/OpenAI-Clip/resolve/main/CLIPImageEncoder.tflite" \
    "clip_image_encoder.tflite"

# ---------------------------------------------------------
# 4. BPE Vocabulary (分词词表)
# 来源: OpenAI CLIP 官方词表 (gzip压缩)
# ---------------------------------------------------------
download_if_missing \
    "https://huggingface.co/openai/clip-vit-base-patch32/resolve/main/vocab.json" \
    "vocab.json" 
    # 注意：我们的代码目前可能使用的是 simple tokenizer，如果未来启用 BPE，需要这个文件
    # 这里的名字暂时保持 simple，视您的 ClipTokenizer 实现而定

echo -e "\n${GREEN}所有模型检查完毕！${NC}"
echo "您可以运行 ./gradlew assembleDebug 来构建应用。"
