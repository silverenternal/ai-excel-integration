# AI Excel Integration - 企业级API接口文档

## 1. 接口规范

### 1.1 API基础信息

- **协议**: HTTP
- **域名**: `http://localhost:8080` (开发环境)
- **认证方式**: 无需认证
- **请求头**: `Content-Type: multipart/form-data` (文件上传接口)
- **字符编码**: UTF-8

### 1.2 数据格式规范

#### 响应数据结构

```json
{
  "success": true,
  "data": {},
  "error": "错误信息（可选）"
}
```

#### 常用响应字段说明

- `success`: Boolean - 操作是否成功
- `data`: Object - 具体响应数据
- `error`: String - 错误信息（失败时）

### 1.3 认证与授权

当前版本的所有API均无需认证。

## 2. 文件上传接口

### 2.1 上传Excel文件

- **接口**: `POST /api/upload`
- **功能**: 上传Excel文件
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  http://localhost:8080/api/upload
```

#### 响应示例

```json
{
  "success": true,
  "message": "File uploaded successfully"
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

## 3. AI处理接口

### 3.1 与AI一起处理Excel

- **接口**: `POST /api/ai/excel-with-ai`
- **功能**: 使用AI分析自然语言命令并执行Excel操作
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `command`: String - 自然语言命令

#### 请求示例

```bash
curl -X POST \
  -F "file=@sample.xlsx" \
  -F "command=创建一个新列'Sum'并计算A列和B列的和" \
  http://localhost:8080/api/ai/excel-with-ai
```

#### 响应示例

```json
{
  "success": true,
  "excelDataPreview": "Sheet: Sheet1\nName\tValue1\tValue2\t\nItem1\t10.0\t20.0\t\nItem2\t30.0\t40.0\t\nItem3\t50.0\t60.0\t\n\n...",
  "outputFile": "modified_sample.xlsx",
  "commandResults": [
    {
      "success": true,
      "commandType": "INSERT_COLUMN",
      "commandParams": "3:Sum",
      "message": "Successfully inserted column at 3"
    },
    {
      "success": true,
      "commandType": "APPLY_FORMULA",
      "commandParams": "C2==A2+B2",
      "message": "Successfully calculated and set result 30.0 to cell C2"
    }
  ],
  "command": "创建一个新列'Sum'并计算A列和B列的和",
  "aiResponse": "[INSERT_COLUMN:3:Sum]\n[APPLY_FORMULA:C2:=A2+B2]\n[APPLY_FORMULA:C3:=A3+B3]",
  "fileId": "sample.xlsx_123456789"
}
```

### 3.2 与AI一起处理Excel并下载

- **接口**: `POST /api/ai/excel-with-ai-download`
- **功能**: 使用AI处理Excel后直接返回处理后的Excel文件
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `command`: String - 自然语言命令
- **响应格式**: Excel文件（application/vnd.openxmlformats-officedocument.spreadsheetml.sheet）

#### 请求示例

```bash
curl -X POST \
  -F "file=@sample.xlsx" \
  -F "command=创建一个新列'Sum'并计算A列和B列的和" \
  http://localhost:8080/api/ai/excel-with-ai-download \
  -H "Accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" \
  --output modified_sample.xlsx
```

#### 响应示例（200 OK — 文件流）

```
HTTP/1.1 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="modified_sample.xlsx"

<binary file stream>
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

### 3.3 生成Excel公式

- **接口**: `POST /api/ai/generate-formula`
- **功能**: 基于上下文和目标生成Excel公式
- **请求格式**: `application/json`
- **请求参数**:
  - `context`: String - Excel数据上下文
  - `goal`: String - 目标描述

#### 请求示例

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"context": "A列是销售额，B列是成本", "goal": "计算利润"}' \
  http://localhost:8080/api/ai/generate-formula
```

#### 响应示例

```json
{
  "success": true,
  "formula": "=A1-B1",
  "context": "A列是销售额，B列是成本",
  "goal": "计算利润"
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

### 3.4 分析Excel数据

- **接口**: `POST /api/ai/excel-analyze`
- **功能**: 使用AI分析Excel数据
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `analysisRequest`: String - 分析请求

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  -F "analysisRequest=分析销售趋势" \
  http://localhost:8080/api/ai/excel-analyze
```

#### 响应示例

```json
{
  "success": true,
  "analysis": "根据提供的销售数据，我发现以下趋势：1.销售量在前3个月持续增长，2.随后在第4-6个月出现下降，3.第7-9个月开始恢复增长，4.最后3个月达到年度高峰。建议在销售淡季加强营销活动。",
  "excelDataPreview": "Sheet: Sales\nMonth\tSales\tRegion\t...\n...",
  "analysisRequest": "分析销售趋势",
  "commandResults": []
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

### 3.5 建议图表

- **接口**: `POST /api/ai/suggest-charts`
- **功能**: 基于Excel数据建议合适的图表类型
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  http://localhost:8080/api/ai/suggest-charts
```

#### 响应示例

```json
{
  "success": true,
  "chartSuggestions": "基于您的数据，我推荐以下图表类型：1.折线图 - 显示时间序列趋势，2.柱状图 - 比较不同类别的数值，3.饼图 - 显示各部分占比，4.散点图 - 分析变量间关系",
  "excelDataPreview": "Sheet: Sales\nMonth\tSales\tRegion\t...\n..."
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

### 3.6 AI聊天接口

- **接口**: `POST /api/ai/chat`
- **功能**: 与AI进行通用聊天对话
- **请求格式**: `application/json`
- **请求参数**:
  - `message`: String - 用户消息

#### 请求示例

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"message": "你好，你能帮我处理Excel吗？"}' \
  http://localhost:8080/api/ai/chat
```

#### 响应示例

```json
{
  "success": true,
  "message": "当然可以！我可以帮您处理Excel文件，包括：1.数据清理和转换，2.自动填充和计算，3.图表创建，4.数据分析和洞察。请上传您的Excel文件并告诉我您需要做什么。"
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

### 3.7 AI聊天流式接口

- **接口**: `POST /api/ai/chat-stream`
- **功能**: 与AI进行流式聊天对话
- **请求格式**: `application/json`
- **请求参数**:
  - `message`: String - 用户消息

#### 请求示例

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"message": "如何计算销售增长率？"}' \
  http://localhost:8080/api/ai/chat-stream
```

#### 响应示例

```json
{
  "success": true,
  "message": "销售增长率可以通过以下公式计算：(本期销售额 - 上期销售额) / 上期销售额 * 100%。例如，如果上月销售额是100,000，本月销售额是120,000，则增长率为(120,000-100,000)/100,000*100%=20%。"
}
```

### 3.8 AI聊天SSE接口

- **接口**: `GET /api/ai/chat-sse`
- **功能**: 通过SSE事件流方式与AI聊天
- **请求格式**: URL参数
- **请求参数**:
  - `message`: String - 用户消息

#### 请求示例

```bash
curl -N "http://localhost:8080/api/ai/chat-sse?message=你好"
```

#### 响应示例（SSE格式）

```
event: start
data: 

event: chunk
data: 你好

event: chunk
data: ！我是AI助手

event: chunk
data: ，我可以帮助您

event: chunk
data: 处理Excel文件

event: done
data: [DONE]
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

## 4. 数据分析接口

### 4.1 读取Excel数据

- **接口**: `POST /api/excel/get-data`
- **功能**: 读取Excel文件数据并返回数组格式
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  http://localhost:8080/api/excel/get-data
```

#### 响应示例

```json
{
  "success": true,
  "data": [
    ["Name", "Value1", "Value2"],
    ["Item1", 10.0, 20.0],
    ["Item2", 30.0, 40.0],
    ["Item3", 50.0, 60.0]
  ]
}
```

```

#### 错误示例（404 Not Found）

```

HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "File not found or could not be processed"
}

