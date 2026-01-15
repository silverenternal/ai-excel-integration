package com.example.aiexcel.service;

import com.example.aiexcel.dto.AiRequest;
import com.example.aiexcel.dto.AiResponse;
import com.example.aiexcel.service.ai.AiService;
import com.example.aiexcel.service.excel.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * AI智能建议服务
 * 基于Excel数据提供智能分析和建议
 */
@Service
public class AiSuggestionService {

    @Autowired
    private AiService aiService;

    @Autowired
    private ExcelService excelService;

    private static final Logger logger = LoggerFactory.getLogger(AiSuggestionService.class);

    /**
     * 为Excel数据提供智能建议
     */
    public Map<String, Object> getSuggestions(MultipartFile file, String context) {
        logger.info("Getting AI suggestions for file: {}", file.getOriginalFilename());

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证输入
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                result.put("success", false);
                result.put("error", "File is required");
                return result;
            }

            // 加载Excel数据
            org.apache.poi.ss.usermodel.Workbook workbook = excelService.loadWorkbook(file);
            String excelData = excelService.getExcelDataAsString(workbook);

            // 构建AI请求
            List<AiRequest.Message> messages = new ArrayList<>();
            
            messages.add(new AiRequest.Message("system", 
                "You are an Excel data analysis expert. Analyze the provided Excel data and provide intelligent suggestions " +
                "for better data management, formatting, analysis, or visualization. " +
                "Consider common Excel best practices, data cleaning techniques, chart suggestions, " +
                "formula recommendations, and data organization strategies. " +
                "Provide your response in a structured JSON format with clear, actionable recommendations."));

            String userMessage = "Here is the Excel data:\n\n" + excelData + "\n\n";
            if (context != null && !context.trim().isEmpty()) {
                userMessage += "Additional context: " + context + "\n\n";
            }
            
            userMessage += "Please provide intelligent suggestions for this data, including:\n" +
                          "- Data cleaning recommendations\n" +
                          "- Formatting suggestions\n" +
                          "- Analysis approaches\n" +
                          "- Chart/visualization ideas\n" +
                          "- Formula recommendations\n" +
                          "- Data organization improvements";
            
            messages.add(new AiRequest.Message("user", userMessage));

            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(messages);

            // 发送请求到AI服务
            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();

            // 解析AI响应
            Map<String, Object> suggestions = parseAISuggestions(aiResponseContent);

            result.put("success", true);
            result.put("data", suggestions);
            result.put("rawResponse", aiResponseContent);

