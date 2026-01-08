# ElinkAIGallery UI/UE 设计规范总结

## 0. 优先级与范围
- **优先级**: 如与 `AGENTS.md` 冲突，以 `AGENTS.md` 为准。
- **设计原则**: 沉浸式状态栏、扁平化、语义化资源、全面支持深浅模式及中英文切换。
- **变更边界**: 默认延续现有布局/导航与视觉语言，避免破坏性修改或重构。

## 1. 布局结构 (Layout Structure)

### 1.1 主界面 (GalleryFragment)
- **头部 (Header)**: `RelativeLayout` 左右对齐。左侧大号粗体标题（相册/Gallery），右侧 `SearchView`。
- **标签栏 (TabLayout)**: 显示“文件夹” (Folders) 和“智能分类” (Smart Categories)。
- **内容区 (ViewPager2)**: 承载两个标签页，支持左右滑动切换。

### 1.2 智能分类与人脸聚类 (AI Features)
- **分类导航流**: 
  - 点击“智能分类”中的“人物” -> 跳转至 **FaceGridFragment** (人脸目录)。
  - 点击“美食/风景”等分类 -> 直接跳转至 **MediaGridFragment** (图片网格)。
- **人脸目录 (FaceGridFragment)**: 
  - 展示不同人脸的头像阵列（圆形裁剪）。
  - 文字显示“人物 1”或识别出的名称。

### 1.3 列表项规范 (List Items)
- **文件夹/分类项 (`item_folder.xml`)**: 
  - 左侧封面：尺寸 `@dimen/gallery_cover_size` (88dp)，圆角矩形。
  - 右侧文本：主标题（24sp Bold）+ 副标题（22sp 数量）。
- **图片项 (`item_media.xml`)**: 
  - 3 列网格布局，高度固定为 `@dimen/gallery_media_item_height` (120dp)，`centerCrop` 填充。

### 1.4 全屏浏览 (PhotoFragment)
- **功能**: 自定义 `ZoomImageView`，支持双指缩放、拖拽和双击放大。
- **背景**: 纯黑色 (`@color/black`)。

## 2. 资源规范 (Resource Standards)

### 2.1 资源硬规则 (Must)
- **字符串**: 统一放在 `strings.xml`，并提供 `values-zh`/`values-en`；动态文本用占位符（`%1$s/%1$d`），复数用 `plurals.xml`。
- **样式**: 布局仅引用 `style`/`textAppearance`；禁止在布局中直接写 `textSize`，可复用的文字样式必须抽成 `TextAppearance.*`。
- **尺寸**: 组件宽高/边距/内边距必须引用 `dimens.xml`，禁止布局中直接写数字。
- **颜色**: 颜色语义化命名并放入 `colors.xml`，必须在 `values-night` 提供对应值，优先使用主题属性。
- **命名示例**: `text_primary`, `bg_surface`, `spacing_m`, `text_title`.

### 2.2 颜色 (Colors)
必须使用语义化名称，并在 `values-night` 提供适配。
- `text_primary`: 主要文字颜色 (#000000 / #FFFFFF)。
- `background_toolbar`: 工具栏背景 (#EEEEEE / 深色)。

### 2.3 字体样式 (Styles)
统一在 `styles.xml` 定义，禁止在布局中直接写 `textSize`。
- `GalleryPageTitleStyle`: 30sp, Bold。
- `GalleryItemTitleStyle`: 24sp, Bold。
- `GalleryItemCountStyle`: 22sp (比标题小 2sp)。

### 2.4 关键尺寸 (Dimens)
- `gallery_page_padding_horizontal`: 16dp。
- `gallery_item_min_height`: 96dp。
- `gallery_cover_size`: 88dp。

## 3. 交互体验 (User Experience)
- **沉浸式**: Activity 设置 `setDecorFitsSystemWindows(false)`，状态栏图标随深浅模式自动切换。
- **空状态**: 未授权或无图片时显示空状态页面，提供“授权/去拍照”按钮。
- **删除流程**:
  - **单图删除**: 在 `PhotoFragment` 提供删除入口，触发系统级确认弹窗。
  - **批量/整体删除**: 列表长按进入多选或菜单删除；删除前二次确认，智能分类删除需额外警示。
  - **结果反馈**: 删除后以 Room 刷新 UI，失败需提示原因；禁止静默删除。