```

### 4.2 创建图表

- **接口**: `POST /api/excel/create-chart`
- **功能**: 为Excel数据创建图表
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `chartType`: String - 图表类型（柱状图、折线图等）
  - `targetColumn`: String - 目标列

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  -F "chartType=bar" \
  -F "targetColumn=Sales" \
  http://localhost:8080/api/excel/create-chart
```

#### 响应示例

```json
{
  "success": true,
  "chartInstructions": "1.选择数据范围A1:B10，2.插入->图表->柱状图，3.添加标题'销售数据图表'，4.调整图表样式",
  "chartType": "bar",
  "targetColumn": "Sales"
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "File not found or could not be processed"
}
```

### 4.3 数据排序

- **接口**: `POST /api/excel/sort-data`
- **功能**: 对Excel数据进行排序
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `sortColumn`: String - 排序列
  - `sortOrder`: String - 排序方向（ASC/DESC）

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  -F "sortColumn=Sales" \
  -F "sortOrder=DESC" \
  http://localhost:8080/api/excel/sort-data
```

#### 响应示例

```json
{
  "success": true,
  "sortInstructions": "1.选择数据范围A1:D20，2.数据->排序，3.选择排序列'Sales'，4.选择降序，5.点击确定",
  "sortColumn": "Sales",
  "sortOrder": "DESC"
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "File not found or could not be processed"
}
```

### 4.4 数据筛选

- **接口**: `POST /api/excel/filter-data`
- **功能**: 对Excel数据进行筛选
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `filterColumn`: String - 筛选列
  - `filterCondition`: String - 筛选条件（如等于、大于等）
  - `filterValue`: String - 筛选值

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  -F "filterColumn=Region" \
  -F "filterCondition=equals" \
  -F "filterValue=North" \
  http://localhost:8080/api/excel/filter-data
```

