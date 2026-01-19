package com.example.aiexcel.controller;

import com.example.aiexcel.service.AiAdvancedOperationsService;
import com.example.aiexcel.service.AiExcelIntegrationService;
import com.example.aiexcel.service.ai.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aiexcel.config.EnvFileReader;
import java.util.Properties;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AiExcelController {

    @Autowired
    private AiExcelIntegrationService aiExcelIntegrationService;

    @Autowired
    private AiAdvancedOperationsService aiAdvancedOperationsService;

    @Autowired
    private AiService aiService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
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
    public ResponseEntity<Map<String, Object>> getApiStatus() {
        // 使用AI服务的连接测试方法来检测API配置状态
        boolean apiConfigured = aiService.testConnection();

        // 优先从真实环境变量读取 QWEN_API_KEY
        String key = System.getenv("QWEN_API_KEY");

        // 若环境变量不存在或为空，尝试从项目根目录的 .env 文件读取
        if (key == null || key.isEmpty()) {
            try {
                Properties props = EnvFileReader.loadEnvFile();
                if (props != null) {
                    String fromFile = props.getProperty("QWEN_API_KEY");
                    if (fromFile != null && !fromFile.isEmpty()) {
                        key = fromFile;
                    }
                }
            } catch (Exception ignored) {
                // 忽略读取失败
            }
        }

        boolean hasApiKey = key != null && !key.isEmpty();

        Map<String, Object> response = Map.of(
            "hasApiKey", hasApiKey,
            "apiConfigured", apiConfigured,
            "status", "available"
        );

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