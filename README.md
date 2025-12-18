# AI Excel Integration 企业级智能Excel处理系统

## 项目概述

AI Excel Integration 是一个功能强大的企业级Excel处理平台，将人工智能技术与传统的Excel操作相结合，为用户提供智能化的电子表格处理能力。项目采用Spring Boot后端架构，集成通义千问(Qwen) API，支持自然语言处理Excel数据，提供从基础的数据读取到高级的商业分析功能。

## 主要功能

### 1. AI驱动的Excel处理
- **自然语言处理**: 用户可通过自然语言指令操作Excel文件，如"创建一个新列'Sum'并计算A列和B列的和"
- **智能公式生成**: 基于上下文和需求自动生成Excel公式
- **数据智能分析**: 自动识别数据模式，生成分析报告
- **图表建议**: 基于数据特征推荐合适的图表类型

### 2. 数据操作功能
- **数据读取**: 支持多种Excel格式的数据读取
- **数据排序**: 按指定列和顺序进行数据排序
- **数据筛选**: 基于条件对数据进行筛选
- **数据清理**: 智能识别并清理无效数据

### 3. 商业分析模块
#### 客户分析
- **RFM分析**: 评估客户最近购买、购买频率和购买金额
- **客户生命周期价值(CLV)**: 计算客户长期价值
- **客户细分**: 将客户分为不同群体并提供个性化策略
- **流失预测**: 识别高风险客户并提供干预建议
- **CAC vs CLV分析**: 分析客户获取成本与生命周期价值关系
- **群组分析**: 按时间维度分析客户行为模式

#### 财务分析
- **财务报表分析**: 综合分析资产负债表、利润表等
- **财务比率计算**: 计算流动比率、资产负债率、ROE等关键指标
- **盈利能力分析**: 分析毛利率、净利率等盈利能力指标
- **现金流分析**: 分析经营、投资、融资现金流
- **预算与实际对比**: 分析预算执行情况

### 4. 高级AI功能
- **智能数据清理**: 根据指令自动清理数据
- **智能数据转换**: 自动执行数据格式转换
- **智能数据分析**: 识别数据趋势并进行预测
- **智能图表创建**: 根据需求自动创建图表
- **智能数据验证**: 自动验证数据完整性和准确性

### 5. 实时通信
- **WebSocket支持**: 提供实时处理反馈
- **SSE流式响应**: 支持AI响应的流式输出
- **实时预览**: 提供处理过程的实时预览

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: JPA + H2 (开发环境), Redis (会话管理)
- **Excel处理**: Apache POI (5.2.5)
- **HTTP客户端**: Apache HttpClient5 (5.2.1)
- **JSON处理**: Jackson
- **Web服务**: Spring Web + WebSocket

### AI服务
- **API**: 通义千问(Qwen) API (兼容OpenAI格式)
- **模型**: 支持qwen-turbo、qwen-plus、qwen-max等多种模型
- **功能**: 自然语言理解、公式生成、数据分析、图表建议

### 前端技术栈
- **框架**: Bootstrap 5, jQuery
- **组件**: 实时聊天界面、文件上传组件、数据分析仪表板
- **图表**: Chart.js (集成图表展示)

## 环境配置

### 系统要求
- Java 17 或更高版本
- Maven 3.6 或更高版本
- 通义千问API密钥

### 环境变量配置
```bash
# 创建环境配置文件
cp .env.example .env

# 编辑 .env 文件
QWEN_API_KEY=your_actual_qwen_api_key_here
QWEN_MODEL_NAME=qwen-max
SERVER_PORT=8080
```

### 通义千问API配置
1. 访问 [阿里云通义千问](https://dashscope.aliyuncs.com/) 获取API密钥
2. 将API密钥配置到环境变量或配置文件中
3. 选择合适的模型类型（qwen-turbo速度最快，qwen-max功能最强）

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/silverenternal/ai-excel-integration.git
cd ai-excel-integration
```

### 2. 构建项目
```bash
# 使用Maven构建
mvn clean compile
mvn package
```

### 3. 运行应用
```bash
# 方式1: 使用Maven
mvn spring-boot:run

# 方式2: 使用JAR包
java -jar target/ai-excel-integration-0.0.1-SNAPSHOT.jar

# 方式3: 使用启动脚本
./start.sh
```

### 4. 访问应用
打开浏览器访问 `http://localhost:8080`

## API端点详述

### 文件操作API
- `POST /api/upload` - 上传Excel文件
- `POST /api/excel/get-data` - 获取Excel数据

### AI处理API
- `POST /api/ai/excel-with-ai` - AI处理Excel（返回结果）
- `POST /api/ai/excel-with-ai-download` - AI处理Excel（直接下载）
- `POST /api/ai/generate-formula` - 生成Excel公式
- `POST /api/ai/excel-analyze` - Excel数据分析
- `POST /api/ai/suggest-charts` - 图表建议

### 数据处理API
- `POST /api/excel/create-chart` - 创建图表
- `POST /api/excel/sort-data` - 数据排序
- `POST /api/excel/filter-data` - 数据筛选

### 客户分析API
- `POST /api/analysis/rfm` - RFM分析
- `POST /api/analysis/clv` - 客户生命周期价值分析
- `POST /api/analysis/customer-segmentation` - 客户细分
- `POST /api/analysis/churn-prediction` - 流失预测
- `POST /api/analysis/cac-clv` - CAC与CLV分析
- `POST /api/analysis/cohort` - 群组分析

### 财务分析API
- `POST /api/analysis/financial` - 财务报表分析
- `POST /api/analysis/financial-ratios` - 财务比率计算
- `POST /api/analysis/profitability` - 盈利能力分析
- `POST /api/analysis/cash-flow` - 现金流分析
- `POST /api/analysis/budget-actual` - 预算与实际对比

### 交流API
- `POST /api/ai/chat` - 普通AI聊天
- `POST /api/ai/chat-stream` - 流式AI聊天
- `GET /api/ai/chat-sse` - SSE流式AI聊天

### 系统API
- `GET /api/health` - 健康检查
- `GET /api/status` - 服务状态

## 使用示例

### 1. 自然语言处理Excel
```bash
curl -X POST \
  -F "file=@sales_data.xlsx" \
  -F "command=创建一个新列'利润率'，计算公式为(销售额-成本)/销售额" \
  http://localhost:8080/api/ai/excel-with-ai
```

### 2. AI生成公式
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"context": "A列是销售额，B列是成本", "goal": "计算利润"}' \
  http://localhost:8080/api/ai/generate-formula