#### 响应示例

```json
{
  "success": true,
  "filterInstructions": "1.选择数据范围A1:D20，2.数据->筛选，3.点击'Region'列筛选箭头，4.选择'North'值，5.点击确定",
  "filterColumn": "Region",
  "filterCondition": "equals",
  "filterValue": "North"
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "File not found or could not be processed"
}
```

## 5. 客户分析接口

### 5.1 RFM分析

- **接口**: `POST /api/analysis/rfm`
- **功能**: 执行RFM分析（最近购买、频率、货币价值）
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含客户交易数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@customer_data.xlsx" \
  http://localhost:8080/api/analysis/rfm
```

#### 响应示例

```json
{
  "success": true,
  "rfmAnalysis": "RFM分析结果：1.高价值客户(高R、高F、高M)：建议个性化营销，2.新客户(高R、低F、低M)：需要提高购买频率，3.流失风险客户(低R、低F、低M)：需要重新激活策略",
  "excelDataPreview": "Sheet: Transactions\nCustomerID\tDate\tAmount\t...\n...",
  "analysisType": "RFM Analysis"
}
```

### 5.2 客户生命周期价值（CLV）

- **接口**: `POST /api/analysis/clv`
- **功能**: 计算客户生命周期价值
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含客户数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@customer_data.xlsx" \
  http://localhost:8080/api/analysis/clv
```

#### 响应示例

```json
{
  "success": true,
  "clvAnalysis": "客户生命周期价值分析：1.高价值客户CLV: >$5000，2.中等价值客户CLV: $1000-$5000，3.低价值客户CLV: <$1000。建议：对高价值客户提供VIP服务，对中等价值客户实施交叉销售策略。",
  "excelDataPreview": "Sheet: Customers\nCustomerID\tPurchaseHistory\t...\n...",
  "analysisType": "Customer Lifetime Value"
}
```

### 5.3 客户细分

- **接口**: `POST /api/analysis/customer-segmentation`
- **功能**: 对客户进行细分
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含客户数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@customer_data.xlsx" \
  http://localhost:8080/api/analysis/customer-segmentation
```

#### 响应示例

```json
{
  "success": true,
  "customerSegmentation": "客户细分结果：1.高价值客户：购买频繁、金额高，2.潜在客户：浏览多、购买少，3.忠诚客户：复购率高，4.流失客户：长时间未购买。建议：对高价值客户提供专属优惠，对潜在客户发送促销信息。",
  "excelDataPreview": "Sheet: CustomerData\nCustomerID\tBehavior\tValue\t...\n...",
  "analysisType": "Customer Segmentation"
}
```

### 5.4 流失预测

- **接口**: `POST /api/analysis/churn-prediction`
- **功能**: 预测客户流失风险
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含客户行为数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@customer_behavior.xlsx" \
  http://localhost:8080/api/analysis/churn-prediction
```

#### 响应示例

```json
{
  "success": true,
  "churnAnalysis": "流失风险分析：1.高风险客户(>70%流失概率)：建议立即干预，2.中风险客户(30-70%)：加强沟通，3.低风险客户(<30%)：保持现有服务。流失指标包括：购买频次下降、客服投诉增加、优惠敏感度提高。",
  "excelDataPreview": "Sheet: Behavior\nCustomerID\tLastPurchase\tActivities\t...\n...",
  "analysisType": "Churn Risk Prediction"
}
```