            logger.info("Successfully generated AI suggestions for file: {}", file.getOriginalFilename());

        } catch (Exception e) {
            logger.error("Error generating AI suggestions for file: {}", file.getOriginalFilename(), e);
            result.put("success", false);
            result.put("error", "Error generating AI suggestions: " + e.getMessage());
        }

        return result;
    }

    /**
     * 分析数据类型并提供相应建议
     */
    public Map<String, Object> analyzeDataTypesAndSuggest(MultipartFile file) {
        logger.info("Analyzing data types and providing suggestions for file: {}", file.getOriginalFilename());

        Map<String, Object> result = new HashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                result.put("success", false);
                result.put("error", "File is required");
                return result;
            }

            org.apache.poi.ss.usermodel.Workbook workbook = excelService.loadWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, DataTypeStats> dataTypeAnalysis = analyzeSheetDataTypes(sheet);

            // 构建AI请求以获取针对特定数据类型的建议
            StringBuilder analysisSummary = new StringBuilder();
            for (Map.Entry<String, DataTypeStats> entry : dataTypeAnalysis.entrySet()) {
                analysisSummary.append(entry.getKey()).append(": ")
                              .append(entry.getValue().toString()).append("\n");
            }

            List<AiRequest.Message> messages = new ArrayList<>();
            messages.add(new AiRequest.Message("system",
                "You are an Excel data type analysis expert. Based on the provided data type analysis, " +
                "suggest optimal Excel configurations, formatting options, and analytical approaches for each data type. " +
                "Consider how to best represent different data types in Excel for maximum usability and analysis."));

            String userMessage = "Data type analysis for Excel sheet:\n\n" + analysisSummary.toString() + 
                                "\n\nPlease provide specific suggestions for handling each data type, " +
                                "including formatting recommendations, formula suggestions, " +
                                "and best practices for data management based on the types present.";

            messages.add(new AiRequest.Message("user", userMessage));

            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(messages);

            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();

            Map<String, Object> suggestions = parseAISuggestions(aiResponseContent);

            result.put("success", true);
            result.put("data", suggestions);
            result.put("dataTypeAnalysis", dataTypeAnalysis);
            result.put("rawResponse", aiResponseContent);

            logger.info("Successfully analyzed data types and provided suggestions for file: {}", file.getOriginalFilename());

        } catch (Exception e) {
            logger.error("Error analyzing data types for file: {}", file.getOriginalFilename(), e);
            result.put("success", false);
            result.put("error", "Error analyzing data types: " + e.getMessage());
        }

        return result;
    }

    /**
     * 提供格式化建议
     */
    public Map<String, Object> getFormattingSuggestions(MultipartFile file) {
        logger.info("Getting formatting suggestions for file: {}", file.getOriginalFilename());

        Map<String, Object> result = new HashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                result.put("success", false);
                result.put("error", "File is required");
                return result;
            }

            org.apache.poi.ss.usermodel.Workbook workbook = excelService.loadWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            // 分析当前格式
            Map<String, Object> currentFormatAnalysis = analyzeCurrentFormatting(sheet);

            List<AiRequest.Message> messages = new ArrayList<>();
            messages.add(new AiRequest.Message("system",
                "You are an Excel formatting expert. Based on the current formatting analysis, " +
                "suggest improvements to make the spreadsheet more readable, professional, and functional. " +
                "Consider header formatting, data alignment, color schemes, borders, and conditional formatting."));

            String userMessage = "Current formatting analysis:\n\n" + currentFormatAnalysis.toString() + 
                                "\n\nPlease provide specific formatting suggestions to improve the appearance " +
                                "and usability of this Excel sheet. Include suggestions for fonts, colors, " +
                                "borders, alignment, and any other formatting that would enhance the data presentation.";

            messages.add(new AiRequest.Message("user", userMessage));

            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(messages);

            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();

            Map<String, Object> suggestions = parseAISuggestions(aiResponseContent);

            result.put("success", true);
            result.put("data", suggestions);
            result.put("currentFormatAnalysis", currentFormatAnalysis);
            result.put("rawResponse", aiResponseContent);

            logger.info("Successfully generated formatting suggestions for file: {}", file.getOriginalFilename());

        } catch (Exception e) {
            logger.error("Error generating formatting suggestions for file: {}", file.getOriginalFilename(), e);
            result.put("success", false);
            result.put("error", "Error generating formatting suggestions: " + e.getMessage());
        }

        return result;
    }

    /**
     * 提供性能优化建议
     */
    public Map<String, Object> getPerformanceSuggestions(MultipartFile file) {
        logger.info("Getting performance suggestions for file: {}", file.getOriginalFilename());

        Map<String, Object> result = new HashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                result.put("success", false);
                result.put("error", "File is required");
                return result;
            }

            org.apache.poi.ss.usermodel.Workbook workbook = excelService.loadWorkbook(file);

            // 分析工作簿结构
            int sheetCount = workbook.getNumberOfSheets();
            int totalRows = 0;
            int totalCells = 0;

            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                totalRows += sheet.getLastRowNum() + 1;
                
                for (Row row : sheet) {
                    totalCells += row.getLastCellNum();
                }
            }

            List<AiRequest.Message> messages = new ArrayList<>();
            messages.add(new AiRequest.Message("system",
                "You are an Excel performance optimization expert. Based on the file statistics, " +
                "provide suggestions to improve Excel file performance, reduce file size, " +
                "and improve calculation speed. Consider data structure, formula optimization, " +
                "and storage best practices."));

            String userMessage = String.format(
                "Excel file statistics:\n" +
                "- Number of sheets: %d\n" +
                "- Total rows: %d\n" +
                "- Estimated total cells: %d\n\n" +
                "Please provide performance optimization suggestions for this Excel file. " +
                "Include recommendations for:\n" +
                "- Data structure improvements\n" +
                "- Formula optimization\n" +
                "- File size reduction\n" +
                "- Calculation speed improvements\n" +
                "- Memory usage optimization",
                sheetCount, totalRows, totalCells);

            messages.add(new AiRequest.Message("user", userMessage));

            AiRequest aiRequest = new AiRequest();
            aiRequest.setMessages(messages);

            AiResponse aiResponse = aiService.generateResponse(aiRequest);
            String aiResponseContent = aiResponse.getChoices()[0].getMessage().getContent();

            Map<String, Object> suggestions = parseAISuggestions(aiResponseContent);

            result.put("success", true);
            result.put("data", suggestions);
            result.put("fileStats", Map.of(
                "sheetCount", sheetCount,
                "totalRows", totalRows,
                "totalCells", totalCells
            ));
            result.put("rawResponse", aiResponseContent);

            logger.info("Successfully generated performance suggestions for file: {}", file.getOriginalFilename());

        } catch (Exception e) {
            logger.error("Error generating performance suggestions for file: {}", file.getOriginalFilename(), e);
            result.put("success", false);
            result.put("error", "Error generating performance suggestions: " + e.getMessage());
        }

        return result;
    }

    /**
     * 解析AI建议响应
     */
    private Map<String, Object> parseAISuggestions(String aiResponse) {
        // 这里可以实现更复杂的JSON解析逻辑
        // 目前我们返回一个包含原始响应的简单结构
        Map<String, Object> parsed = new HashMap<>();
        parsed.put("rawContent", aiResponse);
        
        // 尝试识别常见的建议类别
        if (aiResponse.toLowerCase().contains("format") || aiResponse.toLowerCase().contains("color") || 
            aiResponse.toLowerCase().contains("font") || aiResponse.toLowerCase().contains("border")) {
            parsed.put("hasFormattingSuggestions", true);
        }
        
        if (aiResponse.toLowerCase().contains("chart") || aiResponse.toLowerCase().contains("graph") || 
            aiResponse.toLowerCase().contains("visual")) {
            parsed.put("hasVisualizationSuggestions", true);
        }
        
        if (aiResponse.toLowerCase().contains("formula") || aiResponse.toLowerCase().contains("calculation") || 
            aiResponse.toLowerCase().contains("sum") || aiResponse.toLowerCase().contains("function")) {
            parsed.put("hasFormulaSuggestions", true);
        }
        
        if (aiResponse.toLowerCase().contains("clean") || aiResponse.toLowerCase().contains("remove") || 
            aiResponse.toLowerCase().contains("duplicate") || aiResponse.toLowerCase().contains("missing")) {
            parsed.put("hasDataCleaningSuggestions", true);
        }

        return parsed;
    }

    /**
     * 分析工作表中的数据类型
     */
    private Map<String, DataTypeStats> analyzeSheetDataTypes(Sheet sheet) {
        Map<String, DataTypeStats> typeStats = new HashMap<>();
        typeStats.put("text", new DataTypeStats());
        typeStats.put("number", new DataTypeStats());
        typeStats.put("date", new DataTypeStats());
        typeStats.put("boolean", new DataTypeStats());
        typeStats.put("formula", new DataTypeStats());
        typeStats.put("empty", new DataTypeStats());

        for (Row row : sheet) {
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        typeStats.get("text").increment();
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            typeStats.get("date").increment();
                        } else {
                            typeStats.get("number").increment();
                        }
                        break;
                    case BOOLEAN:
                        typeStats.get("boolean").increment();
                        break;
                    case FORMULA:
                        typeStats.get("formula").increment();
                        break;
                    case BLANK:
                        typeStats.get("empty").increment();
                        break;
                    default:
                        break;
                }
            }
        }

        return typeStats;
    }

    /**
     * 分析当前格式设置
     */
    private Map<String, Object> analyzeCurrentFormatting(Sheet sheet) {
        Map<String, Object> formatAnalysis = new HashMap<>();
        
        int rows = 0, cols = 0;
        int formattedCells = 0;
        int cellsWithBorders = 0;
        int cellsWithColors = 0;

        for (Row row : sheet) {
            rows++;
            for (Cell cell : row) {
                cols = Math.max(cols, cell.getColumnIndex() + 1);
                
                CellStyle style = cell.getCellStyle();
                if (style != null) {
                    formattedCells++;
                    
                    // 检查是否有边框
                    if (style.getBorderBottom() != BorderStyle.NONE || 
                        style.getBorderTop() != BorderStyle.NONE ||
                        style.getBorderLeft() != BorderStyle.NONE || 
                        style.getBorderRight() != BorderStyle.NONE) {
                        cellsWithBorders++;
                    }
                    
                    // 检查是否有颜色
                    if (style.getFillForegroundColor() != 0 || style.getFillBackgroundColor() != 0) {
                        cellsWithColors++;
                    }
                }
            }
        }

        formatAnalysis.put("totalRows", rows);
        formatAnalysis.put("totalCols", cols);
        formatAnalysis.put("formattedCells", formattedCells);
        formatAnalysis.put("cellsWithBorders", cellsWithBorders);
        formatAnalysis.put("cellsWithColors", cellsWithColors);

        return formatAnalysis;
    }

    /**
     * 数据类型统计内部类
     */
    @SuppressWarnings("unused")
    private static class DataTypeStats {
        private int count = 0;
        private int emptyCount = 0;

        public void increment() {
            count++;
        }

        public void incrementEmpty() {
            emptyCount++;
        }

        @Override
        public String toString() {
            return String.format("Count: %d", count);
        }

        public int getCount() {
            return count;
        }

        public int getEmptyCount() {
            return emptyCount;
        }
    }
}