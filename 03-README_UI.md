# ElinkAIGallery UI 交互说明

本文档说明当前版本各界面的交互逻辑与跳转关系，适配现有实现，不包含规划或未落地功能。

## 1. 全局导航与结构
- 单 Activity + Navigation Component。
- 主要页面链路：`GalleryFragment -> MediaGridFragment -> PhotoFragment`。
- “人物”分类额外链路：`GalleryFragment -> PersonGridFragment -> MediaGridFragment -> PhotoFragment`。

## 2. GalleryFragment（主界面）
### 2.1 入口与切换
- 顶部标题左侧为“相册”，右侧常驻 `SearchView`。
- 底部浮动栏两个图标按钮切换“文件夹 / 智能分类”，同时驱动 `ViewPager2` 切页。
- `ViewPager2` 也支持左右滑动切换。

### 2.2 搜索覆盖层
- 搜索框输入后，隐藏 `ViewPager2`，显示搜索结果网格（3 列）。
- 搜索结果为空时显示搜索空状态提示。
- 清空搜索文本后恢复原始分页内容。

### 2.3 权限与空状态
- 未授权或无数据时显示空状态页与授权按钮。
- 点击“授权”按钮触发系统权限请求。

## 3. GalleryTabFragment（文件夹 / 智能分类列表）
### 3.1 文件夹列表
- 点击文件夹进入 `MediaGridFragment`，展示该文件夹内图片。
- 长按文件夹触发删除确认，删除该文件夹内全部图片（系统级确认）。

### 3.2 智能分类列表
- 点击“人物”进入 `PersonGridFragment`。
- 点击“美食 / 风景”进入 `MediaGridFragment`，展示对应标签的图片集合。
- 长按分类触发删除确认，删除该分类下全部图片（系统级确认）。

## 4. PersonGridFragment（人物目录）
- 列表样式与主界面一致（封面图 + 标题 + 数量）。
- 点击人物卡片进入 `MediaGridFragment`，展示该人物相关照片。
- 当前不提供删除入口。

## 5. MediaGridFragment（图片网格）
- 3 列网格展示图片。
- 点击图片进入 `PhotoFragment`（大图浏览）。
- 长按单张图片触发删除确认（系统级确认）。

## 6. PhotoFragment（大图浏览）
- 支持左右滑动切换当前列表中的图片。
- 支持双指缩放/拖拽/双击放大（`ZoomImageView`）。
- 右上角删除按钮触发删除确认（系统级确认）。
- 删除成功后列表刷新；若当前列表为空则返回上一页。

## 7. 删除与系统确认流程
- 所有删除均通过系统级删除确认流程触发。
- 删除确认通过后：
  - Room 先删本地记录
  - UI 基于 Flow 刷新显示

## 8. 交互要点总结
- 搜索优先：搜索输入时覆盖所有分页内容。
- 删除一致性：所有入口删除均需确认，且删除后以 Room 为准刷新。
- 人物目录仅浏览：当前不在人物目录提供删除操作。