### 5.5 CAC与CLV分析

- **接口**: `POST /api/analysis/cac-clv`
- **功能**: 分析客户获取成本与客户生命周期价值关系
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含相关财务数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@cac_clv_data.xlsx" \
  http://localhost:8080/api/analysis/cac-clv
```

#### 响应示例

```json
{
  "success": true,
  "cacClvAnalysis": "CAC vs CLV分析：1.当前CAC: $50, 平均CLV: $400, 比率为1:8，2.理想比率应为1:3，当前比率健康但可优化。3.渠道效果：线上广告CAC $30, CLV $300; 社交媒体CAC $70, CLV $500。建议加大社交媒体投入。",
  "excelDataPreview": "Sheet: CAC_CLV\nChannel\tCAC\tCLV\t...\n...",
  "analysisType": "CAC vs CLV Analysis"
}
```

### 5.6 群组分析

- **接口**: `POST /api/analysis/cohort`
- **功能**: 执行客户群组分析
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含时间序列数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@cohort_data.xlsx" \
  http://localhost:8080/api/analysis/cohort
```

#### 响应示例

```json
{
  "success": true,
  "cohortAnalysis": "群组分析结果：1.1月注册用户群组3个月留存率70%，2.2月注册用户群组留存率65%，3.3月注册用户群组留存率60%。趋势显示早期用户留存率更高，建议分析早期产品优势并应用到后续版本。",
  "excelDataPreview": "Sheet: Cohorts\nCohort\tMonth1\tMonth2\tMonth3\t...\n...",
  "analysisType": "Cohort Analysis"
}
```

## 6. 财务分析接口

### 6.1 财务报表分析

- **接口**: `POST /api/analysis/financial`
- **功能**: 执行综合财务报表分析
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含财务报表的Excel文件
  - `type`: String - 分析类型（comprehensive默认）

#### 请求示例

```bash
curl -X POST \
  -F "file=@financial_statements.xlsx" \
  -F "type=comprehensive" \
  http://localhost:8080/api/analysis/financial
```

#### 响应示例

```json
{
  "success": true,
  "financialAnalysis": "财务报表综合分析：1.收入趋势：Q1-Q4持续增长，2.利润率：毛利率25%，净利率8%，3.资产负债：资产总额增长15%，负债率40%，4.现金流：经营现金流为正，投资现金流为负。建议：控制运营成本，优化资本结构。",
  "excelDataPreview": "Sheet: Financials\nItem\tQ1\tQ2\tQ3\tQ4\t...\n...",
  "analysisType": "comprehensive"
}
```

### 6.2 财务比率计算

- **接口**: `POST /api/analysis/financial-ratios`
- **功能**: 计算各类财务比率
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含财务数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@financial_data.xlsx" \
  http://localhost:8080/api/analysis/financial-ratios
```

#### 响应示例

```json
{
  "success": true,
  "financialRatios": "财务比率计算结果：1.流动比率: 1.8 (健康)，2.速动比率: 1.2 (良好)，3.资产负债率: 45% (适中)，4.毛利率: 28%，5.净利率: 9%，6.ROA: 6%，7.ROE: 14%。与行业对比：流动比率优于行业平均1.5，ROE高于行业平均11%。",
  "excelDataPreview": "Sheet: Ratios\nMetric\tValue\t...\n...",
  "analysisType": "Financial Ratios"
}
```

### 6.3 盈利能力分析

- **接口**: `POST /api/analysis/profitability`
- **功能**: 执行盈利能力分析
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含利润表等相关数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@profitability_data.xlsx" \
  http://localhost:8080/api/analysis/profitability
```

#### 响应示例

```json
{
  "success": true,
  "profitabilityAnalysis": "盈利能力分析：1.毛利率趋势：从Q1的25%提升至Q4的30%，2.净利率趋势：从Q1的7%提升至Q4的10%，3.成本控制：销售成本同比下降5%，运营费用增长3%。建议：继续优化成本结构，提高毛利率。",
  "excelDataPreview": "Sheet: P&L\nItem\tQ1\tQ2\tQ3\tQ4\t...\n...",
  "analysisType": "Profitability Analysis"
}
```

### 6.4 现金流分析

- **接口**: `POST /api/analysis/cash-flow`
- **功能**: 执行现金流分析
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含现金流量表的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@cash_flow.xlsx" \
  http://localhost:8080/api/analysis/cash-flow
