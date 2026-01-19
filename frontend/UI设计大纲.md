
# AI + Excel 前端 UI 设计大纲

## 概览

- **目标**：构建企业级的 AI+Excel 智能工作台，覆盖从文件上传、自然语言命令到可视化和下载的完整工作流。
- **风格定位**：科技感、极简、模块化，强调数据与智能交互体验，优先桌面与大屏展示。

## 设计原则

- 简洁明确：最少操作完成文件上传 + AI 指令。
- 可见反馈：上传进度、AI 处理状态、流式响应实时呈现。
- 可恢复性：处理结果可预览、撤销或下载。
- 无障碍与国际化：中文优先，支持英文/多语言扩展。

## 信息架构

- Landing：产品简介、核心能力演示（短视频/动效）。
- Workspace（主工作区）：上传区、数据预览、AI 命令区、操作历史、输出/下载。
- Analysis（分析中心）：图表建议、自动报告、RFM/CLV/财务分析入口。
- Templates（模板库）：公式、图表、清洗/透视/建模流程模板。
- Settings（设置与监控）：服务状态、API 配置、日志、审计。

### 页面清单与交互流程（扩展）

- 首页 / 仪表盘（Dashboard）
  - 目标：入口、展示最近任务、示例命令与常见操作。
  - 主要动作：上传文件、进入 AI 处理页面、查看历史文件。
- 上传与预览页面（Upload & Preview）
  - 组件：`FileUploader`、`PreviewTable`、文件元信息（大小、Sheet 列表）。
  - 行为：文件验证（格式/大小）、展示首个 Sheet 的头 50 行预览。
  - API：POST `/api/excel/get-data`。
- AI 处理主界面（AI Excel Processor）
  - 组件：`FileUploader`（可替换文件）、`CommandInput`（自然语言）、`CommandPresets`、`ProcessingPanel`（日志/命令结果）。
  - 行为：提交命令后显示流水线：解析 -> 执行命令列表 -> 命令结果（逐项显示），支持部分流式显示 AI 响应（若后端有流式接口）。
  - API：POST `/api/ai/excel-with-ai`（获取 JSON 响应）、POST `/api/ai/excel-with-ai-download`（直接下载）。
- 结果预览与下载（Result Preview & Download）
  - 组件：`PreviewTable`（高亮更改行/列）、`ChangeList`（列出 [INSERT_COLUMN|APPLY_FORMULA|DELETE_ROW...]）、下载按钮。
  - 行为：允许用户接受/撤销整组操作或单条命令，最终调用下载接口或保存为历史版本。
- 分析与图表建议（Analysis & Chart Suggestions）
  - 页面/弹层：选择分析类型（销售趋势、RFM、CLV 等），展示 AI 分析文本与表格摘要，提供图表建议卡片。
  - API：`/api/ai/excel-analyze`, `/api/ai/suggest-charts`, `/api/excel/create-chart`。
- 聊天与流式交互（AI Chat）
  - 组件：`ChatWindow`、`MessageInput`、流式接收器（显示 chunk）。
  - 支持：同步对话（`/api/ai/chat`）、流式（`/api/ai/chat-stream`）、SSE（`/api/ai/chat-sse`）。
- 高级工具页（Smart Operations）
  - 功能：智能清理、转换、验证、透视表与智能图表创建。
  - API：`/api/ai/smart-data-cleaning`, `/api/ai/smart-data-transformation`, `/api/ai/smart-data-validation` 等。
- 系统与状态（Status & Health）
  - 用于展示后端状态、API Key 配置提示（尽管当前无需认证）。
  - API：`/api/status`, `/api/health`。

## 主要页面与模块

- **文件上传面板**：支持拖拽、批量上传、格式与大小校验、上传进度、示例文件。
- **数据预览表格**：分页、列类型识别、列头智能检测、单元格快速编辑、差异高亮预览。
- **AI 命令输入窗**：自然语言输入、智能补全、命令模板、示例与语义提示。
- **AI 聊天 / 流式反馈**：流式文本展示（SSE/Streaming）、建议卡片、可操作的“应用/编辑/忽略”按钮。
- **操作步骤面板（可视化脚本）**：AI 响应自动转为可编辑步骤序列（支持拖拽重排、启用/禁用、回滚）。
- **公式生成器**：上下文感知公式建议、公式预览与应用范围选择。
- **图表与可视化面板**：图表建议、交互式配置、实时预览与导出。
- **结果导出与下载**：导出处理后 Excel、PDF 报告、审计日志导出。

## 组件列表与规格（细化）

### FileUploader

- Props: `acceptedTypes`, `maxSize`, `onUpload(file)`。
- 功能: 拖拽/选择、显示进度、失败重试、文件元信息、列出 sheet。

### PreviewTable

- Props: `data`(array-of-rows), `highlightChanges`, `onCellEdit`。
- 功能: 支持分页、排序、列隐藏、导出 CSV、高亮变更、按阈值限制预览（最多前 200 行、20 列）。

### CommandInput

