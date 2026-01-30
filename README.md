# 日记应用（DiaryTest）

本项目是一个基于 Kotlin 的 Android 日记应用，面向日常记录与整理场景，提供“新增-查看-编辑-管理”的完整流程。应用支持图片/视频/文件附件、分类与标签管理，以及字体样式/字号/颜色等个性化设置，并提供“密码保护条目”的访问控制。数据默认本地落地到 SQLite，同时内置实验性 AI 文本润色/摘要入口，便于快速生成当日日记初稿。

## 功能总览

- **日记记录**：新增、查看、编辑、删除日记条目（标题 + 正文）
- **分类与标签**：内置分类（Personal / Work / Travel / Study / Other）并支持自定义标签
- **多媒体附件**：支持图片、视频、文件附件；提供缩略预览与点击查看
- **搜索**：按标题/正文/标签进行模糊搜索
- **样式定制**：支持字体样式（正常/加粗/斜体）、字号与颜色
- **访问保护**：支持为条目设置密码；列表与操作前需校验密码
- **AI 辅助（实验性）**：基于输入事件生成简短日记摘要，并可一键跳转到新增页继续编辑

## 主要界面与流程

- **首页（HomePageActivity）**：入口导航，分别进入“新增日记”“查看日记”“AI 功能”
- **新增日记（AddDiaryActivity）**：填写标题与正文，选择分类与标签，设置样式，添加多媒体附件与密码保护
- **日记列表（ViewDiaryActivity）**：列表浏览、搜索、编辑/删除；受保护条目需验证密码
- **日记详情（DiaryDetailActivity）**：展示内容与附件，按保存的样式进行渲染
- **编辑日记（EditDiaryActivity）**：修改内容与样式，增删附件，更新密码保护
- **媒体预览（ImagePreviewActivity）**：图片点击放大预览
- **AI 功能页（AiFunctionActivity）**：输入要点 -> 生成摘要 -> 一键带入新增日记页

## 技术栈与依赖

- **语言/平台**：Kotlin + Android SDK
- **UI**：XML 布局 + RecyclerView + Material Components
- **数据**：SQLiteOpenHelper（本地 SQLite）
- **图片加载**：Glide
- **网络**：OkHttp（AI 功能依赖）
- **AI**：阿里 DashScope SDK（Qwen 模型）
- **构建**：Gradle (Kotlin DSL)

> SDK 配置：`minSdk=24`，`targetSdk=34`，`compileSdk=34`

## 数据存储与表结构

数据库名称：`DiaryApp.db`，位于应用本地存储。

- **DiaryEntry**
  - `id`：主键
  - `title` / `content` / `date`
  - `font_style` / `font_size` / `color`
  - `category_id`
  - `is_protected` / `password`
- **Media**：`id` / `diaryEntryId` / `uri` / `type`
- **Tag**：`id` / `name`
- **EntryTag**：`id` / `entry_id` / `tag_id`
- **Category**：`id` / `name`（初始化自 `strings.xml`）

## 权限说明

项目在 `AndroidManifest.xml` 中声明了以下权限（实际使用情况以功能为准）：

- **文件/媒体读取**：读取图片、视频与附件 (`READ_MEDIA_*` / `READ_EXTERNAL_STORAGE`)
- **存储写入**：用于文件处理与持久化 URI 权限（`WRITE_EXTERNAL_STORAGE`，部分版本系统可能忽略）
- **网络访问**：AI 功能调用外部服务（`INTERNET`）
- **定位权限**：声明了定位权限，但目前代码中未实际使用

## AI 功能配置（重要）

AI 功能依赖阿里 DashScope SDK（Qwen 模型）。

1. 在本地通过 `local.properties` 或环境变量配置 Key
2. 在代码中读取配置值（避免硬编码）

当前项目由于当时Bug问题无奈将API Key硬编码到代码当中，切勿做出类似操作

## 构建与运行

前置环境：

- JDK 11 或以上
- Android Studio（或已配置的 Android SDK）

构建 Debug 包：

```bash
./gradlew assembleDebug
```

安装到设备/模拟器：

```bash
./gradlew installDebug
```

也可直接用 Android Studio 打开项目并运行。

## 测试

单元测试：

```bash
./gradlew test
```

仪器测试（需设备或模拟器）：

```bash
./gradlew connectedAndroidTest
```

测试目录：`app/src/test/java/com/example/diarytest/` 与 `app/src/androidTest/java/com/example/diarytest/`。

## 项目结构（核心文件）

- `app/src/main/java/com/example/diarytest/Activity/`
  - `HomePageActivity.kt`：首页入口
  - `AddDiaryActivity.kt`：新增日记
  - `ViewDiaryActivity.kt`：列表/搜索/删除
  - `DiaryDetailActivity.kt`：详情展示
  - `EditDiaryActivity.kt`：编辑日记
  - `AiFunctionActivity.kt`：AI 辅助
  - `ImagePreviewActivity.kt`：图片预览
- `app/src/main/java/com/example/diarytest/Database/DiaryDatabaseHelper.kt`：数据库核心逻辑
- `app/src/main/java/com/example/diarytest/Data/`：数据模型（DiaryEntry / MediaItem / Tag / Category）
- `app/src/main/java/com/example/diarytest/Adapter/`：RecyclerView 适配器
- `app/src/main/java/com/example/diarytest/utils/`：工具类（日期格式化、密码加密）
- `app/src/main/res/layout/`：主要界面布局

## 使用与注意事项

- **受保护条目**：仅做访问控制（密码加密存储），内容仍保存在本地数据库中
- **媒体附件**：使用持久化 URI 权限；删除日记条目不会删除原始文件
- **标签搜索**：支持按标题/内容/标签模糊查询

## 贡献与许可

本项目为小组作业，有共五位贡献者

欢迎提出 Issue 或 PR。请在提交中说明变更意图，并尽量补充测试。

当前仓库未包含明确许可证文件，如需开源许可请添加 `LICENSE` 并在此处说明。