```

#### 响应示例

```json
{
  "success": true,
  "cashFlowAnalysis": "现金流分析：1.经营现金流: $500K (正，健康)，2.投资现金流: -$200K (负，正常)，3.融资现金流: $50K (正)，4.自由现金流: $300K。现金流趋势：经营现金流同比增长15%，投资支出主要用于设备升级。建议：保持运营现金流增长，注意投资回收期。",
  "excelDataPreview": "Sheet: CashFlow\nType\tQ1\tQ2\tQ3\tQ4\t...\n...",
  "analysisType": "Cash Flow Analysis"
}
```

### 6.5 预算与实际对比

- **接口**: `POST /api/analysis/budget-actual`
- **功能**: 对比预算与实际执行情况
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - 包含预算和实际执行数据的Excel文件

#### 请求示例

```bash
curl -X POST \
  -F "file=@budget_actual.xlsx" \
  http://localhost:8080/api/analysis/budget-actual
```

#### 响应示例

```json
{
  "success": true,
  "budgetActualAnalysis": "预算vs实际对比分析：1.收入：预算$1M, 实际$1.1M, 差异+10%(F)，2.销售成本：预算$700K, 实际$650K, 差异-7%(F)，3.运营费用：预算$200K, 实际$220K, 差异+10%(U)。主要差异：收入超预期主要来自新产品线，运营费用超支因市场推广增加。建议：调整下季度预算，加强费用控制。",
  "excelDataPreview": "Sheet: BudgetActual\nItem\tBudget\tActual\tVariance\t...\n...",
  "analysisType": "Budget vs Actual"
}
```

## 7. 高级AI操作接口

### 7.1 智能数据清理

- **接口**: `POST /api/ai/smart-data-cleaning`
- **功能**: 根据指令执行智能数据清理
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `instructions`: String - 清理指令

#### 请求示例

```bash
curl -X POST \
  -F "file=@dirty_data.xlsx" \
  -F "instructions=删除空行和空列，标准化日期格式，去除重复记录" \
  http://localhost:8080/api/ai/smart-data-cleaning
```

#### 响应示例

```json
{
  "success": true,
  "aiResponse": "[DELETE_ROW:5]\n[DELETE_ROW:12]\n[APPLY_FORMULA:A2:=DATE(LEFT(C2,4),MID(C2,6,2),RIGHT(C2,2))]\n[SET_CELL:B3:John Smith]",
  "cleaningInstructions": "删除空行和空列，标准化日期格式，去除重复记录",
  "commandResults": [
    {
      "success": true,
      "commandType": "DELETE_ROW",
      "commandParams": "5",
      "message": "Successfully deleted row 5"
    },
    {
      "success": true,
      "commandType": "APPLY_FORMULA",
      "commandParams": "A2:=DATE(LEFT(C2,4),MID(C2,6,2),RIGHT(C2,2))",
      "message": "Successfully applied formula to cell A2"
    }
  ]
}
```

### 7.2 智能数据转换

- **接口**: `POST /api/ai/smart-data-transformation`
- **功能**: 根据指令执行智能数据转换
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `instructions`: String - 转换指令

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  -F "instructions=创建透视表，按部门汇总销售额，添加同比增长率列" \
  http://localhost:8080/api/ai/smart-data-transformation
```

#### 响应示例

```json
{
  "success": true,
  "aiResponse": "[INSERT_COLUMN:4:Growth%]\n[APPLY_FORMULA:D2:=(B2-C2)/C2*100]",
  "transformationInstructions": "创建透视表，按部门汇总销售额，添加同比增长率列",
  "commandResults": [
    {
      "success": true,
      "commandType": "INSERT_COLUMN",
      "commandParams": "4:Growth%",
      "message": "Successfully inserted column at 4"
    },
    {
      "success": true,
      "commandType": "APPLY_FORMULA",
      "commandParams": "D2:=(B2-C2)/C2*100",
      "message": "Successfully applied formula to cell D2"
    }
  ]
}
```

### 7.3 智能数据分析

- **接口**: `POST /api/ai/smart-data-analysis`
- **功能**: 根据指令执行智能数据分析
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `instructions`: String - 分析指令

#### 请求示例

```bash
curl -X POST \
  -F "file=@sales_data.xlsx" \
  -F "instructions=分析销售趋势，识别季节性模式，预测下季度销售额" \
  http://localhost:8080/api/ai/smart-data-analysis
```

