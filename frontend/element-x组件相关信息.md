
# 其他核心组件信息

---

## 1. Sender (输入组件) ✉️

这是 AI 对话界面中最核心的组件，相比普通 Input，它集成了多模态交互。

* **功能特点**：
* **多模态支持**：原生集成附件上传入口（可与 Attachments 配合）。
* **输入增强**：支持回车发送、`Ctrl+Enter` 换行、清空、语音输入（需配合相关插件）。
* **操作插槽**：提供 `#prefix`、`#action-list` 等插槽用于自定义快捷功能按钮。

* **代码示例**：

```vue
<Sender 
  v-model="inputValue" 
  placeholder="输入消息..." 
  @submit="handleSubmit"
  loading
/>

```

## 2. Bubble & BubbleList (聊天气泡) 💬

用于展示 AI 和用户的对话记录，支持流式渲染效果。

* **功能特点**：
* **Bubble (单气泡)**：支持设置 `role`（user/assistant）、头像、加载状态。
* **BubbleList (气泡列表)**：批量管理对话流，内置自动滚动到触底、多轮对话渲染逻辑。
* **内容渲染**：支持 Markdown 解析和代码高亮。

* **代码示例**：

```vue
<BubbleList :list="chatHistory" />

```

### 3. Conversations (会话管理) 🗂️

用于管理侧边栏的会话列表。

* **功能特点**：
* **状态展示**：支持选中态、未读标识。
* **快捷操作**：支持对会话进行重命名、删除、置顶等交互。
* **性能优化**：针对超长列表（10万+会话）进行了渲染优化。

### 4. Thinking & ThoughtChain (思维链/思考状态) 🧠

用于展示 AI 生成过程中的中间推理过程。

* **功能特点**：
* **ThoughtChain**：一种折叠/展开式的节点展示，用于展示 AI 思考的每一个步骤，增加透明度。
* **Thinking**：提供波浪纹或闪烁的加载动画，提示用户 AI 正在“思考”中。

### 5. Typewriter (打字机效果) ⌨️

针对 AI 流式输出（SSE）专门设计的文本增强组件。

* **功能特点**：
* **平滑度**：解决大块文本直接蹦出导致的视觉冲击，模拟真实的打字速度。
* **增量渲染**：仅对新到达的字符进行动画处理。

---

### 组件 API 对比一览表

| 组件名称 | 主要场景 | 核心属性/事件 |
| --- | --- | --- |
| **Sender** | 消息发送、文件上传集成 | `loading`, `submit`, `action-list` |
| **Bubble** | 单条消息展示、角色区分 | `content`, `role`, `avatar` |
| **BubbleList** | 完整对话流渲染 | `list`, `auto-scroll` |
| **Conversations** | 历史记录侧边栏 | `active-id`, `items`, `click` |
| **Thinking** | AI 响应等待 | `active`, `speed` |
| **Typewriter** | 流式文本展示 | `text`, `speed`, `finish` |

---

### CSS 样式风格

Element Plus X 的样式完全兼容 **Element Plus** 的主题系统。

* **主题同步**：支持一键切换暗黑模式（Dark Mode）。
* **CSS 变量**：所有 AI 组件均使用 `--epx-` 前缀的 CSS 变量，方便在 `App.vue` 或全局样式中进行色彩和圆角的微调。
