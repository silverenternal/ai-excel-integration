package com.example.aiexcel.service;

import com.example.aiexcel.dto.AiRequest;
import com.example.aiexcel.dto.AiResponse;
import com.example.aiexcel.service.ai.AiService;
import com.example.aiexcel.service.analysis.CustomerAnalysisService;
import com.example.aiexcel.service.analysis.FinancialAnalysisService;
import com.example.aiexcel.service.excel.ExcelService;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("unused")
@Service
public class AiExcelIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(AiExcelIntegrationService.class);

    @Autowired
    private AiService aiService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private CustomerAnalysisService customerAnalysisService;

    @Autowired
    private FinancialAnalysisService financialAnalysisService;

    @Autowired
    private AiExcelCommandParser aiExcelCommandParser;

    @Autowired
    private OperationHistoryService operationHistoryService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private AiSuggestionService aiSuggestionService;

    @Autowired
    private FileManagerService fileManagerService;

    public Map<String, Object> processExcelWithAI(MultipartFile file, String command) throws IOException {
        logger.info("Starting AI Excel processing for command: {}", command);

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Validate inputs
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                result.put("success", false);
                result.put("error", "File is required and cannot be empty");
                return result;
            }

            if (command == null || command.trim().isEmpty()) {
                logger.error("Command is null or empty");
                result.put("success", false);
                result.put("error", "Command is required and cannot be empty");
                return result;
            }

            logger.debug("Loading Excel file: {}", file.getOriginalFilename());

            // 2. 加载Excel文件
            Workbook workbook = excelService.loadWorkbook(file);
            logger.debug("Excel file loaded successfully");

            // 3. 获取Excel数据
            String excelData = excelService.getExcelDataAsString(workbook);
            logger.debug("Excel data extracted, length: {}", excelData.length());

            // 4. 构建AI请求
            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(Arrays.asList(
                new AiRequest.Message("system",
                    "You are an Excel expert assistant. You can analyze Excel data and provide formulas, operations, or insights. " +
                    "The user will provide Excel data and a command. Respond with the appropriate Excel formula or operation steps. " +
                    "For formulas, include the actual formula syntax. For operations, provide step-by-step instructions. " +
                    "Always be precise and accurate. If the user wants to modify the Excel data, provide specific commands in this format: " +
                    "[SET_CELL:A1:New Value] to set cell A1 to 'New Value', " +
                    "[INSERT_ROW:3:value1,value2,value3] to insert a row at position 3 with these values, " +
                    "[INSERT_COLUMN:2:value1,value2,value3] to insert a column at position 2 with these values, " +
                    "[DELETE_ROW:5] to delete row 5, " +
                    "[DELETE_COLUMN:1] to delete column 1, " +
                    "[APPLY_FORMULA:A1:B1+C1] to apply the formula 'B1+C1' in cell A1. " +
                    "Embed these commands directly in your response when appropriate."),
                new AiRequest.Message("user",
                    "Here is the Excel data:\n\n" + excelData + "\n\n" +
                    "User command: " + command + "\n\n" +
                    "Please provide the appropriate Excel operations to fulfill this request using the command format mentioned in the system message.")
            ));

            logger.debug("Sending request to AI service");

            // 5. 调用AI服务
            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();
            logger.debug("AI response received, length: {}", aiResponseContent.length());

            // 保存操作前的工作簿副本用于历史记录
            Workbook workbookBefore = cloneWorkbook(workbook);

            // 6. 解析并执行AI返回的Excel操作命令
            List<AiExcelCommandParser.CommandResult> commandResults = aiExcelCommandParser.parseAndExecuteCommands(workbook, aiResponseContent);
            logger.debug("AI commands executed, {} commands processed", commandResults.size());

            // 6.5. 计算工作簿中所有公式，将结果替换公式
            excelService.evaluateAllFormulasInWorkbook(workbook);
            logger.debug("All formulas in workbook have been evaluated and replaced with results");

            // 7. 保存修改后的Excel文件
            String outputFileName = "modified_" + file.getOriginalFilename();
            logger.debug("Saving modified workbook to: {}", outputFileName);
            excelService.saveWorkbook(workbook, outputFileName);

            // 8. 生成文件ID（如果之前没有生成）
            String fileId = result.containsKey("fileId") ? (String) result.get("fileId") :
                           file.getOriginalFilename() + "_" + System.currentTimeMillis();

            // 9. 记录操作历史
            String parameters = "command=" + command + "; aiResponse=" + aiResponseContent;
            operationHistoryService.recordOperationWithWorkbooks(fileId, "AI_PROCESSING", parameters, workbookBefore, workbook);
            logger.debug("Operation history recorded for file ID: {}", fileId);

            // 10. 创建新版本
            versionService.createVersionFromWorkbook(fileId, workbook, "AI processing: " + command, "system", outputFileName);
            logger.debug("Version created for file ID: {}", fileId);

            // 11. 构建结果
            result.put("excelDataPreview", excelData.substring(0, Math.min(excelData.length(), 500)) + "...");
            result.put("aiResponse", aiResponseContent);
            result.put("command", command);
            result.put("success", true);
            result.put("commandResults", commandResults);
            result.put("outputFile", outputFileName);
            result.put("fileId", fileId); // 添加文件ID以供后续操作使用

            logger.info("AI Excel processing completed successfully for command: {}", command);

        } catch (IOException e) {
            logger.error("IO error during AI Excel processing: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "IO error occurred: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during AI Excel processing: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Error occurred: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> generateExcelFormula(String excelContext, String goal) {
        logger.info("Generating Excel formula for goal: {}", goal);

        Map<String, Object> result = new HashMap<>();

        try {
            // Validate inputs
            if (excelContext == null || excelContext.trim().isEmpty()) {
                logger.error("Excel context is null or empty");
                result.put("success", false);
                result.put("error", "Excel context is required");
                return result;
            }

            if (goal == null || goal.trim().isEmpty()) {
                logger.error("Goal is null or empty");
                result.put("success", false);
                result.put("error", "Goal is required");
                return result;
            }

            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(Arrays.asList(
                new AiRequest.Message("system",
                    "You are an Excel formula expert. When given a context and goal, provide the specific Excel formula needed, " +
                    "including function names, parameters, and any necessary explanations. " +
                    "If multiple formulas could work, suggest the most appropriate one and explain why."),
                new AiRequest.Message("user",
                    "Excel context: " + excelContext + "\n" +
                    "Goal: " + goal + "\n\n" +
                    "Please provide the appropriate Excel formula to achieve this goal, with an explanation.")
            ));

            logger.debug("Sending formula request to AI service");

            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();

            result.put("formula", aiResponseContent);
            result.put("context", excelContext);
            result.put("goal", goal);
            result.put("success", true);

            logger.info("Excel formula generation completed successfully");

        } catch (Exception e) {
            logger.error("Error during Excel formula generation: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Error occurred during formula generation: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> analyzeExcelData(MultipartFile file, String analysisRequest) throws IOException {
        logger.info("Starting Excel data analysis for request: {}", analysisRequest);

        Map<String, Object> result = new HashMap<>();

        try {
            // Validate inputs
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty for analysis");
                result.put("success", false);
                result.put("error", "File is required for analysis");
                return result;
            }

            if (analysisRequest == null || analysisRequest.trim().isEmpty()) {
                logger.error("Analysis request is null or empty");
                result.put("success", false);
                result.put("error", "Analysis request is required");
                return result;
            }

            // 加载Excel文件
            logger.debug("Loading Excel file for analysis: {}", file.getOriginalFilename());
            Workbook workbook = excelService.loadWorkbook(file);

            // 获取Excel数据
            String excelData = excelService.getExcelDataAsString(workbook);
            logger.debug("Excel data extracted for analysis, length: {}", excelData.length());

            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(Arrays.asList(
                new AiRequest.Message("system",
                    "You are an Excel data analysis expert. Analyze the provided data and respond to the user's specific request. " +
                    "Provide insights, summaries, trends, or any other requested analysis based on the data. " +
                    "If the user requests specific calculations or formulas, provide them with the exact syntax."),
                new AiRequest.Message("user",
                    "Here is the Excel data:\n\n" + excelData + "\n\n" +
                    "Analysis request: " + analysisRequest + "\n\n" +
                    "Please perform the requested analysis on this data.")
            ));

            logger.debug("Sending analysis request to AI service");

            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();

            result.put("analysis", aiResponseContent);
            result.put("excelDataPreview", excelData.substring(0, Math.min(excelData.length(), 500)) + "...");
            result.put("analysisRequest", analysisRequest);
            result.put("success", true);

            logger.info("Excel data analysis completed successfully");

        } catch (IOException e) {
            logger.error("IO error during Excel data analysis: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "IO error occurred during analysis: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during Excel data analysis: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Error occurred during analysis: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> suggestChartForData(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 加载Excel文件
        Workbook workbook = excelService.loadWorkbook(file);

        // 获取Excel数据
        String excelData = excelService.getExcelDataAsString(workbook);

        AiRequest aiRequest = new AiRequest();
        aiRequest.setMessages(Arrays.asList(
            new AiRequest.Message("system",
                "You are an Excel chart recommendation expert. Based on the provided data, suggest the most appropriate " +
                "chart types for visualization. Consider the data types, relationships, and what insights would be most valuable " +
                "to visualize. Provide specific chart recommendations and explain why each would be appropriate."),
            new AiRequest.Message("user",
                "Here is the Excel data:\n\n" + excelData + "\n\n" +
                "Based on this data, what chart types would you recommend for visualization? " +
                "Please explain why each chart type would be appropriate for this data.")
        ));

        AiResponse aiResponse = aiService.generateResponse(aiRequest);

        result.put("chartSuggestions", aiResponse.getChoices()[0].getMessage().getContent());
        result.put("excelDataPreview", excelData.substring(0, Math.min(excelData.length(), 500)) + "...");
        result.put("success", true);

        return result;
    }

    public Object[][] getExcelDataAsArray(MultipartFile file) throws IOException {
        // 加载Excel文件
        Workbook workbook = excelService.loadWorkbook(file);

        // 获取Excel数据为数组
        return excelService.getExcelDataAsArray(workbook);
    }

    public Map<String, Object> createChartForData(MultipartFile file, String chartType, String targetColumn) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 加载Excel文件
        Workbook workbook = excelService.loadWorkbook(file);

        // 获取Excel数据
        String excelData = excelService.getExcelDataAsString(workbook);

        AiRequest aiRequest = new AiRequest();
        aiRequest.setMessages(Arrays.asList(
            new AiRequest.Message("system",
                "You are an Excel chart expert. Based on the provided data and target column, " +
                "suggest the appropriate chart creation process and formulas. " +
                "Provide step-by-step instructions for creating the requested chart type in Excel."),
            new AiRequest.Message("user",
                "Here is the Excel data:\n\n" + excelData + "\n\n" +
                "Chart type requested: " + chartType + "\n" +
                "Target column: " + targetColumn + "\n\n" +
                "Please provide step-by-step instructions for creating this chart in Excel, " +
                "including how to select data, insert chart, and customize settings.")
        ));

        AiResponse aiResponse = aiService.generateResponse(aiRequest);

        result.put("chartInstructions", aiResponse.getChoices()[0].getMessage().getContent());
        result.put("chartType", chartType);
        result.put("targetColumn", targetColumn);
        result.put("success", true);

        return result;
    }

    public Map<String, Object> sortExcelData(MultipartFile file, String sortColumn, String sortOrder) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 加载Excel文件
        Workbook workbook = excelService.loadWorkbook(file);

        // 获取Excel数据
        String excelData = excelService.getExcelDataAsString(workbook);

        AiRequest aiRequest = new AiRequest();
        aiRequest.setMessages(Arrays.asList(
            new AiRequest.Message("system",
                "You are an Excel sorting expert. Provide exact instructions for sorting data " +
                "in the specified column with the given sort order. Include formulas or operations " +
                "to apply the sorting in Excel."),
            new AiRequest.Message("user",
                "Here is the Excel data:\n\n" + excelData + "\n\n" +
                "Sort column: " + sortColumn + "\n" +
                "Sort order: " + sortOrder + "\n\n" +
                "Please provide step-by-step instructions for sorting this data in Excel, " +
                "including any Excel formulas or operations needed to accomplish this.")
        ));

        AiResponse aiResponse = aiService.generateResponse(aiRequest);

        result.put("sortInstructions", aiResponse.getChoices()[0].getMessage().getContent());
        result.put("sortColumn", sortColumn);
        result.put("sortOrder", sortOrder);
        result.put("success", true);

        return result;
    }

    public Map<String, Object> filterExcelData(MultipartFile file, String filterColumn, String filterCondition, String filterValue) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 加载Excel文件
        Workbook workbook = excelService.loadWorkbook(file);

        // 获取Excel数据
        String excelData = excelService.getExcelDataAsString(workbook);

        AiRequest aiRequest = new AiRequest();
        aiRequest.setMessages(Arrays.asList(
            new AiRequest.Message("system",
                "You are an Excel filtering expert. Provide exact instructions for filtering data " +
                "based on the specified column, condition, and value. Include formulas or operations " +
                "to apply the filter in Excel."),
            new AiRequest.Message("user",
                "Here is the Excel data:\n\n" + excelData + "\n\n" +
                "Filter column: " + filterColumn + "\n" +
                "Filter condition: " + filterCondition + "\n" +
                "Filter value: " + filterValue + "\n\n" +
                "Please provide step-by-step instructions for filtering this data in Excel, " +
                "including any Excel formulas or operations needed to accomplish this.")
        ));

        AiResponse aiResponse = aiService.generateResponse(aiRequest);

        result.put("filterInstructions", aiResponse.getChoices()[0].getMessage().getContent());
        result.put("filterColumn", filterColumn);
        result.put("filterCondition", filterCondition);
        result.put("filterValue", filterValue);
        result.put("success", true);

        return result;
    }

    public String chatWithAI(String userMessage) {
        // 检查用户消息是否涉及表格操作
        String[] tableOperationKeywords = {"修改", "设置", "插入", "添加", "删除", "创建", "更新", "替换", "填充", "复制", "粘贴", "移动", "应用公式", "计算", "求和", "平均", "筛选", "排序", "格式化", "cell", "row", "column", "A1", "B2", "C3", "formula"};

        boolean requiresTableCommand = false;
        for (String keyword : tableOperationKeywords) {
            if (userMessage.toLowerCase().contains(keyword.toLowerCase())) {
                requiresTableCommand = true;
                break;
            }
        }

        String systemMessage = "You are an AI assistant for an Excel integration tool. Your purpose is to help users understand the product features " +
                "and capabilities. The tool can analyze Excel data with AI, generate Excel formulas, create charts, sort and filter data, " +
                "and provide insights. Answer user questions about these features and how they can help with Excel tasks. " +
                "Be helpful, informative, and provide specific examples where relevant. " +
                "If the user asks about technical issues, provide general guidance but note that for actual Excel operations, " +
                "they need to upload a file and use the appropriate tool.";

        if (requiresTableCommand) {
            // 如果需要表格操作，添加指令格式说明
            systemMessage += " If the user requests Excel operations, please provide them in a specific format that can be parsed by the system:\n" +
                    "[SET_CELL:A1:New Value] - To set cell A1 to 'New Value'\n" +
                    "[INSERT_ROW:3:value1,value2,value3] - To insert a row at position 3 with these values\n" +
                    "[INSERT_COLUMN:2:value1,value2,value3] - To insert a column at position 2 with these values\n" +
                    "[DELETE_ROW:5] - To delete row 5\n" +
                    "[DELETE_COLUMN:1] - To delete column 1\n" +
                    "[APPLY_FORMULA:A1:B1+C1] - To apply the formula 'B1+C1' in cell A1\n" +
                    "Embed these commands directly in your response when appropriate.";
        }

        AiRequest aiRequest = new AiRequest();
        aiRequest.setMessages(Arrays.asList(
            new AiRequest.Message("system", systemMessage),
            new AiRequest.Message("user", userMessage)
        ));

        AiResponse aiResponse = aiService.generateResponse(aiRequest);
        return aiResponse.getChoices()[0].getMessage().getContent();
    }

    public Map<String, Object> performRFMAnalysis(MultipartFile file) throws IOException {
        return customerAnalysisService.performRFMAnalysis(file);
    }

    public Map<String, Object> calculateCustomerLifetimeValue(MultipartFile file) throws IOException {
        return customerAnalysisService.calculateCustomerLifetimeValue(file);
    }

    public Map<String, Object> segmentCustomers(MultipartFile file) throws IOException {
        return customerAnalysisService.segmentCustomers(file);
    }

    public Map<String, Object> predictChurnRisk(MultipartFile file) throws IOException {
        return customerAnalysisService.predictChurnRisk(file);
    }

    public Map<String, Object> calculateCACvsCLV(MultipartFile file) throws IOException {
        return customerAnalysisService.calculateCACvsCLV(file);
    }

    public Map<String, Object> analyzeCustomerCohorts(MultipartFile file) throws IOException {
        return customerAnalysisService.analyzeCustomerCohorts(file);
    }

    public Map<String, Object> analyzeFinancialStatements(MultipartFile file, String analysisType) throws IOException {
        return financialAnalysisService.analyzeFinancialStatements(file, analysisType);
    }

    public Map<String, Object> calculateFinancialRatios(MultipartFile file) throws IOException {
        return financialAnalysisService.calculateFinancialRatios(file);
    }

    public Map<String, Object> analyzeProfitability(MultipartFile file) throws IOException {
        return financialAnalysisService.analyzeProfitability(file);
    }

    public Map<String, Object> analyzeCashFlow(MultipartFile file) throws IOException {
        return financialAnalysisService.analyzeCashFlow(file);
    }

    public Map<String, Object> compareBudgetVsActual(MultipartFile file) throws IOException {
        return financialAnalysisService.compareBudgetVsActual(file);
    }

    /**
     * 应用AI命令修改Excel并返回工作簿对象
     */
    public org.apache.poi.ss.usermodel.Workbook getExcelWorkbookWithAIChanges(MultipartFile file, String command) throws IOException {
        logger.info("Getting Excel workbook with AI changes for command: {}", command);

        try {
            // 1. Validate inputs
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                throw new IOException("File is required and cannot be empty");
            }

            if (command == null || command.trim().isEmpty()) {
                logger.error("Command is null or empty");
                throw new IOException("Command is required and cannot be empty");
            }

            logger.debug("Loading Excel file: {}", file.getOriginalFilename());

            // 2. 加载Excel文件
            Workbook workbook = excelService.loadWorkbook(file);
            logger.debug("Excel file loaded successfully");

            // 3. 获取Excel数据
            String excelData = excelService.getExcelDataAsString(workbook);
            logger.debug("Excel data extracted, length: {}", excelData.length());

            // 4. 构建AI请求
            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(Arrays.asList(
                new AiRequest.Message("system",
                    "You are an Excel expert assistant. You can analyze Excel data and provide formulas, operations, or insights. " +
                    "The user will provide Excel data and a command. Respond with the appropriate Excel formula or operation steps. " +
                    "For formulas, include the actual formula syntax. For operations, provide step-by-step instructions. " +
                    "Always be precise and accurate. If the user wants to modify the Excel data, provide specific commands in this format: " +
                    "[SET_CELL:A1:New Value] to set cell A1 to 'New Value', " +
                    "[INSERT_ROW:3:value1,value2,value3] to insert a row at position 3 with these values, " +
                    "[INSERT_COLUMN:2:value1,value2,value3] to insert a column at position 2 with these values, " +
                    "[DELETE_ROW:5] to delete row 5, " +
                    "[DELETE_COLUMN:1] to delete column 1, " +
                    "[APPLY_FORMULA:A1:B1+C1] to apply the formula 'B1+C1' in cell A1. " +
                    "Embed these commands directly in your response when appropriate."),
                new AiRequest.Message("user",
                    "Here is the Excel data:\n\n" + excelData + "\n\n" +
                    "User command: " + command + "\n\n" +
                    "Please provide the appropriate Excel operations to fulfill this request using the command format mentioned in the system message.")
            ));

            logger.debug("Sending request to AI service");

            // 5. 调用AI服务
            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();
            logger.debug("AI response received, length: {}", aiResponseContent.length());

            // 6. 解析并执行AI返回的Excel操作命令
            List<AiExcelCommandParser.CommandResult> commandResults = aiExcelCommandParser.parseAndExecuteCommands(workbook, aiResponseContent);
            logger.debug("AI commands executed, {} commands processed", commandResults.size());

            // 6.5. 计算工作簿中所有公式，将结果替换公式
            excelService.evaluateAllFormulasInWorkbook(workbook);
            logger.debug("All formulas in workbook have been evaluated and replaced with results");

            logger.info("Excel workbook with AI changes generated successfully for command: {}", command);

            // 7. 返回修改后的工作簿
            return workbook;

        } catch (IOException e) {
            logger.error("IO error getting Excel workbook with AI changes: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error getting Excel workbook with AI changes: {}", e.getMessage(), e);
            throw new IOException("Error occurred during AI processing: " + e.getMessage(), e);
        }
    }

    /**
     * 将工作簿转换为字节数组
     */
    public byte[] getExcelAsBytes(Workbook workbook, String originalFilename) throws IOException {
        return excelService.getWorkbookAsBytes(workbook);
    }

    /**
     * 获取Excel文件的表头
     * @param file Excel文件
     * @return 表头数组
     */
    public String[] getExcelHeaders(MultipartFile file) throws IOException {
        // 加载Excel文件
        Workbook workbook = excelService.loadWorkbook(file);

        // 通过ExcelService获取表头
        return excelService.getExcelHeaders(workbook);
    }

    /**
     * 克隆工作簿
     * @param original 原始工作簿
     * @return 克隆的工作簿
     */
    private Workbook cloneWorkbook(Workbook original) throws IOException {
        byte[] workbookBytes = excelService.getWorkbookAsBytes(original);
        return excelService.loadWorkbook(new java.io.ByteArrayInputStream(workbookBytes));
    }
}