```

### 3. 客户RFM分析
```bash
curl -X POST \
  -F "file=@customer_transactions.xlsx" \
  http://localhost:8080/api/analysis/rfm
```

### 4. 财务比率分析
```bash
curl -X POST \
  -F "file=@financial_statements.xlsx" \
  http://localhost:8080/api/analysis/financial-ratios
```

### 5. WebSocket实时通信
WebSocket端点: `/websocket/{clientId}`

## 前端界面功能

### 主要界面组件
1. **文件上传区域**: 支持拖拽上传，支持.xlsx、.xls、.csv格式
2. **命令输入框**: 自然语言输入Excel操作命令
3. **实时预览**: 显示Excel数据的表格视图
4. **分析结果面板**: 展示AI分析结果和建议
5. **聊天界面**: 与AI助手的对话区域，支持流式响应

### 使用流程
1. 上传Excel文件
2. 输入自然语言命令或选择预设功能
3. 等待AI处理并查看结果
4. 下载处理后的Excel文件或继续进行其他操作

## 高级功能说明

### 商业智能分析
系统集成了专业的商业分析能力，可帮助用户从Excel数据中提取有价值的商业洞察：

#### 客户分析能力
- **RFM模型**: 基于Recency（最近购买时间）、Frequency（购买频次）、Monetary（购买金额）三个维度分析客户价值
- **客户细分**: 运用聚类算法将客户分为高价值客户、潜在客户、忠诚客户、流失客户等群体
- **流失预测**: 基于历史行为数据预测客户流失风险，并提供干预策略
- **生命周期价值**: 计算客户在整个生命周期内的价值，指导营销资源分配

#### 财务分析能力
- **比率分析**: 自动计算流动性、偿债能力、盈利能力、运营效率等关键财务比率
- **趋势分析**: 识别收入、成本、利润等财务指标的变化趋势
- **现金流管理**: 分析经营、投资、融资现金流，提供现金流管理建议
- **预算控制**: 对比预算与实际执行情况，分析差异原因

### 智能化处理流程
1. **数据理解**: AI自动识别Excel数据结构和内容
2. **意图识别**: 理解用户的自然语言指令意图
3. **操作规划**: 规划需要执行的Excel操作步骤
4. **执行反馈**: 实时反馈操作进度和结果
5. **结果验证**: 验证处理结果的正确性

## 部署说明

### 生产环境部署
```bash
# Docker部署
docker build -t ai-excel-integration .
docker run -d -p 8080:8080 -e QWEN_API_KEY=your_key ai-excel-integration

# 系统服务部署
sudo cp ai-excel-integration.service /etc/systemd/system/
sudo systemctl enable ai-excel-integration
sudo systemctl start ai-excel-integration
```

### 性能优化
- **文件大小限制**: 默认限制为10MB，可根据需要调整
- **缓存策略**: 使用Redis缓存频繁访问的数据
- **异步处理**: 复杂操作采用异步处理，提高响应速度
- **数据库优化**: 针对大数据集进行索引和查询优化

## 维护和监控

### 监控指标
- API响应时间
- 错误率
- 并发用户数
- 文件处理成功率

### 备份策略
- 定期备份配置文件
- 监控API密钥安全
- 保持系统更新

## 贡献指南

欢迎提交Issue和Pull Request来改进项目。在提交之前，请确保：
- 代码遵循项目编码规范
- 添加适当的测试用例
- 更新相关文档

## 许可证

本项目采用 [许可证类型] 许可证。

## 技术支持

如遇问题，请通过以下方式获取支持：
- 提交GitHub Issue
- 查阅API文档 (`API接口文档.md`)
- 检查功能同步文档 (`FUNCTIONALITY_SYNC.md`)

该项目为企业用户提供了一个功能全面、易于使用、智能化的Excel处理平台，帮助用户更高效地处理和分析Excel数据。