#### 响应示例

```json
{
  "success": true,
  "aiResponse": "[INSERT_COLUMN:5:Trend]\n[APPLY_FORMULA:E2:=FORECAST(B2,$C$2:$C$13,$B$2:$B$13)]",
  "analysisInstructions": "分析销售趋势，识别季节性模式，预测下季度销售额",
  "commandResults": [
    {
      "success": true,
      "commandType": "INSERT_COLUMN",
      "commandParams": "5:Trend",
      "message": "Successfully inserted column at 5"
    },
    {
      "success": true,
      "commandType": "APPLY_FORMULA",
      "commandParams": "E2:=FORECAST(B2,$C$2:$C$13,$B$2:$B$13)",
      "message": "Successfully applied formula to cell E2"
    }
  ]
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

### 7.4 智能图表创建

- **接口**: `POST /api/ai/smart-chart-creation`
- **功能**: 根据指令创建智能图表
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `instructions`: String - 图表创建指令

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  -F "instructions=创建销售趋势折线图，包含预算线对比，添加数据标签" \
  http://localhost:8080/api/ai/smart-chart-creation
```

#### 响应示例

```json
{
  "success": true,
  "aiResponse": "[INSERT_COLUMN:6:ChartHelper]\n[SET_CELL:F1:Sales Chart Data]",
  "chartInstructions": "创建销售趋势折线图，包含预算线对比，添加数据标签",
  "commandResults": [
    {
      "success": true,
      "commandType": "INSERT_COLUMN",
      "commandParams": "6:ChartHelper",
      "message": "Successfully inserted column at 6"
    },
    {
      "success": true,
      "commandType": "SET_CELL",
      "commandParams": "F1:Sales Chart Data",
      "message": "Successfully set cell F1"
    }
  ]
}
```

### 7.5 智能数据验证

- **接口**: `POST /api/ai/smart-data-validation`
- **功能**: 根据指令执行智能数据验证
- **请求格式**: `multipart/form-data`
- **请求参数**:
  - `file`: MultipartFile - Excel文件
  - `instructions`: String - 验证指令

#### 请求示例

```bash
curl -X POST \
  -F "file=@data.xlsx" \
  -F "instructions=验证数据完整性，检查数值范围，标记异常值" \
  http://localhost:8080/api/ai/smart-data-validation
```

#### 响应示例

```json
{
  "success": true,
  "aiResponse": "[INSERT_COLUMN:7:Validation]\n[APPLY_FORMULA:G2:=IF(OR(B2<0,B2>100000),\"异常\",\"正常\")]",
  "validationInstructions": "验证数据完整性，检查数值范围，标记异常值",
  "commandResults": [
    {
      "success": true,
      "commandType": "INSERT_COLUMN",
      "commandParams": "7:Validation",
      "message": "Successfully inserted column at 7"
    },
    {
      "success": true,
      "commandType": "APPLY_FORMULA",
      "commandParams": "G2:=IF(OR(B2<0,B2>100000),\"异常\",\"正常\")",
      "message": "Successfully applied formula to cell G2"
    }
  ]
}
```

## 8. 系统接口

### 8.1 服务状态

- **接口**: `GET /api/status`
- **功能**: 获取API服务配置状态
- **响应示例**:

```json
{
  "hasApiKey": true,
  "apiConfigured": true,
  "status": "available"
}
```

#### 错误示例（404 Not Found）

```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "error": "Requested resource not found or endpoint unavailable"
}
```

### 8.3 分析中心系统设置

- **接口**: `GET /api/analysis-center/settings`
- **功能**: 获取前端显示的系统级设置（包含当前 API endpoint 与 server port）
- **响应格式**: `application/json`
- **响应示例**:

```json
{
  "success": true,
  "data": {
    "apiEndpoint": "http://localhost:8081",
    "serverPort": 8081,
    "language": "zh-CN",
    "theme": "dark",
    "autoSave": true,
    "notifications": true
  }
}
```

- **接口**: `POST /api/analysis-center/settings`
- **功能**: 保存系统设置（演示：直接回显提交的配置）
- **请求格式**: `application/json`
- **请求示例**:

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"apiEndpoint":"http://localhost:8081","autoSave":true}' \
  http://localhost:8081/api/analysis-center/settings
