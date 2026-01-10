# ElinkAIGallery UI 测试说明

## 运行环境
- Android 13/14/15 模拟器（API 33+），建议使用 Google APIs 镜像。
- 设备上关闭系统动画可减少 UI 波动（可选）：
  - 开发者选项中将 Window/Transition/Animator duration 设为 0.5x 或 Off。

## 测试数据准备
- Instrumentation 测试会自动从 `app/src/androidTest/assets/coffee.jpg` 注入测试图片到 MediaStore。
- 测试会在 `Pictures/ElinkAIGalleryTest/<Folder>` 下创建图片，不需要手动导入图片。
- 测试中涉及系统权限与删除确认弹窗，使用 UIAutomator 自动点击“允许/删除”按钮。

## 运行方式
在项目根目录执行：

```bash
./gradlew connectedAndroidTest
```

如需只跑 UI 测试类，可在 Android Studio 中运行：
- `GalleryPermissionTest`
- `GalleryUiTest`