- Props: `placeholder`, `presets`, `onSubmit(command)`。
- 功能: 文本框 + 提交按钮 + 示例命令下拉 + 历史命令 + 公式建议集成。

### ProcessingPanel

- Props: `logs`, `commandResults`。
- 功能: 实时追加日志、显示每条命令执行状态、可展开查看详单、支持流式更新。

### ChangeList

- Props: `changes`(array), `onAcceptAll`, `onReject(changeId)`。
- 功能: 列出 AI 生成的操作，支持单项接受/撤销、批量接受。

### ChatWindow / StreamViewer

- 功能: 支持流式追加消息、停止/继续流、显示错误与重试。

### ChartSuggestionCard

- Props: `suggestionText`, `examplePreview`, `confidence`。

## AI 与 Excel 深度融合体验要点

- **上下文感知**：AI 在工作区持续保留上下文（当前 Sheet、选区、列元数据）。
- **可解释性**：AI 返回包含“自然语言说明 + 可执行指令序列 + 变更预览”。
- **工作流编排**：将多条命令合并为可复用的模板或自动化流程。
- **沙盒校验**：应用批量操作或公式前进行模拟校验并提示风险。
- **渐进自动化**：提供“建议”“半自动”“全自动”三档，按信任度逐步提升自动化。

## 可访问性、性能与安全

- **无障碍**：键盘导航、可读对比度、屏幕阅读器支持。
- **性能**：大文件流式读取、前端虚拟滚动、后端异步任务与任务进度反馈。
- **安全**：文件类型/大小限制、敏感数据脱敏选项、私有部署建议。
- **审计**：完整操作日志、AI 决策追踪、版本控制与回滚。

## 状态、错误处理与用户反馈

- 上传阶段：文件大小/类型校验本地完成，出错提示友好文案，支持失败重试。
- 处理阶段：显示全局 loading、命令列表中每条命令的状态（pending/success/fail）。
- 流式响应：边显示边滚动，失败时允许重试最后一步或重跑全部命令。
- 全局错误弹窗：捕获 400/500，显示可操作提示（例如：检查文件格式，稍后重试）。

## 验证与限制

- 文件类型：`.xlsx`, `.xls`, `.csv`。
- 单文件最大：建议 20 MB（可配置；前端以 5–20 MB 做保护性提示）。
- 行列预览阈值：最多展示前 200 行、前 20 列以保证性能。

## 无障碍与响应式要求

- 无障碍：表单元素支持键盘导航、ARIA 标签、语义化按钮。
- 对比度：文字与背景对比度符合 WCAG AA。
- 屏幕尺寸：
  - Mobile (<= 480px)：简化视图，主操作为上传+命令输入+结果简要预览。
  - Tablet (481–1024px)：两栏布局（预览 + 控制）。
  - Desktop (>1024px)：三栏或主侧栏布局（左导航/中预览/右控制）。

## 国际化与本地化

- 文本资源集中管理，支持中文/英文切换（建议使用 `vue-i18n` 或类似方案）。

## 安全与隐私

- 前端仅在必要时上传文件；敏感信息警示与脱敏建议由后端实现。
- 限制上传来源并实施大小与类型检查。

## 可观测性与分析事件

- 关键事件埋点（上传开始/完成、AI 请求开始/完成、下载、错误）。
- 建议记录：命令文本（脱敏处理）、处理耗时、成功率。

## API 映射（UI -> 后端）

- 上传并预览: POST `/api/excel/get-data` -> 展示 `data` 数组。
- AI 处理（JSON）: POST `/api/ai/excel-with-ai` -> 返回 `excelDataPreview`, `commandResults`, `outputFile`。
- AI 下载: POST `/api/ai/excel-with-ai-download` -> 返回文件流，前端触发下载。
- 生成公式: POST `/api/ai/generate-formula` -> 在 `CommandInput` 中提供公式建议。
- 分析: POST `/api/ai/excel-analyze` -> 在 Analysis 页面展示 `analysis`。
- 图表建议: POST `/api/ai/suggest-charts` -> 渲染 `ChartSuggestionCard`。
- 聊天: POST `/api/ai/chat`、流式 `/api/ai/chat-stream`、SSE `/api/ai/chat-sse`。

注意：所有交互需要关注后端返回的 `success` 字段与 `error` 字段。

## 技术栈建议

- 框架：Vue（示例以 Vue 组件为主）。
- 状态管理：Vue Query / Pinia 或 Vuex（建议以异步请求缓存为主）。
- 样式：Tailwind CSS 或 Ant Design（企业级控件库）。
- 国际化：`vue-i18n`。

## 接受标准（验收条件）

- 能上传并预览 Excel，显示前端验证错误。
- 能用自然语言命令提交 AI 处理并展示 `commandResults` 与 `excelDataPreview`。
- 支持处理结果的单项接受/撤销与最终下载。
- 聊天功能支持同步与流式显示。
- 页面在主流屏幕尺寸下正常工作并满足基本无障碍要求。

---
> 文件生成于项目：UI 设计以提升可解释性与可控性为目标，建议先完成线框验证交互再推进高保真实现。
