# ElinkAIGallery UI/UE 设计规范总结

本文档总结了 ElinkAIGallery 项目当前的 UI/UE 设计规范、资源命名约定及布局标准。

## 1. 总体设计原则

*   **沉浸式体验**：应用采用沉浸式状态栏设计，内容延伸至状态栏下方，提供更现代的视觉体验。
*   **简洁清晰**：界面元素扁平化，去除冗余装饰（如旧版的 TensorFlow 标题栏），强调内容本身。
*   **语义化资源**：所有颜色、尺寸、字符串均通过语义化命名管理，严禁在布局文件中硬编码。
*   **多语言支持**：全面支持中英文切换，所有文本必须通过 `strings.xml` 引用。

## 2. 布局结构 (Layout Structure)

### 2.1 主界面 (GalleryFragment)
*   **头部 (Header)**：
    *   布局：`RelativeLayout`，左右两端对齐。
    *   **标题**：左侧显示“相册” (Gallery)，使用大号粗体 (`GalleryPageTitleStyle`)。
    *   **搜索**：右侧显示搜索图标 (`SearchView`)，默认折叠，点击展开。
    *   内边距：水平方向统一使用 `@dimen/gallery_page_padding_horizontal` (16dp)。
*   **标签栏 (TabLayout)**：
    *   显示“文件夹” (Folders) 和“智能分类” (Smart Categories) 两个标签。
    *   字体样式：`GalleryTabTextAppearance` (16sp, Bold)。
*   **内容区**：
    *   包含 `RecyclerView` 用于展示列表，以及 `ProgressBar` (加载中) 和 `EmptyState` (空状态)。
    *   空状态包含提示文字 (`GalleryEmptyMessageStyle`) 和授权按钮。

### 2.2 列表项 (List Items)
*   **文件夹项 (`item_folder.xml`) / 分类项 (`item_category.xml`)**：
    *   **封面图**：左侧固定大小圆角矩形或正方形，尺寸 `@dimen/gallery_cover_size` (88dp)。
    *   **文本区**：右侧垂直排列。
        *   **主标题**：文件夹/分类名称，使用 `GalleryItemTitleStyle` (24sp, Bold, Black)。
        *   **副标题**：图片数量，使用 `GalleryItemCountStyle` (22sp, Black)。
    *   **交互**：点击带波纹效果 (`?attr/selectableItemBackground`)。
    *   **高度**：最小高度 `@dimen/gallery_item_min_height` (96dp)。
*   **图片项 (`item_media.xml`)**：
    *   网格布局中的单个图片单元。
    *   **高度**：固定为 `@dimen/gallery_media_item_height` (120dp)，`scaleType="centerCrop"`。

## 3. 资源规范 (Resource Standards)

### 3.1 颜色 (Colors)
禁止使用硬编码颜色值（如 `#000000`），必须使用 `colors.xml` 定义的语义化名称。

| 语义名称 | 颜色值 | 用途 |
| :--- | :--- | :--- |
| `text_primary` | `#000000` | 主要文字颜色（标题、正文） |
| `background_toolbar` | `#EEEEEE` | 工具栏/头部背景 |
| `background_bottom_sheet` | `#EEEEEE` | 底部弹窗背景 |
| `ic_launcher_background` | `#FFFFFF` | 图标背景 |

### 3.2 字体样式 (Styles)
统一在 `styles.xml` 中定义，禁止在 layout 中直接写 `textSize` 或 `textStyle`。

| 样式名称 | 父样式 | 属性 | 用途 |
| :--- | :--- | :--- | :--- |
| `GalleryPageTitleStyle` | `TextAppearance.AppCompat.Title` | 30sp, Bold, `text_primary` | 页面顶部大标题 |
| `GalleryTabTextAppearance` | `TextAppearance.AppCompat.Button` | 24sp, Bold | Tab 栏文字 |
| `GalleryItemTitleStyle` | `TextAppearance.AppCompat.Subhead` | 24sp, Bold, `text_primary` | 列表项主标题 |
| `GalleryItemCountStyle` | `TextAppearance.AppCompat.Body2` | 22sp, `text_primary` | 列表项数量文字（比标题小 2sp） |
| `GalleryEmptyMessageStyle` | `TextAppearance.AppCompat.Body1` | 22sp, `text_primary` | 空状态提示文字 |

### 3.3 尺寸 (Dimens)
所有尺寸必须在 `dimens.xml` 中定义。

| 尺寸名称 | 数值 | 说明 |
| :--- | :--- | :--- |
| **字体大小** | | |
| `gallery_page_title_size` | 30sp | 页面标题 |
| `gallery_tab_text_size` | 24sp | Tab 文字 |
| `gallery_item_title_size` | 24sp | 列表标题 |
| `gallery_item_count_size` | 22sp | 列表副标题 |
| `gallery_empty_message_size` | 22sp | 空状态文字 |
| **布局尺寸** | | |
| `gallery_page_padding_horizontal` | 16dp | 页面水平边距 |
| `gallery_item_min_height` | 96dp | 列表项最小高度 |
| `gallery_item_padding` | 12dp | 列表项内边距 |
| `gallery_item_content_margin` | 12dp | 列表项内容间距 |
| `gallery_cover_size` | 88dp | 列表项封面尺寸 |
| `gallery_media_item_height` | 120dp | 网格图片高度 |
| `gallery_empty_state_padding` | 24dp | 空状态容器内边距 |
| `gallery_empty_button_min_width` | 160dp | 按钮最小宽度 |
| `gallery_empty_button_margin_top` | 16dp | 按钮顶部间距 |
| `gallery_empty_button_min_height` | 48dp | 按钮最小高度 |

## 4. 交互体验 (User Experience)

*   **沉浸式状态栏**：
    *   Activity 设置 `WindowCompat.setDecorFitsSystemWindows(window, false)`。
    *   根布局设置 `android:fitsSystemWindows="true"`（或根据需要处理 WindowInsets）。
    *   状态栏背景透明，图标自动适配浅色/深色模式（`windowLightStatusBar`）。
*   **权限处理**：
    *   未授权时显示空状态页面，提供明确的“授权并进入相册”按钮。
    *   授权后自动加载数据。
*   **加载反馈**：
    *   数据扫描过程中显示居中的 `ProgressBar`。

## 5. 修改记录

*   **2026-01-08**:
    *   移除 TensorFlow 标题栏，改为自定义 Header。
    *   调整搜索图标至 Header 右侧。
    *   Tab 名称由“相册”改为“文件夹”。
    *   引入沉浸式状态栏。
    *   全面重构资源文件，提取 `dimens` 和 `styles`，实现语义化管理。
    *   清理未使用的相机相关资源。