```

**说明**: 在开发环境中，前端会先通过 `/api/config` 获取后端建议的 base URL，再调用该设置接口。若需要在前端临时覆盖目标 API，请在 Settings 页面修改 `API Endpoint` 并保存。

### 8.4 健康检查

- **接口**: `GET /api/health`
- **功能**: 检查服务健康状态
- **响应示例**:

```json
{
  "status": "UP",
  "service": "AI Excel Integration"
}
```

## 9. 错误处理和状态码规范

### 9.1 错误响应格式

所有错误响应都使用以下标准格式：

```json
{
  "success": false,
  "error": "错误信息描述"
}
```

### 9.2 常见错误类型

#### 9.2.1 文件相关错误

- **错误码**: 400
- **错误信息**: "Error processing Excel file: [具体错误信息]"
- **描述**: 文件处理失败，可能是文件格式不正确、损坏或无法读取

#### 9.2.2 AI服务相关错误

- **错误码**: 400
- **错误信息**: "Error processing AI request: [具体错误信息]"
- **描述**: AI服务调用失败，可能是API配置问题或网络连接问题

#### 9.2.3 参数验证错误

- **错误码**: 400
- **错误信息**: "Message is required"
- **描述**: 必需参数缺失或为空

#### 9.2.4 内部服务器错误

- **错误码**: 500
- **错误信息**: "Error processing request: [具体错误信息]"
- **描述**: 服务器端发生未预期的错误

### 9.3 HTTP状态码规范

- **200**: 成功响应，操作正常完成
- **400**: 客户端错误，请求参数不正确或缺失
- **404**: 资源未找到
- **500**: 服务器内部错误

### 9.4 错误处理最佳实践

1. **客户端处理**:
   - 始终检查响应中的`success`字段
   - 提供用户友好的错误信息
   - 实现重试机制以处理网络临时故障

2. **常见错误处理**:
   - 上传文件前验证文件格式和大小
   - 检查必需参数是否提供
   - 实现适当的超时机制

3. **调试信息**:
   - 保留API返回的详细错误信息用于调试
   - 记录请求ID以便追踪问题

## 10. 示例和最佳实践

### 10.1 完整的Excel处理工作流示例

以下是一个完整的前端与后端交互示例，实现从上传Excel到AI处理的完整流程：

#### 10.1.1 上传文件并获取数据预览

```javascript
// 上传Excel并获取数据
async function uploadAndPreview(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const response = await fetch('http://localhost:8080/api/excel/get-data', {
      method: 'POST',
      body: formData
    });
    
    const result = await response.json();
    
    if (result.success) {
      // 显示Excel数据预览
      console.log('Excel Data:', result.data);
      return result.data;
    } else {
      throw new Error(result.error || '上传失败');
    }
  } catch (error) {
    console.error('上传文件失败:', error);
    throw error;
  }
}
```

#### 10.1.2 使用AI处理Excel

```javascript
// 使用AI对Excel进行处理
async function processExcelWithAI(file, command) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('command', command);
  
  try {
    const response = await fetch('http://localhost:8080/api/ai/excel-with-ai', {
      method: 'POST',
      body: formData
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log('处理结果:', result);
      return result;
    } else {
      throw new Error(result.error || 'AI处理失败');
    }
  } catch (error) {
    console.error('AI处理失败:', error);
    throw error;
  }
}
```

#### 10.1.3 下载处理后的Excel

```javascript
// 下载处理后的Excel文件
async function downloadProcessedExcel(file, command) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('command', command);
  
  try {
    const response = await fetch('http://localhost:8080/api/ai/excel-with-ai-download', {
      method: 'POST',
      body: formData
    });
    
    if (response.ok) {
      // 获取文件名
      const disposition = response.headers.get('Content-Disposition');
      let filename = 'modified_file.xlsx';
      if (disposition && disposition.indexOf('filename=') !== -1) {
        filename = disposition.split('filename=')[1].replace(/"/g, '');
      }
      
      // 创建下载链接
      const blob = await response.blob();
      const downloadUrl = URL.createObjectURL(blob);
      
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      
      URL.revokeObjectURL(downloadUrl);
    } else {
      throw new Error('下载失败');
    }
  } catch (error) {
    console.error('下载失败:', error);
    throw error;
  }
}
```

#### 10.1.4 复合分析示例

```javascript
// 复合分析示例：客户细分 + 财务分析
async function comprehensiveAnalysis(file) {
  try {
    // 1. 客户细分分析
    const segmentationFormData = new FormData();
    segmentationFormData.append('file', file);
    const segmentationResponse = await fetch('http://localhost:8080/api/analysis/customer-segmentation', {
      method: 'POST',
      body: segmentationFormData
    });
    const segmentationResult = await segmentationResponse.json();
    
    // 2. 财务分析
    const financialFormData = new FormData();
    financialFormData.append('file', file);
    const financialResponse = await fetch('http://localhost:8080/api/analysis/financial-ratios', {
      method: 'POST',
      body: financialFormData
    });
    const financialResult = await financialResponse.json();
    
    return {
      customerSegmentation: segmentationResult,
      financialAnalysis: financialResult
    };
  } catch (error) {
    console.error('综合分析失败:', error);
    throw error;
  }
}
```

### 10.2 前端集成最佳实践

#### 10.2.1 文件上传组件

```javascript
// React组件示例
import React, { useState } from 'react';

