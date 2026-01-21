package com.example.aiexcel.controller;

import com.example.aiexcel.service.AiAdvancedOperationsService;
import com.example.aiexcel.service.AiExcelIntegrationService;
import com.example.aiexcel.service.ai.AiService;
import com.example.aiexcel.config.EnvFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AiExcelController {

    private static final Logger logger = LoggerFactory.getLogger(AiExcelController.class);

    @Autowired
    private AiExcelIntegrationService aiExcelIntegrationService;

    @Autowired
    private AiAdvancedOperationsService aiAdvancedOperationsService;

    @Autowired
    private AiService aiService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        logger.info("/api/upload called, file={}", file == null ? "<none>" : file.getOriginalFilename());
        try {
            // 上传功能将在集成服务中实现
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "File uploaded successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/ai/excel-with-ai")
    public ResponseEntity<Map<String, Object>> processExcelWithAI(
            @RequestParam("file") MultipartFile file,
            @RequestParam("command") String command) {
        logger.info("/api/ai/excel-with-ai called, file={}, command={}", file == null ? "<none>" : file.getOriginalFilename(), command);
        try {
            Map<String, Object> result = aiExcelIntegrationService.processExcelWithAI(file, command);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing AI request: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/ai/excel-with-ai-download", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> processExcelWithAIAndDownload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("command") String command) {
        logger.info("/api/ai/excel-with-ai-download called, file={}, command={}", file == null ? "<none>" : file.getOriginalFilename(), command);
        try {
            // 重新实现，直接在内存中处理而不保存到文件
            org.apache.poi.ss.usermodel.Workbook workbook = aiExcelIntegrationService.getExcelWorkbookWithAIChanges(file, command);

            if (workbook != null) {
                // 将工作簿转换为字节数组
                byte[] fileContent = aiExcelIntegrationService.getExcelAsBytes(workbook, file.getOriginalFilename());

                String outputFileName = "modified_" + file.getOriginalFilename();
                return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + outputFileName + "\"")
                    .body(fileContent);
            } else {
                // 如果处理失败，返回错误
                return ResponseEntity.badRequest().body(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/ai/generate-formula")
    public ResponseEntity<Map<String, Object>> generateFormula(@RequestBody Map<String, String> request) {
        logger.info("/api/ai/generate-formula called, context={}, goal={}", request == null ? "<none>" : request.get("context"), request == null ? "<none>" : request.get("goal"));
        try {
            String excelContext = request.get("context");
            String goal = request.get("goal");

            Map<String, Object> result = aiExcelIntegrationService.generateExcelFormula(excelContext, goal);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error generating formula: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/ai/excel-analyze")
    public ResponseEntity<Map<String, Object>> analyzeExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("analysisRequest") String analysisRequest) {
        logger.info("/api/ai/excel-analyze called, file={}, analysisRequest={}", file == null ? "<none>" : file.getOriginalFilename(), analysisRequest);
        try {
            Map<String, Object> result = aiExcelIntegrationService.analyzeExcelData(file, analysisRequest);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/ai/suggest-charts")
    public ResponseEntity<Map<String, Object>> suggestCharts(@RequestParam("file") MultipartFile file) {
        logger.info("/api/ai/suggest-charts called, file={}", file == null ? "<none>" : file.getOriginalFilename());
        try {
            Map<String, Object> result = aiExcelIntegrationService.suggestChartForData(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing chart suggestion: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/excel/get-data")
    public ResponseEntity<Map<String, Object>> getExcelData(@RequestParam("file") MultipartFile file) {
        logger.info("/api/excel/get-data called, file={}", file == null ? "<none>" : file.getOriginalFilename());
        try {
            Object[][] excelData = aiExcelIntegrationService.getExcelDataAsArray(file);
            Map<String, Object> response = Map.of(
                "success", true,
                "data", excelData
            );
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error reading Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing request: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/excel/create-chart")
    public ResponseEntity<Map<String, Object>> createChart(@RequestParam("file") MultipartFile file,
                                                           @RequestParam("chartType") String chartType,
                                                           @RequestParam("targetColumn") String targetColumn) {
        logger.info("/api/excel/create-chart called, file={}, chartType={}, targetColumn={}", file == null ? "<none>" : file.getOriginalFilename(), chartType, targetColumn);
        try {
            Map<String, Object> result = aiExcelIntegrationService.createChartForData(file, chartType, targetColumn);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing chart request: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/excel/sort-data")
    public ResponseEntity<Map<String, Object>> sortData(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("sortColumn") String sortColumn,
                                                        @RequestParam("sortOrder") String sortOrder) {
        logger.info("/api/excel/sort-data called, file={}, sortColumn={}, sortOrder={}", file == null ? "<none>" : file.getOriginalFilename(), sortColumn, sortOrder);
        try {
            Map<String, Object> result = aiExcelIntegrationService.sortExcelData(file, sortColumn, sortOrder);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing sort request: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/excel/filter-data")
    public ResponseEntity<Map<String, Object>> filterData(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("filterColumn") String filterColumn,
                                                          @RequestParam("filterCondition") String filterCondition,
                                                          @RequestParam("filterValue") String filterValue) {
        logger.info("/api/excel/filter-data called, file={}, filterColumn={}, filterCondition={}, filterValue={}", file == null ? "<none>" : file.getOriginalFilename(), filterColumn, filterCondition, filterValue);
        try {
            Map<String, Object> result = aiExcelIntegrationService.filterExcelData(file, filterColumn, filterCondition, filterValue);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing filter request: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getApiStatus(@RequestParam(value = "reveal", required = false, defaultValue = "false") boolean reveal) {
        // 使用统一的 EnvFile 工具读取运行时配置
        String foundKey = com.example.aiexcel.config.EnvFile.getApiKey();
        boolean hasApiKey = foundKey != null && !foundKey.isEmpty();
        Integer connectionStatus = aiService.testConnection();
        boolean apiConfigured = connectionStatus != null && connectionStatus == 200;

        String resolvedBase = com.example.aiexcel.config.EnvFile.getBaseUrl();
        boolean isDev = com.example.aiexcel.config.EnvFile.isDev();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("hasApiKey", hasApiKey);
        response.put("apiConfigured", apiConfigured);
        response.put("status", "available");

        if (isDev) {
            java.util.Map<String, Object> diag = new java.util.HashMap<>();
            String source = hasApiKey ? "env/system/.env (via EnvFile)" : "none";
            diag.put("keySource", source);
            diag.put("keyMasked", EnvFile.mask(foundKey));
            if (reveal) diag.put("keyPlain", foundKey);
            diag.put("keyFormatOk", hasApiKey && foundKey.trim().startsWith("sk-"));
            diag.put("baseUrlDetected", resolvedBase);

            // 仅在出现 401 （未经授权）时才显示后续建议；其他情况下不显示建议
            if (connectionStatus != null && connectionStatus == 401) {
                java.util.List<String> suggestions = new java.util.ArrayList<>();
                if (!hasApiKey) {
                    suggestions.add("未检测到 API Key：请通过环境变量 DASHSCOPE_API_KEY 或 QWEN_API_KEY 设置密钥，或在 .env 中配置（仅用于本地调试）。");
                } else {
                    if (!foundKey.trim().startsWith("sk-")) {
                        suggestions.add("检测到的密钥格式异常：DashScope 的 API Key 通常以 'sk-' 开头，请确认证书不是其他厂商的密钥。");
                    }
                    suggestions.add("出现 401，请确认所用 API Key 未被删除或过期，并从 DashScope 控制台重新生成。");
                }
                suggestions.add("确认 Base URL 与 API Key 所属地域匹配：\n - 中国（北京）：https://dashscope.aliyuncs.com/compatible-mode/v1\n - 国际（新加坡）：https://dashscope-intl.aliyuncs.com/compatible-mode/v1");
                suggestions.add("不要将密钥写入生产代码或长时间放在 JVM 启动参数中（进程列表可能泄露）。");

                diag.put("suggestions", suggestions);
            }
            response.put("diagnosis", diag);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/ai/chat")
    public ResponseEntity<Map<String, Object>> chatWithAI(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Message is required"
            );
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 使用AI服务回复用户消息
            String aiResponse = aiExcelIntegrationService.chatWithAI(userMessage);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", aiResponse
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing AI chat: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "AI Excel Integration"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ai/chat-stream")
    public ResponseEntity<Map<String, Object>> chatWithAIStream(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Message is required"
            );
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 使用AI服务回复用户消息
            String aiResponse = aiExcelIntegrationService.chatWithAI(userMessage);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", aiResponse
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing AI chat: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping(value = "/ai/chat-sse", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chatWithAISSE(@RequestParam String message) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(Long.MAX_VALUE); // 设置长时间连接

        // 在单独的线程中处理响应
        Runnable task = () -> {
            try {
                if (message == null || message.trim().isEmpty()) {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name("error")
                            .data("Message is required"));
                    emitter.complete();
                    return;
                }

                // 使用AI服务回复用户消息
                String aiResponse = aiExcelIntegrationService.chatWithAI(message);

                // 先发送一个开始事件
                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                        .name("start")
                        .data(""));

                // 将AI响应按单词分割模拟流式输出
                String[] words = aiResponse.split("(?<=\\S)\\s+");

                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (!word.trim().isEmpty()) {
                        // 添加空格（除了第一个词）
                        if (i > 0) {
                            word = " " + word;
                        }

                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                                .name("chunk")
                                .data(word));
                        Thread.sleep(50); // 模拟打字延迟
                    }
                }

                // 发送完成事件
                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                        .name("done")
                        .data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name("error")
                            .data("Error: " + e.getMessage()));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        };

        // 异步执行任务
        new Thread(task).start();

        return emitter;
    }

    // 客户分析API端点
    @PostMapping("/analysis/rfm")
    public ResponseEntity<Map<String, Object>> performRFMAnalysis(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.performRFMAnalysis(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing RFM analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/clv")
    public ResponseEntity<Map<String, Object>> calculateCustomerLifetimeValue(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.calculateCustomerLifetimeValue(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing CLV analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/customer-segmentation")
    public ResponseEntity<Map<String, Object>> segmentCustomers(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.segmentCustomers(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing customer segmentation: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/churn-prediction")
    public ResponseEntity<Map<String, Object>> predictChurnRisk(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.predictChurnRisk(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing churn prediction: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/cac-clv")
    public ResponseEntity<Map<String, Object>> calculateCACvsCLV(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.calculateCACvsCLV(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing CAC vs CLV analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/cohort")
    public ResponseEntity<Map<String, Object>> analyzeCustomerCohorts(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.analyzeCustomerCohorts(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing cohort analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 财务分析API端点
    @PostMapping("/analysis/financial")
    public ResponseEntity<Map<String, Object>> analyzeFinancialStatements(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "comprehensive") String analysisType) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.analyzeFinancialStatements(file, analysisType);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing financial analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/financial-ratios")
    public ResponseEntity<Map<String, Object>> calculateFinancialRatios(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.calculateFinancialRatios(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing financial ratios: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/profitability")
    public ResponseEntity<Map<String, Object>> analyzeProfitability(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.analyzeProfitability(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing profitability analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/cash-flow")
    public ResponseEntity<Map<String, Object>> analyzeCashFlow(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.analyzeCashFlow(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing cash flow analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/analysis/budget-actual")
    public ResponseEntity<Map<String, Object>> compareBudgetVsActual(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = aiExcelIntegrationService.compareBudgetVsActual(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing budget vs actual analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 高级AI操作端点
    @PostMapping("/ai/smart-data-cleaning")
    public ResponseEntity<Map<String, Object>> performSmartDataCleaning(
            @RequestParam("file") MultipartFile file,
            @RequestParam("instructions") String instructions) {
        try {
            Map<String, Object> result = aiAdvancedOperationsService.performSmartDataCleaning(file, instructions);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing smart data cleaning: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/ai/smart-data-transformation")
    public ResponseEntity<Map<String, Object>> performSmartDataTransformation(
            @RequestParam("file") MultipartFile file,
            @RequestParam("instructions") String instructions) {
        try {
            Map<String, Object> result = aiAdvancedOperationsService.performSmartDataTransformation(file, instructions);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing smart data transformation: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/ai/smart-data-analysis")
    public ResponseEntity<Map<String, Object>> performSmartDataAnalysis(
            @RequestParam("file") MultipartFile file,
            @RequestParam("instructions") String instructions) {
        try {
            Map<String, Object> result = aiAdvancedOperationsService.performSmartDataAnalysis(file, instructions);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing smart data analysis: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/ai/smart-chart-creation")
    public ResponseEntity<Map<String, Object>> performSmartChartCreation(
            @RequestParam("file") MultipartFile file,
            @RequestParam("instructions") String instructions) {
        try {
            Map<String, Object> result = aiAdvancedOperationsService.performSmartChartCreation(file, instructions);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing smart chart creation: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/ai/smart-data-validation")
    public ResponseEntity<Map<String, Object>> performSmartDataValidation(
            @RequestParam("file") MultipartFile file,
            @RequestParam("instructions") String instructions) {
        try {
            Map<String, Object> result = aiAdvancedOperationsService.performSmartDataValidation(file, instructions);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing Excel file: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error processing smart data validation: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}