function ExcelProcessor() {
  const [file, setFile] = useState(null);
  const [command, setCommand] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleProcess = async () => {
    if (!file || !command) {
      alert('请上传文件并输入命令');
      return;
    }

    setLoading(true);
    try {
      const processResult = await processExcelWithAI(file, command);
      setResult(processResult);
    } catch (error) {
      alert(`处理失败: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <input 
        type="file" 
        accept=".xlsx,.xls,.csv"
        onChange={handleFileChange}
      />
      <textarea 
        placeholder="输入命令，例如：创建一个新列'Sum'并计算A列和B列的和"
        value={command}
        onChange={(e) => setCommand(e.target.value)}
      />
      <button onClick={handleProcess} disabled={loading}>
        {loading ? '处理中...' : '处理Excel'}
      </button>
      {result && (
        <div>
          <h3>处理结果</h3>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}
```

#### 10.2.2 错误处理和重试机制

```javascript
// 带重试机制的API调用
async function fetchWithRetry(url, options, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await fetch(url, options);
      
      if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (!data.success && i < maxRetries - 1) {
        // 非最终重试，则抛出错误触发重试
        throw new Error(data.error || 'API调用失败');
      }
      
      return data;
    } catch (error) {
      if (i === maxRetries - 1) {
        // 最后一次重试也失败了
        throw error;
      }
      
      // 等待一段时间后重试
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}
```

#### 10.2.3 文件类型验证

```javascript
// 文件类型验证工具
function validateExcelFile(file) {
  const allowedTypes = [
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
    'application/vnd.ms-excel', // .xls
    'text/csv', // .csv
    'application/csv'
  ];
  
  const allowedExtensions = ['.xlsx', '.xls', '.csv'];
  
  // 检查MIME类型
  if (!allowedTypes.includes(file.type)) {
    return false;
  }
  
  // 检查文件扩展名
  const fileName = file.name.toLowerCase();
  const hasValidExtension = allowedExtensions.some(ext => fileName.endsWith(ext));
  
  return hasValidExtension;
}

// 使用示例
function handleFileUpload(file) {
  if (!validateExcelFile(file)) {
    alert('请上传.xlsx、.xls或.csv格式的文件');
    return;
  }
  
  // 继续执行上传逻辑
}
```

### 10.3 性能优化建议

1. **大文件处理**: 对于大Excel文件，考虑分页处理或流式处理
2. **缓存机制**: 对于重复的分析请求，实现适当的缓存策略
3. **批量操作**: 将多个相关操作合并为一个API调用
4. **预加载数据**: 提前获取可能需要的数据，减少API调用次数

### 10.4 安全考虑

1. **文件上传安全**: 限制文件大小和类型，防止恶意文件上传
2. **API调用频率限制**: 实现API调用频率限制，防止滥用
3. **输入验证**: 对所有用户输入进行验证和清理
4. **敏感数据处理**: 对包含敏感信息的Excel文件进行适当处理

### 10.5 监控和调试

1. **日志记录**: 实现详细的API调用日志
2. **性能监控**: 监控API响应时间和错误率
3. **错误追踪**: 实现错误追踪和告警机制
4. **使用分析**: 记录API使用情况以优化服务

该API文档提供了一个完整的AI Excel Integration后端服务接口说明，支持从基本的Excel数据处理到高级的AI分析功能，满足企业级应用的需求。
